package com.samsa.node.out;

import java.util.Objects;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.samsa.core.Message;
import com.samsa.core.node.OutNode;

import lombok.extern.slf4j.Slf4j;

/**
 * MQTT 입력 노드 클래스. MQTT 브로커로부터 메시지를 구독하고 처리합니다.
 */
@Slf4j
public class MqttInNode extends OutNode {
    // mqtt를 in하고 자바 내에서 out 하는 노드

    private MqttClient mqttClient;
    private MqttConnectOptions connectOptions;
    private String broker;
    private String clientId;
    private String[] topics;
    private int[] qos;

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
        // 메시지 생성 로직 구현
        // 이 부분은 MQTT 메시지를 수신하여 Message 객체로 변환하는 로직을 작성해야 합니다.
        // 실제 메시지 반환하도록 구현 필요

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
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
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
            connectOptions.setAutomaticReconnect(true);
            connectOptions.setCleanSession(true);
            connectOptions.setConnectionTimeout(10);
            
            mqttClient.connect(connectOptions);
            
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
                    // MQTT 메시지를 수신할 때마다 호출됩니다.
                    // 수신된 메시지를 Message 객체로 변환하여 emit 메서드를 통해 전송합니다.
                    log.info("토픽 '{}'에서 메시지 수신: {}", topic, new String(mqttMessage.getPayload()));

                    // Message 객체 생성 및 emit 호출
                    // Message message = new Message(new String(mqttMessage.getPayload()));

                    // createMessage 메서드로 byte 데이터 Message클래스 기반 데이터로 변환
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

        } catch (MqttException e) {
            log.error("MQTT 처리 중 오류 발생", e);
        }
    }

}
