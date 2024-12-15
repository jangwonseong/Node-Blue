package com.samsa.node.inout;

import com.samsa.core.InPort;
import com.samsa.core.OutPort;
import com.samsa.core.Pipe;
import com.samsa.core.Message;
import com.samsa.core.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RangeNodeTest {
    private RangeNode rangeNode;
    private InPort inPort;
    private OutPort outPort;
    private Node dummyNode;
    private Pipe pipe;

    @BeforeEach
    void setUp() {
        // 더미 노드 생성
        dummyNode = new Node() {
            @Override
            public void onMessage(Message message) {
                // 테스트용 더미 메서드
            }
        };

        // 포트 생성
        inPort = new InPort(dummyNode);
        outPort = new OutPort(dummyNode);

        // 파이프 생성 및 연결
        pipe = new Pipe();
        outPort.addPipe(pipe);

        // RangeNode 생성
        rangeNode = new RangeNode(UUID.randomUUID(), inPort, outPort, 0, 100, 0, 1, true);
    }

    /**
     * 입력 값이 입력 범위 내에 있을 때의 매핑을 테스트합니다.
     */
    @Test
    void Range_Mapping_Test() {
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
    void Range_Limit_Test() {
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
    void Limitless_Test() {
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
    void Wrong_Payload_Test() {
        rangeNode.onMessage(new Message("invalid"));
        Message result = outPort.getPipes().get(0).poll();
        assertNull(result);
    }

    /**
     * 입력 값이 범위 경계값일 때를 테스트합니다.
     */
    @Test
    void Limit_Value_Test() {
        rangeNode.onMessage(new Message(0.0));
        Message result1 = outPort.getPipes().get(0).poll();
        assertEquals(0.0, (Double) result1.getPayload(), 0.001);

        rangeNode.onMessage(new Message(100.0));
        Message result2 = outPort.getPipes().get(0).poll();
        assertEquals(1.0, (Double) result2.getPayload(), 0.001);
    }
}
