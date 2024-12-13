package com.samsa.node.inout;

import com.samsa.core.InPort;
import com.samsa.core.OutPort;
import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RangeNode의 기능을 테스트하는 클래스입니다.
 */
class RangeNodeTest {

    private RangeNode rangeNode;
    private InPort inPort;
    private OutPort outPort;

    @BeforeEach
    void setUp() {
        // 테스트용 RangeNode 생성
        inPort = new InPort(null);
        outPort = new OutPort(null);
        rangeNode = new RangeNode(UUID.randomUUID(), inPort, outPort, 0, 100, 0, 1, true);
    }

    /**
     * 입력 값이 입력 범위 내에 있을 때의 매핑을 테스트합니다.
     */
    @Test
    void 범위내_매핑_테스트() {
        Message inputMessage = new Message(50.0);
        rangeNode.onMessage(inputMessage);

        Message outputMessage = outPort.getPipes().get(0).poll();
        assertNotNull(outputMessage);
        assertEquals(0.5, (Double) outputMessage.getPayload(), 0.001);
    }

    /**
     * 입력 값이 범위를 벗어났을 때 제한 동작을 검증합니다.
     */
    @Test
    void 범위제한_테스트() {
        rangeNode.onMessage(new Message(150.0));
        Message result1 = outPort.getPipes().get(0).poll();
        assertEquals(1.0, (Double) result1.getPayload(), 0.001);

        rangeNode.onMessage(new Message(-50.0));
        Message result2 = outPort.getPipes().get(0).poll();
        assertEquals(0.0, (Double) result2.getPayload(), 0.001);
    }

    /**
     * 제한 없이 동작할 때를 검증합니다.
     */
    @Test
    void 제한없는_매핑_테스트() {
        rangeNode = new RangeNode(UUID.randomUUID(), inPort, outPort, 0, 100, 0, 1, false);

        rangeNode.onMessage(new Message(150.0));
        Message result1 = outPort.getPipes().get(0).poll();
        assertEquals(1.5, (Double) result1.getPayload(), 0.001);

        rangeNode.onMessage(new Message(-50.0));
        Message result2 = outPort.getPipes().get(0).poll();
        assertEquals(-0.5, (Double) result2.getPayload(), 0.001);
    }

    /**
     * 잘못된 타입의 payload 처리를 테스트합니다.
     */
    @Test
    void 잘못된_페이로드_테스트() {
        rangeNode.onMessage(new Message("invalid"));
        Message result = outPort.getPipes().get(0).poll();
        assertNull(result);
    }

    /**
     * 입력 값이 범위 경계값일 때를 테스트합니다.
     */
    @Test
    void 경계값_테스트() {
        rangeNode.onMessage(new Message(0.0));
        Message result1 = outPort.getPipes().get(0).poll();
        assertEquals(0.0, (Double) result1.getPayload(), 0.001);

        rangeNode.onMessage(new Message(100.0));
        Message result2 = outPort.getPipes().get(0).poll();
        assertEquals(1.0, (Double) result2.getPayload(), 0.001);
    }
}
