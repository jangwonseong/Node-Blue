package com.samsa.node.out;

import java.util.Objects;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
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
    private MqttConnectOptions connectOptions;
    private String broker;
    private String clientId;
    private String[] topics;
    private int[] qos;

    /**
     * Jackson 역직렬화를 위한 생성자
     */
    @JsonCreator
    public MqttInNode(@JsonProperty("broker") String broker,
            @JsonProperty("clientId") String clientId, @JsonProperty("topics") String[] topics) {
        super();
        if (Objects.isNull(broker) || Objects.isNull(clientId)) {
            throw new IllegalArgumentException("브로커와 클라이언트 ID는 null일 수 없습니다.");
        }
        this.broker = broker;
        this.clientId = clientId;
        this.topics = topics;
        this.qos = new int[topics.length];
        // 기본 QoS 레벨을 설정 (0)
        for (int i = 0; i < topics.length; i++) {
            this.qos[i] = 0;
        }
    }

    @Override
    protected Message createMessage() {
        // 이 메서드는 MQTT 메시지를 직접 생성하지 않으므로 null 반환
        return null;
    }

    /**
     * MQTT 연결이 끊어졌을 때 재연결을 시도하는 메서드입니다.
     * 
     * @param client MQTT 클라이언트 인스턴스
     * @param options MQTT 연결 옵션
     * @param cause 연결 끊김 원인
     * @param topics 구독할 토픽 배열
     * @param qos QoS 레벨 배열
     */
    private void handleConnectionLost(MqttClient client, MqttConnectOptions options,
            Throwable cause, String[] topics, int[] qos) {
        log.trace("연결이 끊어졌습니다: {}", cause.getMessage());

        while (!client.isConnected()) {
            try {
                log.info("재연결 시도 중...");
                client.connect(options);
                log.info("재연결 성공!");
                client.subscribe(topics, qos);
                log.info("토픽 재구독 완료");
            } catch (MqttException e) {
                log.error("재연결 실패: {}", e.getMessage());
                try {
                    Thread.sleep(5000); // 5초 후 재시도
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // 인터럽트 처리
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            mqttClient = new MqttClient(broker, clientId);
            connectOptions = new MqttConnectOptions();
            connectOptions.setAutomaticReconnect(true); // 자동 재연결 활성화
            connectOptions.setCleanSession(true); // 클린 세션
            connectOptions.setConnectionTimeout(10); // 연결 타임아웃 설정 (초)

            mqttClient.connect(connectOptions); // 연결 시도

            // 토픽 구독
            for (String topic : topics) {
                mqttClient.subscribe(topic);
                log.info("토픽 '{}'을 구독했습니다.", topic);
            }

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT 연결이 끊어졌습니다.", cause);
                    handleConnectionLost(mqttClient, connectOptions, cause, topics, qos);
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    log.info("토픽 '{}'에서 메시지 수신: {}", topic, new String(mqttMessage.getPayload()));
                    String payload = String.format("{topic: %s, %s}", topic,
                            new String(mqttMessage.getPayload()));
                    Message message = new Message(payload);
                    emit(message); // 수신된 메시지를 처리
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 발행 완료 시 호출
                }
            });

        } catch (MqttException e) {
            log.error("MQTT 처리 중 오류 발생", e);
        }
    }
}
