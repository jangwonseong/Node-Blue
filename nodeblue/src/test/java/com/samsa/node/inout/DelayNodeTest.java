package com.samsa.node.inout;

import com.samsa.core.Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DelayNodeTest {
    @Test
    void testMessageDelay() throws InterruptedException {
        DelayNode delayNode = new DelayNode("delayNode1", 1000);
        Message message = new Message("Delayed Message");

        // 메시지를 추가하고 지연 시작
        delayNode.onMessage(message);

        // 바로 처리되지 않음 확인
        Thread.sleep(500);
        assertTrue(delayNode.getQueue().contains(message));

        // 지연 이후 메시지 처리 확인
        Thread.sleep(600);
        assertFalse(delayNode.getQueue().contains(message));
    }

    @Test
    void testFlushMessages() {
        DelayNode delayNode = new DelayNode("delayNode2", 1000);
        delayNode.onMessage(new Message("Message 1"));
        delayNode.onMessage(new Message("Message 2"));

        // 메시지 강제 플러시
        delayNode.flush();
        assertTrue(delayNode.getQueue().isEmpty());
    }

    @Test
    void testResetQueue() {
        DelayNode delayNode = new DelayNode("delayNode3", 1000);
        delayNode.onMessage(new Message("Message to reset"));

        // 큐 초기화
        delayNode.reset();
        assertTrue(delayNode.getQueue().isEmpty());
    }
}
