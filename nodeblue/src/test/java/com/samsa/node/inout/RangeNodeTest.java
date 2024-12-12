package com.samsa.node.inout;

import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RangeNodeTest {

    private RangeNode rangeNode;

    @BeforeEach
    void setUp() {
        // 테스트용 RangeNode 생성
        rangeNode = new RangeNode("testNode", 0, 100, 0, 1, true);
    }

    @Test
    void testMappingWithinRange() {
        // 입력 값이 입력 범위 내에 있을 때
        Double result = rangeNode.processMessage(new Message(50));
        assertEquals(0.5, result);
    }

    @Test
    void testMappingWithConstrain() {
        // 입력 값이 입력 범위를 벗어났을 때 제한 동작 검증
        Double result1 = rangeNode.processMessage(new Message(150));
        assertEquals(1.0, result1);

        Double result2 = rangeNode.processMessage(new Message(-50));
        assertEquals(0.0, result2);
    }

    @Test
    void testMappingWithoutConstrain() {
        // 제한 없이 동작할 때 검증
        rangeNode.setConstrainToTarget(false);
        Double result1 = rangeNode.processMessage(new Message(150));
        assertEquals(1.5, result1);

        Double result2 = rangeNode.processMessage(new Message(-50));
        assertEquals(-0.5, result2);
    }

    @Test
    void testInvalidPayload() {
        // 잘못된 타입의 payload 처리
        Double result = rangeNode.processMessage(new Message("invalid"));
        assertNull(result);
    }

    @Test
    void testEdgeCases() {
        // 입력 값이 범위 경계값일 때
        Double result1 = rangeNode.processMessage(new Message(0));
        assertEquals(0.0, result1);

        Double result2 = rangeNode.processMessage(new Message(100));
        assertEquals(1.0, result2);
    }
}
