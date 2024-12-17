package com.samsa.inout;

import com.samsa.core.Message;
import com.samsa.node.inout.SwitchNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SwitchNodeTest {
    private SwitchNode switchNode;

    @BeforeEach
    void setUp() {
        switchNode = new SwitchNode(true); // stopOnFirstMatch = true
    }

    @Test
    void testConstructorWithCustomId() {
        UUID customId = UUID.randomUUID();
        SwitchNode node = new SwitchNode(customId, true);
        assertEquals(customId, node.getId());
    }

    @Test
    void testNullMessage() {
        assertDoesNotThrow(() -> switchNode.onMessage(null));
    }

    @Test
    void testAddNullRule() {
        assertThrows(IllegalArgumentException.class, () -> 
            switchNode.addRule(null));
    }

    @Test
    void testSingleRuleMatch() {
        // 숫자가 100보다 큰 경우를 검사하는 규칙
        switchNode.addRule(new SwitchNode.Rule(msg -> 
            msg.getPayload() instanceof Number && 
            ((Number) msg.getPayload()).doubleValue() > 100));

        Message message = new Message(150);
        switchNode.onMessage(message);
    }

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

    @Test
    void testNoMatchingRules() {
        switchNode.addRule(new SwitchNode.Rule(msg -> 
            msg.getPayload() instanceof Number && 
            ((Number) msg.getPayload()).doubleValue() > 100));

        Message message = new Message(50);
        switchNode.onMessage(message);
    }

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
