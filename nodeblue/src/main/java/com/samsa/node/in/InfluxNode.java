package com.samsa.node.in;

import java.util.Map;
import java.util.UUID;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.samsa.core.Message;
import com.samsa.core.node.InNode;

import lombok.extern.slf4j.Slf4j;

/**
 * InfluxNode는 메시지를 수신하고 데이터를 InfluxDB에 저장하는 역할을 합니다.
 * 메시지를 처리하고, 관련 필드와 태그를 추출하여 InfluxDB에 저장합니다.
 */
@Slf4j
public class InfluxNode extends InNode {
    private InfluxDBClient influxDBClient;
    private WriteApiBlocking writeApi;

    /**
     * 지정된 InfluxDB 연결 정보를 사용하여 InfluxNode를 생성합니다.
     *
     * @param url     InfluxDB 인스턴스의 URL
     * @param token   InfluxDB 인증 토큰
     * @param org     InfluxDB의 조직 이름
     * @param bucket  InfluxDB의 버킷 이름
     */
    public InfluxNode(String url, String token, String org, String bucket) {
        super();
        initialize(url, token, org, bucket);
    }

    /**
     * 지정된 ID와 InfluxDB 연결 정보를 사용하여 InfluxNode를 생성합니다.
     *
     * @param id      이 노드의 고유 식별자
     * @param url     InfluxDB 인스턴스의 URL
     * @param token   InfluxDB 인증 토큰
     * @param org     InfluxDB의 조직 이름
     * @param bucket  InfluxDB의 버킷 이름
     */
    public InfluxNode(UUID id, String url, String token, String org, String bucket) {
        super(id);
        initialize(url, token, org, bucket);
    }

    /**
     * 지정된 InfluxDB 연결 정보를 사용하여 InfluxNode를 초기화합니다.
     *
     * @param url     InfluxDB 인스턴스의 URL
     * @param token   InfluxDB 인증 토큰
     * @param org     InfluxDB의 조직 이름
     * @param bucket  InfluxDB의 버킷 이름
     */
    private void initialize(String url, String token, String org, String bucket) {
        try {
            // InfluxDB 클라이언트 생성
            this.influxDBClient = InfluxDBClientFactory.create(url,
                    token.toCharArray(),
                    org,
                    bucket);
            this.writeApi = influxDBClient.getWriteApiBlocking();

            // 연결 테스트
            try {
                influxDBClient.ping();
                log.info("성공적으로 InfluxDB에 연결되었습니다: {}", url);
            } catch (Exception e) {
                log.error("InfluxDB에 연결하지 못했습니다: {}", e.getMessage());
                throw new IllegalStateException("InfluxDB에 연결하지 못했습니다", e);
            }

        } catch (Exception e) {
            log.error("InfluxNode 초기화 중 오류 발생: {}", e.getMessage());
            throw new IllegalStateException("InfluxNode 초기화 중 오류 발생", e);
        }
    }

    /**
     * 메시지를 수신하고 InfluxDB에 데이터를 저장합니다.
     *
     * @param message 수신된 메시지
     */
    @Override
    protected void onMessage(Message message) {
        try {
            if (message == null) {
                log.warn("Node[{}] - null 메시지를 수신했습니다", getId());
                return;
            }

            log.debug("Node[{}] - 메시지를 수신했습니다: {}", getId(), message);

            Point point = createPoint(message);
            if (point != null) {
                writeApi.writePoint(point);
                log.debug("Node[{}] - InfluxDB에 포인트를 성공적으로 기록했습니다. MessageId: {}",
                        getId(), message.getId());
            } else {
                log.warn("Node[{}] - 포인트 생성이 null을 반환했습니다. MessageId: {}", getId(), message.getId());
            }

        } catch (Exception e) {
            log.error("Node[{}] - InfluxDB에 기록 중 오류 발생: {}", getId(), e.getMessage());
        }
    }

    /**
     * 메시지에서 포인트를 생성합니다.
     *
     * @param message 수신된 메시지
     * @return 생성된 포인트 객체
     */
    private Point createPoint(Message message) {
        try {
          Object payloadObj = message.getPayload();
            if (!(payloadObj instanceof Map)) {
                log.warn("Node[{}] - 페이로드가 Map이 아닙니다. MessageId: {}", getId(), message.getId());
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) payloadObj;

            String measurement = (String) payload.get("measurement");
            Map<String, String> tags = (Map<String, String>) payload.get("tags");
            Object field = payload.get("field");
            long time = (long) payload.get("time");

            log.debug("Node[{}] - 페이로드: {}로 포인트를 생성합니다", getId(), payload);

            Point point = Point.measurement(measurement)
                    .time(time, WritePrecision.MS);

            // 태그 추가
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                point.addTag(tag.getKey(), tag.getValue());
                log.debug("Node[{}] - 태그 추가: {} = {}", getId(), tag.getKey(), tag.getValue());
            }

            // 필드 추가
            if (field instanceof Number) {
                point.addField("field", (Number) field);
                log.debug("Node[{}] - 필드 추가: field = {}", getId(), field);
            } else if (field instanceof Boolean) {
                point.addField("field", (Boolean) field);
                log.debug("Node[{}] - 필드 추가: field = {}", getId(), field);
            } else if (field instanceof String) {
                point.addField("field", (String) field);
                log.debug("Node[{}] - 필드 추가: field = {}", getId(), field);
            } else {
                log.warn("Node[{}] - 유효한 필드가 없습니다. MessageId: {}", getId(), message.getId());
                return null;
            }

            return point;

        } catch (Exception e) {
            log.error("Node[{}] - 포인트 생성 중 오류 발생: {}", getId(), e.getMessage());
            return null;
        }
    }

    /**
     * InfluxDB 클라이언트를 닫습니다.
     */
    public void close() {
        try {
            if (writeApi != null) {
                ((AutoCloseable) writeApi).close();
            }
            if (influxDBClient != null) {
                influxDBClient.close();
                log.info("Node[{}] - InfluxDB 연결을 닫았습니다", getId());
            }
        } catch (Exception e) {
            log.error("Node[{}] - InfluxDB 클라이언트 닫기 중 오류 발생: {}", getId(), e.getMessage());
        }
    }
}