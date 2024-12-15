package com.samsa.node.inout;

import com.samsa.core.InPort;
import com.samsa.core.OutPort;
import com.samsa.core.Message;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DelayNode의 기본적인 기능을 테스트하는 클래스입니다.
 */
class DelayNodeTest {

    /**
     * 지연 시간이 음수일 때 예외가 발생하는지 테스트합니다.
     */
    @Test
    void Minus_Delay_Test() {
        InPort inPort = new InPort(null);
        OutPort outPort = new OutPort(null);

        assertThrows(IllegalArgumentException.class, () -> {
            new DelayNode(UUID.randomUUID(), inPort, outPort, -1000);
        });
    }

    /**
     * 메시지가 실제로 지연되는지 테스트합니다.
     */
    @Test
    void Message_Delay_Test() throws InterruptedException {
        // 테스트를 위한 DelayNode 생성 (500ms 지연)
        InPort inPort = new InPort(null);
        OutPort outPort = new OutPort(null);
        DelayNode delayNode = new DelayNode(UUID.randomUUID(), inPort, outPort, 500);

        // 시작 시간 기록
        long startTime = System.currentTimeMillis();

        // 메시지 전송
        Message message = new Message("테스트 메시지");
        delayNode.onMessage(message);

        // 경과 시간 확인
        long elapsedTime = System.currentTimeMillis() - startTime;

        // 지연 시간(500ms)보다 경과 시간이 큰지 확인
        assertTrue(elapsedTime >= 500);
    }

    /**
     * null 메시지 처리를 테스트합니다.
     */
    @Test
    void Null_Message_Test() {
        InPort inPort = new InPort(null);
        OutPort outPort = new OutPort(null);
        DelayNode delayNode = new DelayNode(UUID.randomUUID(), inPort, outPort, 100);

        // null 메시지 전송 시 예외가 발생하지 않아야 함
        assertDoesNotThrow(() -> delayNode.onMessage(null));
    }
}
