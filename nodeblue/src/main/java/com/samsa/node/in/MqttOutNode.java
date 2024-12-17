package com.samsa.node.in;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.samsa.core.Message;
import com.samsa.core.node.InNode;

import lombok.extern.slf4j.Slf4j;

/**
 * MQTT 출력 노드 클래스. MQTT 브로커에 연결하고 메시지를 발행합니다.
 */
@Slf4j
public class MqttOutNode extends InNode{
    // 자바에서 in하고 mqtt를 브로커로 out 하는 노드

    private MqttClient mqttClient;
    private String broker;
    private String clientId;
    private String topic;

    /**
     * 주어진 브로커 URL과 클라이언트 ID로 MqttOutNode 객체를 생성합니다.
     *
     * @param broker MQTT 브로커의 URL
     * @param clientId 연결에 사용할 클라이언트 ID
     * @param outPort 브로커 포트 번호
     * @throws NullPointerException broker 또는 clientId가 null인 경우
     */
    public MqttOutNode(String broker, String clientId) {
        super();
        if (Objects.isNull(broker) || Objects.isNull(clientId)) {
            throw new NullPointerException("브로커와 클라이언트 ID는 null일 수 없습니다.");
        }
        this.broker = broker;
        this.clientId = clientId;
    }

    /**
     * 주어진 브로커 URL, 클라이언트 ID, 토픽으로 MqttOutNode 객체를 생성합니다.
     *
     * @param broker MQTT 브로커의 URL
     * @param clientId 연결에 사용할 클라이언트 ID
     * @param outPort 브로커 포트 번호
     * @param topic 메시지를 발행할 토픽
     */
    public MqttOutNode(String broker, String clientId, String topic) {
        this(broker, clientId);
        this.topic = topic;
    }

    @Override
    protected void onMessage(Message message) {
        // 브로커로 보낼 메시지를 가공하는 곳
        log.info(message.getPayload().toString());
        try{
            mqttClient = new MqttClient(broker, clientId); // mqtt 클라이언트가 해당 브로커와 연결할 것이라고 알려줘야함.
            System.out.println("mqttClient create");
            mqttClient.connect(); // 지정된 브로커에 연결을 시도합니다.

            while(true) {
                // 지정된 주제와 메시지를 MQTT 브로커에 발행합니다. 브로커로 토픽과 메시지(바이트로 변환)를 날림.
                mqttClient.publish(topic, new MqttMessage(message.toString().getBytes()));
                log.info("Message send!!");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
    }
}
