package com.samsa.node.in;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.samsa.core.Pipe;
import com.samsa.node.out.MqttInNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqttInNodeTest {
    MqttInNode mqttInNode;
    MqttOutNode mqttOutNode;

    @BeforeEach
    void setUp() {
        String[] topics = {"application/#", "applicatio"};
        mqttInNode = new MqttInNode("tcp://192.168.70.203:1883", "1235", topics); // 노드부터 만들었을 때
        Pipe pipe = new Pipe();
        mqttInNode.getPort().addPipe(pipe);

        mqttOutNode = new MqttOutNode("tcp://192.168.70.203:1883", "4321", topics[1]);
        mqttOutNode.getPort().addPipe(pipe);
    }

    /*
     * 테스트가 바로 끝나는 이유는 JUnit 테스트 프레임워크가 기본적으로 테스트 메서드가 종료되면 테스트를 완료하기 때문입니다. 
     * MqttInNode를 스레드로 실행해도 테스트 메서드 자체는 별도의 스레드에서 실행되므로, 
     * 메인 테스트 스레드가 종료되면 JUnit은 테스트를 완료합니다. 
     * 따라서, 백그라운드에서 실행 중인 스레드가 계속 실행되더라도 메인 스레드가 종료되면 테스트는 끝납니다.
     */
    @Test
    void start() throws InterruptedException{
        Thread thread = new Thread(mqttInNode);
        Thread thread2 = new Thread(mqttOutNode);
        thread.start();
        thread2.start();

        // 메인 스레드가 백그라운드 스레드를 기다리도록 함
        thread.join(); // 이 라인이 없으면 메인 스레드는 즉시 종료됨
        thread2.join();
    }
}