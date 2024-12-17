package com.samsa.node.out;

import java.util.Objects;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.samsa.core.Message;
import com.samsa.core.node.OutNode;

import lombok.extern.slf4j.Slf4j;

/**
 * MQTT 입력 노드 클래스. MQTT 브로커로부터 메시지를 구독하고 처리합니다.
 */
@Slf4j
public class MqttInNode extends OutNode{
    // mqtt를 in하고 자바 내에서 out 하는 노드

    private MqttClient mqttClient;
    private String broker;
    private String clientId;
    private String[] topics;
    private int[] qos;

    private Message message;

    /**
     * 주어진 브로커 URL과 클라이언트 ID로 MqttInNode 객체를 생성합니다.
     *
     * @param broker MQTT 브로커의 URL
     * @param clientId 연결에 사용할 클라이언트 ID
     * @param port 브로커 포트 번호
     * @throws NullPointerException broker 또는 clientId가 null인 경우
     */
    public MqttInNode(String broker, String clientId) {
        super();
        if (Objects.isNull(broker) || Objects.isNull(clientId)) {
            throw new NullPointerException("브로커와 클라이언트 ID는 null일 수 없습니다.");
        }
        this.broker = broker;
        this.clientId = clientId;
    }

    /**
     * 주어진 브로커 URL, 클라이언트 ID 및 토픽으로 MqttInNode 객체를 생성합니다.
     *
     * @param broker MQTT 브로커의 URL
     * @param clientId 연결에 사용할 클라이언트 ID
     * @param topics 구독할 토픽 목록
     */
    public MqttInNode(String broker, String clientId, String[] topics) {
        this(broker, clientId);
        this.topics = topics;
        this.qos = new int[topics.length];
    }

    @Override
    protected Message createMessage() {
        try {
            mqttClient = new MqttClient(broker, clientId);
            mqttClient.connect();
            // 각각의 토픽을 개별적으로 구독
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
                    // MQTT 메시지를 수신할 때마다 호출됩니다.
                    // 수신된 메시지를 Message 객체로 변환하여 emit 메서드를 통해 전송합니다.
                    log.info("토픽 '{}'에서 메시지 수신: {}", topic, new String(mqttMessage.getPayload()));

                    // Message 객체 생성 및 emit 호출
                    // Message message = new Message(new String(mqttMessage.getPayload()));

                    // createMessage 메서드로 byte 데이터 Message클래스 기반 데이터로 변환
                    message = new Message(new String(mqttMessage.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 발행이 완료되면 호출됩니다.
                }
            });

            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(1000); // 주기적으로 실행할 작업이 있다면 여기에 추가
            }
        } catch (MqttException | InterruptedException e) {
            log.error("MQTT 처리 중 오류 발생", e);
            Thread.currentThread().interrupt();
        }

        return message;
    }

}
