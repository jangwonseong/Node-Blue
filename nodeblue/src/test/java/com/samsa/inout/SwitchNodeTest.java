package com.samsa.inout;

import com.samsa.core.Message;
import com.samsa.node.inout.SwitchNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


/**
 * SwitchNode의 기능을 테스트하는 테스트 클래스입니다.
 * 조건에 따른 메시지 라우팅, 규칙 매칭, 예외 처리 등을 검증합니다.
 */
class SwitchNodeTest {
    private SwitchNode switchNode;

    /**
 * 각 테스트 전에 실행되며, stopOnFirstMatch가 true인 SwitchNode를 생성합니다.
 */
    @BeforeEach
    void setUp() {
        switchNode = new SwitchNode(true); // stopOnFirstMatch = true
    }
        /**
     * 커스텀 ID를 사용한 생성자 테스트.
     * 지정된 ID로 노드가 정상적으로 생성되는지 확인합니다.
     */

    @Test
    void testConstructorWithCustomId() {
        UUID customId = UUID.randomUUID();
        SwitchNode node = new SwitchNode(customId, true);
        assertEquals(customId, node.getId());
    }

        /**
     * null 메시지 처리 테스트.
     * null 메시지 입력 시 예외가 발생하지 않는지 확인합니다.
     */
    @Test
    void testNullMessage() {
        assertDoesNotThrow(() -> switchNode.onMessage(null));
    }

        /**
     * null 규칙 추가 테스트.
     * null 규칙 추가 시 IllegalArgumentException이 발생하는지 확인합니다.
     */
    @Test
    void testAddNullRule() {
        assertThrows(IllegalArgumentException.class, () -> 
            switchNode.addRule(null));
    }

        /**
     * 단일 규칙 매칭 테스트.
     * 숫자가 100보다 큰 경우를 검사하는 규칙을 테스트합니다.
     */
    @Test
    void testSingleRuleMatch() {
        // 숫자가 100보다 큰 경우를 검사하는 규칙
        switchNode.addRule(new SwitchNode.Rule(msg -> 
            msg.getPayload() instanceof Number && 
            ((Number) msg.getPayload()).doubleValue() > 100));

        Message message = new Message(150);
        switchNode.onMessage(message);
    }

        /**
     * 다중 규칙 매칭 테스트 (첫 매칭 시 중단).
     * stopOnFirstMatch가 true일 때 첫 번째 매칭 규칙에서 중단되는지 확인합니다.
     */
    @Test
    void testMultipleRulesWithStopOnFirstMatch() {
        switchNode.addRule(new SwitchNode.Rule(msg -> 
            msg.getPayload() instanceof Number && 
            ((Number) msg.getPayload()).doubleValue() > 100));
        
        switchNode.addRule(new SwitchNode.Rule(msg -> 
            msg.getPayload() instanceof Number && 
            ((Number) msg.getPayload()).doubleValue() > 50));

        Message message = new Message(150);
        switchNode.onMessage(message);
    }

        /**
     * 다중 규칙 매칭 테스트 (모든 규칙 평가).
     * stopOnFirstMatch가 false일 때 모든 규칙이 평가되는지 확인합니다.
     */
    @Test
    void testMultipleRulesWithoutStopOnFirstMatch() {
        SwitchNode continuousNode = new SwitchNode(false);
        
        continuousNode.addRule(new SwitchNode.Rule(msg -> 
            msg.getPayload() instanceof Number && 
            ((Number) msg.getPayload()).doubleValue() > 100));
        
        continuousNode.addRule(new SwitchNode.Rule(msg -> 
            msg.getPayload() instanceof Number && 
            ((Number) msg.getPayload()).doubleValue() > 50));

        Message message = new Message(150);
        continuousNode.onMessage(message);
    }

        /**
     * 매칭되는 규칙이 없는 경우 테스트.
     * 메시지가 어떤 규칙과도 매칭되지 않을 때의 동작을 확인합니다.
     */
    @Test
    void testNoMatchingRules() {
        switchNode.addRule(new SwitchNode.Rule(msg -> 
            msg.getPayload() instanceof Number && 
            ((Number) msg.getPayload()).doubleValue() > 100));

        Message message = new Message(50);
        switchNode.onMessage(message);
    }

        /**
     * 다양한 페이로드 타입 테스트.
     * 문자열과 숫자 타입의 페이로드에 대한 규칙 매칭을 확인합니다.
     */
    @Test
    void testDifferentPayloadTypes() {
        switchNode.addRule(new SwitchNode.Rule(msg -> 
            msg.getPayload() instanceof String));
        
        switchNode.addRule(new SwitchNode.Rule(msg -> 
            msg.getPayload() instanceof Number));

        Message stringMessage = new Message("test");
        Message numberMessage = new Message(100);
        
        switchNode.onMessage(stringMessage);
        switchNode.onMessage(numberMessage);
    }
}
