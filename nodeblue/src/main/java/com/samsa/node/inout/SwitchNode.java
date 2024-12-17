package com.samsa.node.inout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SwitchNode extends InOutNode {
    private final List<Rule> rules;
    private final boolean stopOnFirstMatch;

    public SwitchNode(boolean stopOnFirstMatch) {
        this.rules = new ArrayList<>();
        this.stopOnFirstMatch = stopOnFirstMatch;
    }

    public SwitchNode(UUID id, boolean stopOnFirstMatch) {
        super(id);
        this.rules = new ArrayList<>();
        this.stopOnFirstMatch = stopOnFirstMatch;
    }

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            log.error("메시지가 null입니다. NodeId: {}", getId());
            return;
        }

        try {
            boolean matched = false;
            for (Rule rule : rules) {
                if (rule.evaluate(message)) {
                    matched = true;
                    Message clonedMessage = new Message(message.getPayload());
                    log.debug("규칙 일치. NodeId: {}, MessageId: {}, RuleId: {}", 
                        getId(), message.getId(), rule.getId());
                    super.onMessage(clonedMessage);
                    
                    if (stopOnFirstMatch) {
                        break;
                    }
                }
            }

            if (!matched) {
                log.debug("일치하는 규칙 없음. NodeId: {}, MessageId: {}", 
                    getId(), message.getId());
            }
            
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생. NodeId: {}, MessageId: {}", 
                getId(), message.getId(), e);
        }
    }

    public void addRule(Rule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("규칙은 null일 수 없습니다");
        }
        rules.add(rule);
    }

    private Message cloneMessage(Message original) {
        return new Message(original.getPayload());
    }

    public static class Rule {
        private final UUID id;
        private final Predicate<Message> condition;

        public Rule(Predicate<Message> condition) {
            this.id = UUID.randomUUID();
            this.condition = condition;
        }

        public boolean evaluate(Message message) {
            return condition.test(message);
        }

        public UUID getId() {
            return id;
        }
    }
}
