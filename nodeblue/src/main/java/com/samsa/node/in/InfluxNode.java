package com.samsa.node.in;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.samsa.core.Message;
import com.samsa.core.node.InNode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class InfluxNode extends InNode {
    private InfluxDBClient influxDBClient;
    private WriteApiBlocking writeApi;
    private String measurement;
    private List<String> tagKeys;
    private List<String> fieldKeys;

    public InfluxNode(String url, String token, String org, String bucket) {
        super();
        initialize(url, token, org, bucket);
    }

    public InfluxNode(UUID id, String url, String token, String org, String bucket) {
        super(id);
        initialize(url, token, org, bucket);
    }

    private void initialize(String url, String token, String org, String bucket) {
        this.tagKeys = new ArrayList<>();
        this.fieldKeys = new ArrayList<>();

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
                log.info("Successfully connected to InfluxDB: {}", url);
            } catch (Exception e) {
                log.error("Failed to connect to InfluxDB: {}", e.getMessage());
                throw new IllegalStateException("Failed to connect to InfluxDB", e);
            }

        } catch (Exception e) {
            log.error("Error initializing InfluxNode: {}", e.getMessage());
            throw new IllegalStateException("Error initializing InfluxNode", e);
        }
    }

    public void setMeasurement(String measurement) {
        if (measurement == null || measurement.trim().isEmpty()) {
            throw new IllegalArgumentException("Measurement name cannot be empty");
        }
        this.measurement = measurement.trim();
        log.debug("Measurement set to: {}", this.measurement);
    }

    public void addTagKey(String tagKey) {
        if (tagKey != null && !tagKey.trim().isEmpty()) {
            this.tagKeys.add(tagKey.trim());
            log.debug("Added tag key: {}", tagKey.trim());
        }
    }

    public void addFieldKey(String fieldKey) {
        if (fieldKey != null && !fieldKey.trim().isEmpty()) {
            this.fieldKeys.add(fieldKey.trim());
            log.debug("Added field key: {}", fieldKey.trim());
        }
    }

    @Override
    protected void onMessage(Message message) {
        try {
            if (message == null) {
                log.warn("Node[{}] - Received null message", getId());
                return;
            }

            if (measurement == null) {
                log.error("Node[{}] - Measurement name not set", getId());
                return;
            }

            Point point = createPoint(message);
            if (point != null) {
                writeApi.writePoint(point);
                log.debug("Node[{}] - Successfully wrote point to InfluxDB. MessageId: {}",
                        getId(), message.getId());
            }

        } catch (Exception e) {
            log.error("Node[{}] - Error writing to InfluxDB: {}", getId(), e.getMessage());
        }
    }

    private Point createPoint(Message message) {
        try {
            Object payloadObj = message.getPayload();
            if (!(payloadObj instanceof Map)) {
                log.warn("Node[{}] - Payload is not a Map. MessageId: {}", getId(), message.getId());
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) payloadObj;
            Map<String, Object> metadata = message.getMetadata();

            Point point = Point.measurement(measurement)
                    .time(System.currentTimeMillis(), WritePrecision.MS);

            // 태그 추가
            for (String tagKey : tagKeys) {
                if (metadata.containsKey(tagKey)) {
                    point.addTag(tagKey, String.valueOf(metadata.get(tagKey)));
                }
            }

            // 필드 추가
            boolean hasValidField = false;
            for (String fieldKey : fieldKeys) {
                if (payload.containsKey(fieldKey)) {
                    Object value = payload.get(fieldKey);
                    if (value instanceof Number) {
                        point.addField(fieldKey, (Number) value);
                        hasValidField = true;
                    } else if (value instanceof Boolean) {
                        point.addField(fieldKey, (Boolean) value);
                        hasValidField = true;
                    } else if (value instanceof String) {
                        point.addField(fieldKey, (String) value);
                        hasValidField = true;
                    }
                }
            }

            return hasValidField ? point : null;

        } catch (Exception e) {
            log.error("Node[{}] - Error creating point: {}", getId(), e.getMessage());
            return null;
        }
    }

    public void close() {
        try {
            if (writeApi != null) {
                ((AutoCloseable) writeApi).close();
            }
            if (influxDBClient != null) {
                influxDBClient.close();
                log.info("Node[{}] - Closed InfluxDB connection", getId());
            }
        } catch (Exception e) {
            log.error("Node[{}] - Error closing InfluxDB client: {}", getId(), e.getMessage());
        }
    }
}