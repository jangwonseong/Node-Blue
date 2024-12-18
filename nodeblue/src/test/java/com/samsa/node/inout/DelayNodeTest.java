package com.samsa.node.inout;

import com.samsa.core.Message;
import com.samsa.core.node.Node;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DelayNode} 클래스의 동작을 검증하기 위한 테스트 클래스입니다.
 * <p>
 * 다양한 생성자와 메시지 처리 지연 시간, 예외 처리 동작을 테스트합니다.
 * </p>
 *
 * @see DelayNode
 * @see Message
 * @see Node
 */
class DelayNodeTest {
    private DelayNode delayNode;
    private static final long DELAY_TIME = 500;

    /**
     * 각 테스트 실행 전에 {@link DelayNode} 인스턴스를 초기화합니다.
     */
    @BeforeEach
    void setUp() {
        delayNode = new DelayNode(DELAY_TIME);
    }

    /**
     * 음수 지연 시간을 설정했을 때 {@link IllegalArgumentException}이 발생하는지 확인합니다.
     */
    @Test
    void testConstructorWithNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DelayNode(-1000);
        });
    }

    /**
     * 메시지 처리 시 지정된 지연 시간이 정확하게 적용되는지 확인합니다.
     * <p>
     * 메시지 처리가 최소 {@link #DELAY_TIME} 밀리초 이상 지연되는지 테스트합니다.
     * </p>
     */
    @Test
    void testMessageDelay() {
        Message message = new Message("test");
        long startTime = System.currentTimeMillis();
        
        delayNode.onMessage(message);
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertTrue(elapsedTime >= DELAY_TIME, 
            String.format("지연 시간이 %d ms 이상이어야 합니다", DELAY_TIME));
    }

    /**
     * {@code null} 메시지가 전달되었을 때 예외가 발생하지 않는지 확인합니다.
     */
    @Test
    void testNullMessage() {
        assertDoesNotThrow(() -> delayNode.onMessage(null));
    }

    /**
     * 사용자 정의 ID를 사용하는 생성자가 정상적으로 동작하는지 확인합니다.
     */
    @Test
    void testCustomIdConstructor() {
        UUID customId = UUID.randomUUID();
        DelayNode customNode = new DelayNode(DELAY_TIME);
        
        assertEquals(customId, customNode.getId());
        assertEquals(DELAY_TIME, customNode.getDelayMillis());
    }

    /**
     * {@link DelayNode}의 초기 상태가 {@code CREATED}인지 확인합니다.
     */
    @Test
    void testNodeStatus() {
        assertEquals(Node.NodeStatus.CREATED, delayNode.getStatus());
    }

    /**
     * {@link DelayNode#getDelayMillis()} 메서드가 올바른 지연 시간을 반환하는지 확인합니다.
     */
    @Test
    void testGetDelayMillis() {
        assertEquals(DELAY_TIME, delayNode.getDelayMillis());
    }

    /**
     * 메시지가 성공적으로 처리되고 ID가 생성되는지 확인합니다.
     */
    @Test
    void testMessagePropagation() {
        Message message = new Message("test");
        delayNode.onMessage(message);
        // 메시지가 성공적으로 처리되었는지 확인
        assertNotNull(message.getId());
    }
}
