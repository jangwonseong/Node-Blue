package com.samsa.node.inout;

import com.samsa.core.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link RangeNode} 클래스의 동작을 검증하기 위한 테스트 클래스입니다.
 * <p>
 * 입력 범위와 출력 범위 간의 매핑, 예외 상황, 경계값 동작 등을 테스트합니다.
 * </p>
 *
 * @see RangeNode
 * @see Message
 */
class RangeNodeTest {
    private RangeNode rangeNode;
    private static final double INPUT_MIN = 0.0;
    private static final double INPUT_MAX = 100.0;
    private static final double OUTPUT_MIN = 0.0;
    private static final double OUTPUT_MAX = 1.0;
    private static final double DELTA = 0.001;

    /**
     * 각 테스트 실행 전에 {@link RangeNode} 인스턴스를 초기화합니다.
     */
    @BeforeEach
    void setUp() {
        rangeNode = new RangeNode(INPUT_MIN, INPUT_MAX, OUTPUT_MIN, OUTPUT_MAX, true);
    }

    /**
     * 입력 또는 출력 범위가 잘못 설정된 경우 {@link IllegalArgumentException}이 발생하는지 검증합니다.
     */
    @Test
    void testConstructorWithInvalidRange() {
        assertThrows(IllegalArgumentException.class, () -> 
            new RangeNode(100.0, 0.0, 0.0, 1.0, true));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new RangeNode(0.0, 100.0, 1.0, 0.0, true));
    }

    /**
     * 유효한 입력 값이 지정된 범위에 매핑되는지 확인합니다.
     */
    @Test
    void testValidMapping() {
        Message message = new Message(50.0);
        rangeNode.onMessage(message);
        assertEquals(0.5, (Double) message.getPayload(), DELTA);
    }

    /**
     * 입력 값이 범위를 벗어난 경우, 제한된(constrained) 매핑이 적용되는지 검증합니다.
     */
    @Test
    void testConstrainedMapping() {
        Message overMax = new Message(150.0);
        rangeNode.onMessage(overMax);
        assertEquals(1.0, (Double) overMax.getPayload(), DELTA);

        Message underMin = new Message(-50.0);
        rangeNode.onMessage(underMin);
        assertEquals(0.0, (Double) underMin.getPayload(), DELTA);
    }

    /**
     * 제한되지 않은(unconstrained) 매핑이 적용되었을 때 입력 값이 그대로 출력 범위로 변환되는지 검증합니다.
     */
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

    /**
     * 잘못된 페이로드(숫자가 아닌 값)가 주어졌을 때 페이로드가 변경되지 않는지 확인합니다.
     */
    @Test
    void testInvalidPayload() {
        Message stringMessage = new Message("invalid");
        rangeNode.onMessage(stringMessage);
        // 페이로드가 변경되지 않아야 함
        assertEquals("invalid", stringMessage.getPayload());
    }

    /**
     * {@code null} 메시지가 전달되었을 때 예외가 발생하지 않는지 검증합니다.
     */
    @Test
    void testNullMessage() {
        assertDoesNotThrow(() -> rangeNode.onMessage(null));
    }

    /**
     * 입력 값이 최소 및 최대 경계값에 위치했을 때 출력이 올바르게 매핑되는지 검증합니다.
     */
    @Test
    void testBoundaryValues() {
        Message minMessage = new Message(INPUT_MIN);
        rangeNode.onMessage(minMessage);
        assertEquals(OUTPUT_MIN, (Double) minMessage.getPayload(), DELTA);

        Message maxMessage = new Message(INPUT_MAX);
        rangeNode.onMessage(maxMessage);
        assertEquals(OUTPUT_MAX, (Double) maxMessage.getPayload(), DELTA);
    }

    /**
     * setter 메서드의 유효성 검사를 검증합니다.
     * <p>
     * 입력 또는 출력의 최대값이 최소값보다 작아질 경우 {@link IllegalArgumentException}이 발생해야 합니다.
     * </p>
     */
    @Test
    void testSetterValidation() {
        assertThrows(IllegalArgumentException.class, () -> 
            rangeNode.setInputMax(INPUT_MIN - 1));
        
        assertThrows(IllegalArgumentException.class, () -> 
            rangeNode.setOutputMax(OUTPUT_MIN - 1));
    }
}
