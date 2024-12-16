package com.samsa.node.inout;

import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RangeNodeTest {
    private RangeNode rangeNode;
    private static final double INPUT_MIN = 0.0;
    private static final double INPUT_MAX = 100.0;
    private static final double OUTPUT_MIN = 0.0;
    private static final double OUTPUT_MAX = 1.0;
    private static final double DELTA = 0.001;

    @BeforeEach
    void setUp() {
        rangeNode = new RangeNode(INPUT_MIN, INPUT_MAX, OUTPUT_MIN, OUTPUT_MAX, true);
    }

    @Test
    void testConstructorWithInvalidRange() {
        assertThrows(IllegalArgumentException.class, () -> 
            new RangeNode(100.0, 0.0, 0.0, 1.0, true));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new RangeNode(0.0, 100.0, 1.0, 0.0, true));
    }

    @Test
    void testValidMapping() {
        Message message = new Message(50.0);
        rangeNode.onMessage(message);
        assertEquals(0.5, (Double) message.getPayload(), DELTA);
    }

    @Test
    void testConstrainedMapping() {
        Message overMax = new Message(150.0);
        rangeNode.onMessage(overMax);
        assertEquals(1.0, (Double) overMax.getPayload(), DELTA);

        Message underMin = new Message(-50.0);
        rangeNode.onMessage(underMin);
        assertEquals(0.0, (Double) underMin.getPayload(), DELTA);
    }

    @Test
    void testUnconstrainedMapping() {
        rangeNode = new RangeNode(INPUT_MIN, INPUT_MAX, OUTPUT_MIN, OUTPUT_MAX, false);
        
        Message overMax = new Message(150.0);
        rangeNode.onMessage(overMax);
        assertEquals(1.5, (Double) overMax.getPayload(), DELTA);

        Message underMin = new Message(-50.0);
        rangeNode.onMessage(underMin);
        assertEquals(-0.5, (Double) underMin.getPayload(), DELTA);
    }

    @Test
    void testInvalidPayload() {
        Message stringMessage = new Message("invalid");
        rangeNode.onMessage(stringMessage);
        // 페이로드가 변경되지 않아야 함
        assertEquals("invalid", stringMessage.getPayload());
    }

    @Test
    void testNullMessage() {
        assertDoesNotThrow(() -> rangeNode.onMessage(null));
    }

    @Test
    void testBoundaryValues() {
        Message minMessage = new Message(INPUT_MIN);
        rangeNode.onMessage(minMessage);
        assertEquals(OUTPUT_MIN, (Double) minMessage.getPayload(), DELTA);

        Message maxMessage = new Message(INPUT_MAX);
        rangeNode.onMessage(maxMessage);
        assertEquals(OUTPUT_MAX, (Double) maxMessage.getPayload(), DELTA);
    }

    @Test
    void testSetterValidation() {
        assertThrows(IllegalArgumentException.class, () -> 
            rangeNode.setInputMax(INPUT_MIN - 1));
        
        assertThrows(IllegalArgumentException.class, () -> 
            rangeNode.setOutputMax(OUTPUT_MIN - 1));
    }
}
