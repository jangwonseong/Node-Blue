package com.samsa.node.inout;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DelayNode extends InOutNode {
    private final long delayMillis;

    public DelayNode(long delayMillis) {
        this(UUID.randomUUID(), delayMillis);
    }

    public DelayNode(UUID id, long delayMillis) {
        super(id);
        if (delayMillis < 0) {
            log.error("지연 시간이 음수입니다: {} ms", delayMillis);
            throw new IllegalArgumentException("지연 시간은 음수일 수 없습니다");
        }
        this.delayMillis = delayMillis;
    }

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

    public long getDelayMillis() {
        return delayMillis;
    }
}
