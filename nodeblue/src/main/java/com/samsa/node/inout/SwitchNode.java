package com.samsa.node.inout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;

import lombok.extern.slf4j.Slf4j;

/**
 * {@code SwitchNode} 클래스는 메시지에 대해 정의된 규칙들을 적용하여 해당 규칙에 맞는 처리를 하는 노드입니다.
 * 
 * <p>
 * 메시지가 주어진 조건에 맞는 규칙에 의해 처리되며, 규칙이 일치할 경우 메시지를 클론하여 처리합니다. {@code stopOnFirstMatch}가 {@code true}인
 * 경우, 첫 번째 규칙이 일치하면 이후 규칙을 평가하지 않습니다.
 * </p>
 */
@Slf4j
public class SwitchNode extends InOutNode {

    private final List<Rule> rules;
    private final boolean stopOnFirstMatch;

    /**
     * {@code SwitchNode} 객체를 생성합니다. 규칙은 빈 목록으로 초기화됩니다.
     *
     * @param stopOnFirstMatch 첫 번째 규칙이 일치하면 이후 규칙을 평가하지 않을지 여부.
     */
    public SwitchNode(boolean stopOnFirstMatch) {
        this(UUID.randomUUID(), stopOnFirstMatch);
    }

    /**
     * {@code SwitchNode} 객체를 생성합니다. 규칙은 빈 목록으로 초기화됩니다.
     *
     * @param id 노드의 고유 식별자(UUID).
     * @param stopOnFirstMatch 첫 번째 규칙이 일치하면 이후 규칙을 평가하지 않을지 여부.
     */
    public SwitchNode(UUID id, boolean stopOnFirstMatch) {
        super(id);
        this.rules = new ArrayList<>();
        this.stopOnFirstMatch = stopOnFirstMatch;
    }

    /**
     * 메시지를 처리하는 메서드로, 규칙을 순차적으로 평가하여 메시지를 처리합니다.
     * 
     * @param message 처리할 메시지; {@code null}일 수 없습니다.
     */
    @Override
    public void onMessage(Message message) {
        if (message == null) {
            log.error("메시지가 null입니다. NodeId: {}", getId());
            return;
        }

        boolean matched = false;
        try {
            for (Rule rule : rules) {
                if (rule.evaluate(message)) {
                    matched = true;
                    Message clonedMessage = cloneMessage(message);
                    log.debug("규칙 일치. NodeId: {}, MessageId: {}, RuleId: {}", getId(),
                            message.getId(), rule.getId());

                    super.onMessage(clonedMessage);

                    if (stopOnFirstMatch) {
                        break;
                    }
                }
            }

            if (!matched) {
                log.debug("일치하는 규칙 없음. NodeId: {}, MessageId: {}", getId(), message.getId());
            }

        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생. NodeId: {}, MessageId: {}", getId(), message.getId(), e);
        }
    }

    /**
     * 새로운 규칙을 추가합니다.
     *
     * @param rule 추가할 규칙; {@code null}일 수 없습니다.
     * @throws IllegalArgumentException 규칙이 {@code null}일 경우 예외가 발생합니다.
     */
    public void addRule(Rule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("규칙은 null일 수 없습니다");
        }
        rules.add(rule);
    }

    /**
     * 메시지를 복제하여 새로운 {@code Message} 객체를 반환합니다.
     * 
     * @param original 원본 메시지.
     * @return 복제된 메시지 객체.
     */
    private Message cloneMessage(Message original) {
        return new Message(original.getPayload());
    }

    /**
     * 규칙을 나타내는 클래스입니다. 규칙의 조건을 테스트할 수 있는 {@code Predicate}를 가지고 있습니다.
     */
    public static class Rule {
        private final UUID id;
        private final Predicate<Message> condition;

        /**
         * 주어진 조건을 만족하는 {@code Rule} 객체를 생성합니다.
         * 
         * @param condition 규칙에 해당하는 조건을 테스트하는 {@code Predicate} 객체.
         */
        public Rule(Predicate<Message> condition) {
            if (condition == null) {
                throw new IllegalArgumentException("조건은 null일 수 없습니다");
            }
            this.id = UUID.randomUUID();
            this.condition = condition;
        }

        /**
         * 메시지가 규칙의 조건을 만족하는지 평가합니다.
         * 
         * @param message 평가할 메시지.
         * @return 조건을 만족하면 {@code true}, 그렇지 않으면 {@code false}.
         */
        public boolean evaluate(Message message) {
            return condition.test(message);
        }

        /**
         * 규칙의 고유 식별자(UUID)를 반환합니다.
         * 
         * @return 규칙의 UUID.
         */
        public UUID getId() {
            return id;
        }
    }
}
