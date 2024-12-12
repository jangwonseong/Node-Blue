package com.samsa.node.inout;

import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * DelayNode 클래스에 대한 단위 테스트를 수행합니다. 이 테스트는 JUnit 5와 Mockito를 사용하여 DelayNode의 주요 동작을 검증합니다.
 */
class DelayNodeTest {

    private DelayNode delayNode;
    private static final long TEST_DELAY = 500; // 테스트에 사용할 딜레이 시간 (500ms)

    /**
     * 각 테스트 전에 실행되는 초기화 메서드입니다. DelayNode 인스턴스를 생성하여 테스트 환경을 설정합니다.
     */
    @BeforeEach
    void setUp() {
        delayNode = new DelayNode("testNode", TEST_DELAY);
    }

    /**
     * onMessage 메서드 테스트. 메시지가 정상적으로 큐에 추가되고 설정된 딜레이 시간 후에 emit 메서드가 호출되는지 확인합니다.
     *
     * @throws InterruptedException 테스트 중 스레드가 중단될 경우 발생
     */
    @Test
    void testOnMessage() throws InterruptedException {
        // DelayNode의 emit 메서드를 모킹하여 호출 여부를 확인
        DelayNode spyDelayNode = Mockito.spy(delayNode);

        // 테스트 메시지 생성
        Message message = new Message("Hello, world!");

        // onMessage 호출
        spyDelayNode.onMessage(message);

        // 딜레이 시간이 지나도록 대기
        Thread.sleep(TEST_DELAY + 100);

        // emit 메서드가 한 번 호출되었는지 확인
        verify(spyDelayNode, times(1)).emit(message);
    }

    /**
     * flush 메서드 테스트. 큐에 추가된 모든 메시지가 즉시 처리되고 emit 메서드가 호출되는지 확인합니다.
     */
    
    @Test
    void testFlush() {
        // DelayNode의 emit 메서드를 모킹
        DelayNode spyDelayNode = Mockito.spy(delayNode);

        // 여러 메시지 추가
        spyDelayNode.onMessage(new Message("Message 1"));
        spyDelayNode.onMessage(new Message("Message 2"));

        // flush 호출
        spyDelayNode.flush();

        // emit 메서드가 두 번 호출되었는지 확인
        verify(spyDelayNode, times(2)).emit(any(Message.class));
    }

    /**
     * reset 메서드 테스트. 큐를 초기화한 후 추가된 메시지가 모두 제거되었는지 확인합니다.
     */
    @Test
    void testReset() {
        // 여러 메시지 추가
        delayNode.onMessage(new Message("Message 1"));
        delayNode.onMessage(new Message("Message 2"));

        // reset 호출
        delayNode.reset();

        // 큐가 비워졌는지 확인
        DelayNode spyDelayNode = Mockito.spy(delayNode);
        spyDelayNode.flush();

        // emit 메서드가 호출되지 않았는지 확인
        verify(spyDelayNode, never()).emit(any(Message.class));
    }

    /**
     * setDelayTime 및 getDelayTime 메서드 테스트. 딜레이 시간을 설정하고 해당 값이 올바르게 반영되는지 확인합니다.
     */
    @Test
    void testSetAndGetDelayTime() {
        // 새로운 딜레이 시간 설정
        long newDelayTime = 1000;
        delayNode.setDelayTime(newDelayTime);

        // 설정된 값 확인
        assertEquals(newDelayTime, delayNode.getDelayTime());
    }
}
