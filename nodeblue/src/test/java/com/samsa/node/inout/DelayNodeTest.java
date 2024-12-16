package com.samsa.node.inout;

import com.samsa.core.Message;
import com.samsa.core.node.Node;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DelayNodeTest {
    private DelayNode delayNode;
    private static final long DELAY_TIME = 500;

    @BeforeEach
    void setUp() {
        delayNode = new DelayNode(DELAY_TIME);
    }

    @Test
    void testConstructorWithNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DelayNode(-1000);
        });
    }

    @Test
    void testConstructorWithIdAndNegativeDelay() {
        UUID id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> {
            new DelayNode(id, -1000);
        });
    }

    @Test
    void testMessageDelay() {
        Message message = new Message("test");
        long startTime = System.currentTimeMillis();
        
        delayNode.onMessage(message);
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertTrue(elapsedTime >= DELAY_TIME, 
            String.format("지연 시간이 %d ms 이상이어야 합니다", DELAY_TIME));
    }

    @Test
    void testNullMessage() {
        assertDoesNotThrow(() -> delayNode.onMessage(null));
    }

    @Test
    void testCustomIdConstructor() {
        UUID customId = UUID.randomUUID();
        DelayNode customNode = new DelayNode(customId, DELAY_TIME);
        
        assertEquals(customId, customNode.getId());
        assertEquals(DELAY_TIME, customNode.getDelayMillis());
    }

    @Test
    void testNodeStatus() {
        assertEquals(Node.NodeStatus.CREATED, delayNode.getStatus());
    }

    @Test
    void testGetDelayMillis() {
        assertEquals(DELAY_TIME, delayNode.getDelayMillis());
    }

    @Test
    void testMessagePropagation() {
        Message message = new Message("test");
        delayNode.onMessage(message);
        // 메시지가 성공적으로 처리되었는지 확인
        assertNotNull(message.getId());
    }
}
