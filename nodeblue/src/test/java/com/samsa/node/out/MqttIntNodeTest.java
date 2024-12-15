package com.samsa.node.out;

import org.junit.jupiter.api.*;

import com.samsa.core.InPort;
import com.samsa.core.OutPort;
import com.samsa.core.Pipe;
import com.samsa.node.in.MqttOutNode;
import com.samsa.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MqttIntNodeTest {

    MqttInNode mqttInNode;
    MqttOutNode mqttOutNode;

    @BeforeEach
    void setUp() {
        // String[] topics = {"application/#", "123"};
        // mqttInNode = new MqttInNode("tcp://192.168.70.203:1883", "1235", topics); // 노드부터 만들었을 때
        // OutPort outPort = new OutPort(mqttInNode);
        // Pipe pipe = new Pipe();
        // outPort.addPipe(pipe);
        // mqttInNode.setOutPort(outPort);
    }

    // @Test
    // void constructorTest() {
    //     String[] topics = {"application/#", "123"};
    //     Assertions.assertThrows(NullPointerException.class, () -> new MqttInNode("tcp://192.168.70.203:1883", null, topics));

    //     Assertions.assertThrows(NullPointerException.class, () -> new MqttInNode("123", null, topics));
    // }

    @Test
    void start() {
        String[] topics = {"application/#", "123"};
        mqttInNode = new MqttInNode("tcp://192.168.70.203:1883", "1235", topics); // 노드부터 만들었을 때
        OutPort outPort = new OutPort(mqttInNode);
        Pipe pipe = new Pipe();
        outPort.addPipe(pipe);
        mqttInNode.setOutPort(outPort);

        mqttInNode.start();
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Assertions.assertDoesNotThrow(() -> mqttInNode.start());
    }
}
