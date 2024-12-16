package com.samsa.node.inout;

import com.samsa.core.port.InPort;
import com.samsa.core.port.OutPort;
import com.samsa.core.Message;
import com.samsa.core.node.Node;
import com.samsa.node.inout.DelayNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DelayNodeTest {

    private Node dummyNode;
    private InPort inPort;
    private OutPort outPort;

    @BeforeEach
    void setUp() {
        dummyNode = new Node() {
            @Override
            public void onMessage(Message message) {
                // 테스트용 더미 메서드
            }
        };
        inPort = new InPort(dummyNode);
        outPort = new OutPort(dummyNode);
    }

    @Test
    void testNegativeDelayTime() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DelayNode(UUID.randomUUID(), inPort, outPort, -1000);
        });
    }

    @Test
    void testMessageDelay() throws InterruptedException {
        DelayNode delayNode = new DelayNode(UUID.randomUUID(), inPort, outPort, 500);

        long startTime = System.currentTimeMillis();
        Message message = new Message("테스트 메시지");
        delayNode.onMessage(message);
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertTrue(elapsedTime >= 500);
    }

    @Test
    void testNullMessage() {
        DelayNode delayNode = new DelayNode(UUID.randomUUID(), inPort, outPort, 100);
        assertDoesNotThrow(() -> delayNode.onMessage(null));
    }

    @Test
    void testGetDelayMillis() {
        long expectedDelay = 1000;
        DelayNode delayNode = new DelayNode(UUID.randomUUID(), inPort, outPort, expectedDelay);
        assertEquals(expectedDelay, delayNode.getDelayMillis());
    }

    @Test
    void testRunMethod() throws InterruptedException {
        DelayNode delayNode = new DelayNode(UUID.randomUUID(), inPort, outPort, 100);
        Thread thread = new Thread(delayNode);
        thread.start();

        // Add small delay to allow status change
        Thread.sleep(100);

        assertTrue(delayNode.getStatus() == Node.NodeStatus.RUNNING);

        delayNode.stop();
        thread.join(500);

        assertTrue(delayNode.getStatus() == Node.NodeStatus.STOPPED);
    }

}
