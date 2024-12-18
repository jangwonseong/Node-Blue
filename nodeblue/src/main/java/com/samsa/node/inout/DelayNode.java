package com.samsa.node.inout;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;
import com.samsa.annotation.NodeType;

import lombok.extern.slf4j.Slf4j;
/**
 * {@code DelayNode} 클래스는 메시지를 처리할 때 지연을 도입하는 노드입니다.
 * 이 클래스는 {@link InOutNode}를 상속하며 밀리초 단위의 지연 시간을 설정할 수 있습니다.
 *
 * <p>지연 시간은 음수가 될 수 없으며, 디버깅을 위해 지연 처리 과정을 로깅합니다.</p>
 * 
 * <p>이 클래스는 주로 메시지 처리 타이밍을 제어해야 하는 시스템에서 사용됩니다.
 * 예를 들어, 시뮬레이션 또는 처리 속도가 제한된 메시지 파이프라인에서 활용됩니다.</p>
 */
@NodeType("DelayNode")
@Slf4j
public class DelayNode extends InOutNode {
    private final long delayMillis;

    /**
     * Jackson 역직렬화를 위한 생성자
     */
    @JsonCreator
    public DelayNode(@JsonProperty("delay") long delayMillis) {
        super();
        validateDelay(delayMillis);
        this.delayMillis = delayMillis;
    }

    /**
     * 지정된 UUID와 지연 시간을 사용하여 DelayNode를 생성합니다.
     */
    public DelayNode(UUID id, long delayMillis) {
        super(id);
        validateDelay(delayMillis);
        this.delayMillis = delayMillis;
    }

    private void validateDelay(long delayMillis) {
        if (delayMillis < 0) {
            log.error("지연 시간이 음수입니다: {} ms", delayMillis);
            throw new IllegalArgumentException("지연 시간은 음수일 수 없습니다");
        }
    }
    /**
     * 메시지를 처리할 때 지연을 추가합니다. 이 메소드는 메시지를 부모 노드의 {@code onMessage} 메소드로 전달하기 전에
     * 설정된 지연 시간을 적용합니다.
     *
     * @param message 처리할 메시지; {@code null}일 수 없습니다.
     *
     * <p>메시지 처리 과정을 로깅하며, 지연 중 인터럽트를 처리합니다.</p>
     */
    @Override
    protected void onMessage(Message message) {
        if (message == null) {
            log.error("메시지가 null입니다. NodeId: {}", getId());
            return;
        }

        try {
            log.debug("메시지 지연 시작. NodeId: {}, MessageId: {}, Delay: {}ms", 
                getId(), message.getId(), delayMillis);

            TimeUnit.MILLISECONDS.sleep(delayMillis);

            log.debug("메시지 지연 완료. NodeId: {}, MessageId: {}", 
                getId(), message.getId());

            super.onMessage(message);

        } catch (InterruptedException e) {
            log.error("지연 처리 중 인터럽트 발생. NodeId: {}, MessageId: {}", 
                getId(), message.getId(), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 지연 시간을 밀리초 단위로 반환합니다.
     *
     * @return 밀리초 단위의 지연 시간.
     */
    public long getDelayMillis() {
        return delayMillis;
    }
}
