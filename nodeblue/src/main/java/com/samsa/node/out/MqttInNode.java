package com.samsa.node.out;

import java.util.Objects;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samsa.annotation.NodeType;
import com.samsa.core.Message;
import com.samsa.core.node.OutNode;

import lombok.extern.slf4j.Slf4j;

/**
 * MQTT 입력 노드 클래스. MQTT 브로커로부터 메시지를 구독하고 처리합니다.
 */
@NodeType("MqttInNode")
@Slf4j
public class MqttInNode extends OutNode {
    private MqttClient mqttClient;
    private final String broker;
    private final String clientId;
    private final String[] topics;
    private int[] qos;
    
    /**
     * Jackson 역직렬화를 위한 생성자
     */
    @JsonCreator
    public MqttInNode(
            @JsonProperty("broker") String broker,
            @JsonProperty("clientId") String clientId,
            @JsonProperty("topics") String[] topics) {
        super();
        if (Objects.isNull(broker) || Objects.isNull(clientId)) {
            throw new NullPointerException("브로커와 클라이언트 ID는 null일 수 없습니다.");
        }
        this.broker = broker;
        this.clientId = clientId;
        this.topics = topics;
        this.qos = new int[topics.length];
    }
    
    public MqttInNode(String broker, String clientId) {
        super();
        if (Objects.isNull(broker) || Objects.isNull(clientId)) {
            throw new NullPointerException("브로커와 클라이언트 ID는 null일 수 없습니다.");
        }
        this.topics = new String[0];
        this.qos = new int[0];
        this.broker = broker;
        this.clientId = clientId;
    }
    

    @Override
    protected Message createMessage() {
        return null;
    }

    @Override
    public void run() {
        try {
            mqttClient = new MqttClient(broker, clientId);
            mqttClient.connect();
            
            for (String topic : topics) {
                mqttClient.subscribe(topic);
                log.info("토픽 '{}'을 구독했습니다.", topic);
            }

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT 연결이 끊어졌습니다.", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    log.info("토픽 '{}'에서 메시지 수신: {}", topic, new String(mqttMessage.getPayload()));
                    String payload = String.format("{topic: %s, %s}", topic,
                            new String(mqttMessage.getPayload()));
                    Message message = new Message(payload);
                    emit(message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 발행이 완료되면 호출됩니다.
                }
            });

            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(1000);
            }
        } catch (MqttException | InterruptedException e) {
            log.error("MQTT 처리 중 오류 발생", e);
            Thread.currentThread().interrupt();
        }
    }
}