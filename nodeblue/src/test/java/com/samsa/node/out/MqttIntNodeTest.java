package com.samsa.node.out;

import org.junit.jupiter.api.*;

import com.samsa.core.InPort;
import com.samsa.core.Message;
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
        String[] topics = {"application/#", "123"};
        mqttInNode = new MqttInNode("tcp://192.168.70.203:1883", "1235", topics); // 노드부터 만들었을 때
        OutPort outPort = new OutPort(mqttInNode);
        Pipe pipe = new Pipe();
        outPort.addPipe(pipe);
        mqttInNode.setOutPort(outPort);

        pipe.offer(new Message("dddddddddd2"));
        pipe.offer(new Message("dddddddddd12"));
        pipe.offer(new Message("dddddddddd123"));

        mqttOutNode = new MqttOutNode("tcp://192.168.70.203:1883", "12356", "app");
        InPort inPort = new InPort(mqttOutNode);
        inPort.addPipe(pipe);
        mqttOutNode.setInPort(inPort);
    }

    // @Test
    // void constructorTest() {
    //     String[] topics = {"application/#", "123"};
    //     Assertions.assertThrows(NullPointerException.class, () -> new MqttInNode("tcp://192.168.70.203:1883", null, topics));

    //     Assertions.assertThrows(NullPointerException.class, () -> new MqttInNode("123", null, topics));
    // }

    @Test
    void start() {
        mqttInNode.start();

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Assertions.assertDoesNotThrow(() -> mqttInNode.start());
    }

    @Test
    void start2() {
        mqttOutNode.start();

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Assertions.assertDoesNotThrow(() -> mqttInNode.start());
    }
}
