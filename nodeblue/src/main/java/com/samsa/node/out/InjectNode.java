package com.samsa.node.out;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samsa.annotation.NodeType;
import com.samsa.core.Message;
import com.samsa.core.node.OutNode;
import lombok.extern.slf4j.Slf4j;

/**
 * {@code InjectNode} 클래스는 지정된 페이로드를 사용하여 메시지를 생성하고 외부로 전송하는 노드입니다.
 *
 * <p>
 * 이 노드는 주어진 페이로드를 포함하는 메시지를 생성하고, {@link OutNode}의 {@code createMessage} 메서드를 통해 메시지를 반환합니다.
 * </p>
 */
@NodeType("InjectNode")
@Slf4j
public class InjectNode extends OutNode {

    private final Object payload;

    /**
     * 지정된 페이로드를 사용하여 {@code InjectNode}를 생성합니다.
     *
     * @param payload 페이로드; {@code null}일 수 없습니다.
     * @throws IllegalArgumentException 페이로드가 {@code null}일 경우 예외가 발생합니다.
     */
    @JsonCreator
    public InjectNode(@JsonProperty("payload") Object payload) {
        if (payload == null) {
            throw new IllegalArgumentException("페이로드는 null일 수 없습니다.");
        }
        this.payload = payload;
    }

    /**
     * 지정된 페이로드를 포함하는 {@code Message} 객체를 생성합니다.
     *
     * @return 생성된 {@code Message} 객체.
     */
    @Override
    protected Message createMessage() {
        log.debug("InjectNode가 메시지를 생성합니다. Payload: {}", payload);
        return new Message(payload);
    }

    // Getter
    public Object getPayload() {
        return payload;
    }
}
