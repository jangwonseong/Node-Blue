package com.samsa.node.in;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samsa.annotation.NodeType;
import com.samsa.core.Message;
import com.samsa.core.node.InNode;

import lombok.extern.slf4j.Slf4j;

/**
 * MQTT 출력 노드 클래스. MQTT 브로커에 연결하고 메시지를 발행하는 기능을 수행합니다.
 */
@NodeType("MqttOutNode")
@Slf4j
public class MqttOutNode extends InNode {

    private MqttClient mqttClient;
    private String broker;
    private String clientId;

    /**
     * 주어진 브로커 URL과 클라이언트 ID로 MqttOutNode 객체를 생성합니다.
     * 
     * @param broker MQTT 브로커의 URL
     * @param clientId 연결에 사용할 클라이언트 ID
     * @throws NullPointerException broker 또는 clientId가 null인 경우 예외 발생
     */
    @JsonCreator
    public MqttOutNode(@JsonProperty("broker") String broker,
            @JsonProperty("clientId") String clientId) {
        super();
        if (Objects.isNull(broker) || Objects.isNull(clientId)) {
            throw new NullPointerException("브로커와 클라이언트 ID는 null일 수 없습니다.");
        }
        this.broker = broker;
        this.clientId = clientId;
    }

    /**
     * 수신된 메시지를 MQTT 브로커로 발행합니다.
     * 
     * @param message 처리할 메시지 객체
     */
    @Override
    protected void onMessage(Message message) {
        // 메시지 또는 페이로드가 null인 경우 로그 경고
        if (message == null || message.getPayload() == null) {
            log.warn("수신된 메시지 또는 페이로드가 null입니다.");
            return;
        }

        log.info("수신된 메시지: {}", message.getPayload());

        try {
            // MQTT 클라이언트가 없거나 연결되지 않은 경우 연결 처리
            if (mqttClient == null || !mqttClient.isConnected()) {
                mqttClient = new MqttClient(broker, clientId);
                mqttClient.connect(); // 브로커에 연결
                log.info("MQTT 클라이언트가 브로커에 연결되었습니다: {}", broker);
            }

            // 페이로드가 Map인 경우 처리
            if (message.getPayload() instanceof Map) {
                Map<String, Object> payload = (Map<String, Object>) message.getPayload();
                String topic = (String) payload.get("topic");
                String payloadData = payload.get("payload").toString();
                MqttMessage mqttMessage = new MqttMessage(payloadData.getBytes());
                mqttClient.publish(topic, mqttMessage); // MQTT 토픽으로 메시지 발행
                log.info("토픽 '{}'에 메시지가 발행되었습니다.", topic);
            }
            // 페이로드가 List인 경우 처리
            else if (message.getPayload() instanceof List) {
                List<Map<String, Object>> payloadList =
                        (List<Map<String, Object>>) message.getPayload();
                for (Map<String, Object> item : payloadList) {
                    String topic = (String) item.get("topic");
                    String payloadData = item.get("payload").toString();
                    MqttMessage mqttMessage = new MqttMessage(payloadData.getBytes());
                    mqttClient.publish(topic, mqttMessage); // MQTT 토픽으로 메시지 발행
                    log.info("토픽 '{}'에 메시지가 발행되었습니다.", topic);
                }
            }
            // 예상하지 못한 페이로드 타입이 올 경우 처리
            else {
                log.warn("예상하지 못한 페이로드 타입입니다: {}", message.getPayload().getClass());
            }

        } catch (Exception e) {
            // 예외 발생 시 오류 로그
            log.error("MQTT로 메시지를 발행하는 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
