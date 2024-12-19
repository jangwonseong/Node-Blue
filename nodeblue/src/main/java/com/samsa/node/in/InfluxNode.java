package com.samsa.node.in;

import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.samsa.annotation.NodeType;
import com.samsa.core.Message;
import com.samsa.core.node.InNode;

import lombok.extern.slf4j.Slf4j;

/**
 * InfluxDB에 데이터를 저장하는 노드 클래스입니다. 메시지를 수신하여 InfluxDB에 시계열 데이터로 저장합니다.
 * 
 * @version 1.0
 */
@NodeType("InfluxNode")
@Slf4j
public class InfluxNode extends InNode {
    private InfluxDBClient influxDBClient;
    private WriteApiBlocking writeApi;

    /**
     * InfluxDB 연결을 위한 기본 생성자입니다.
     * 
     * @param url InfluxDB 서버 URL (예: "http://localhost:8086")
     * @param token InfluxDB 인증 토큰
     * @param org InfluxDB 조직 이름
     * @param bucket 데이터를 저장할 버킷 이름
     * @throws IllegalStateException InfluxDB 연결 실패 시
     */
    @JsonCreator
    public InfluxNode(@JsonProperty("url") String url, @JsonProperty("token") String token,
            @JsonProperty("org") String org, @JsonProperty("bucket") String bucket) {
        super();
        initialize(url, token, org, bucket);
    }

    /**
     * ID를 지정하여 InfluxDB 연결을 생성하는 생성자입니다.
     * 
     * @param id 노드의 고유 식별자
     * @param url InfluxDB 서버 URL (예: "http://localhost:8086")
     * @param token InfluxDB 인증 토큰
     * @param org InfluxDB 조직 이름
     * @param bucket 데이터를 저장할 버킷 이름
     * @throws IllegalStateException InfluxDB 연결 실패 시
     */
    public InfluxNode(UUID id, String url, String token, String org, String bucket) {
        super(id);
        initialize(url, token, org, bucket);
    }

    /**
     * InfluxDB 클라이언트를 초기화하고 연결을 설정합니다. 초기화 성공 시 연결 테스트(ping)를 수행합니다.
     * 
     * @param url InfluxDB 서버 URL
     * @param token InfluxDB 인증 토큰
     * @param org InfluxDB 조직 이름
     * @param bucket 데이터를 저장할 버킷 이름
     * @throws IllegalStateException 클라이언트 초기화 또는 연결 실패 시
     */
    private void initialize(String url, String token, String org, String bucket) {
        try {
            this.influxDBClient =
                    InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
            this.writeApi = influxDBClient.getWriteApiBlocking();

            try {
                influxDBClient.ping();
                log.info("InfluxDB에 성공적으로 연결되었습니다: {}", url);
            } catch (Exception e) {
                log.error("InfluxDB 연결 실패: {}", e.getMessage());
                throw new IllegalStateException("InfluxDB 연결 실패", e);
            }
        } catch (Exception e) {
            log.error("InfluxNode 초기화 오류: {}", e.getMessage());
            throw new IllegalStateException("InfluxNode 초기화 오류", e);
        }
    }

    /**
     * 메시지를 수신하여 InfluxDB에 저장합니다. null 메시지는 무시되며, 저장 실패 시 오류가 기록됩니다.
     * 
     * @param message 처리할 메시지 객체
     */
    @Override
    protected void onMessage(Message message) {
        try {
            if (message == null) {
                log.warn("노드[{}] - null 메시지가 수신되었습니다", getId());
                return;
            }

            Point point = createPoint(message);
            if (point != null) {
                writeApi.writePoint(point);
                log.debug("노드[{}] - InfluxDB에 포인트를 성공적으로 기록했습니다. 메시지 ID: {}", getId(),
                        message.getId());
            }
        } catch (Exception e) {
            log.error("노드[{}] - InfluxDB 기록 중 오류 발생: {}", getId(), e.getMessage());
        }
    }

    /**
     * 메시지를 InfluxDB Point 객체로 변환합니다.
     * 
     * <p>
     * 지원하는 페이로드 형식:
     * 
     * <pre>
     * {
     *   "measurement": String,          // 필수
     *   "tags": Map<String, String>,    // 선택
     *   "fields": Map<String, Object>,  // 필수
     *   "time": Long                    // 선택 (없으면 현재 시간 사용)
     * }
     * </pre>
     * 
     * @param message 변환할 메시지
     * @return 생성된 Point 객체, 변환 실패 시 null
     */
    private Point createPoint(Message message) {
        try {
            Object payloadObj = message.getPayload();
            if (!(payloadObj instanceof Map)) {
                log.warn("노드[{}] - 페이로드가 Map이 아닙니다. 메시지 ID: {}", getId(), message.getId());
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) payloadObj;

            String measurement = (String) payload.get("measurement");
            Map<String, String> tags = (Map<String, String>) payload.get("tags");
            @SuppressWarnings("unchecked")
            Map<String, Object> fields = (Map<String, Object>) payload.get("fields");
            Long timestamp = (Long) payload.get("time");

            Point point = Point.measurement(measurement).time(timestamp, WritePrecision.MS);

            // 태그 추가
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                point.addTag(tag.getKey(), tag.getValue());
            }

            // 필드 추가 (double로 통일)
            for (Map.Entry<String, Object> field : fields.entrySet()) {
                point.addField(field.getKey(), ((Number) field.getValue()).doubleValue());
            }

            return point;

        } catch (Exception e) {
            log.error("노드[{}] - 포인트 생성 중 오류 발생: {}", getId(), e.getMessage());
            return null;
        }
    }

    /**
     * InfluxDB 연결을 안전하게 종료합니다. WriteApi와 InfluxDBClient 리소스를 해제합니다. 이 메서드는 노드가 종료될 때 반드시 호출되어야 합니다.
     */
    public void close() {
        try {
            if (writeApi != null) {
                ((AutoCloseable) writeApi).close();
            }
            if (influxDBClient != null) {
                influxDBClient.close();
                log.info("노드[{}] - InfluxDB 연결을 종료했습니다", getId());
            }
        } catch (Exception e) {
            log.error("노드[{}] - InfluxDB 클라이언트 종료 중 오류 발생: {}", getId(), e.getMessage());
        }
    }
}
