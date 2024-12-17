package com.samsa.node.inout;

import java.util.HashMap;
import java.util.Map;
import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;
import lombok.extern.slf4j.Slf4j;

/**
 * ModbusToMqttNode 클래스는 Modbus에서 수신한 메시지를 처리하여
 * MQTT 호환 메시지로 포맷한 후 송출하는 노드입니다.
 */
@Slf4j
public class ModbusToMqttNode extends InOutNode {

    /**
     * MQTT로 메시지를 발행할 토픽입니다.
     */
    private final String topic;

    /**
     * 지정된 MQTT 토픽과 브로커로 ModbusToMqttNode를 생성합니다.
     *
     * @param topic MQTT로 발행할 토픽
     */
    public ModbusToMqttNode(String topic) {
        super();
        if (topic == null || topic.isEmpty()) {
            log.error("MQTT 토픽은 null이거나 비어 있을 수 없습니다.");
            throw new IllegalArgumentException("MQTT 토픽이 지정되어야 합니다.");
        }
        this.topic = topic;
        log.info("ModbusToMqttNode가 초기화되었습니다. 토픽: {}", topic);
    }

    /**
     * Modbus에서 수신한 메시지를 처리하여 MQTT 포맷으로 변환한 후 송출합니다.
     *
     * @param message Modbus에서 수신한 메시지
     */
    @Override
    protected void onMessage(Message message) {
        if (message == null || message.getPayload() == null) {
            log.warn("null이거나 비어 있는 메시지를 수신했습니다. 처리를 건너뜁니다.");
            return;
        }

        try {
            // MQTT 페이로드 생성
            Map<String, Object> mqttPayload = new HashMap<>();
            mqttPayload.put("topic", topic);
            mqttPayload.put("payload", String.format("{\"time\": %d, \"value\": \"%s\"}",
                    System.currentTimeMillis(), message.getPayload().toString()));

            // MQTT 페이로드로 새로운 메시지 생성
            Message newMessage = new Message(mqttPayload);

            // 새로운 메시지 송출
            emit(newMessage);
            log.info("토픽: {}에 대한 메시지가 성공적으로 처리 및 송출되었습니다.", topic);
        } catch (Exception e) {
            log.error("메시지 처리 중 오류가 발생했습니다: {}", e.getMessage(), e);
        }
    }
}
