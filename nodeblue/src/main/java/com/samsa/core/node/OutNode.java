package com.samsa.core.node;

import java.util.UUID;

import com.samsa.core.Message;
import com.samsa.core.port.OutPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class OutNode extends Node {
    private final OutPort port;

    protected OutNode() {
        this(UUID.randomUUID());
    }

    protected OutNode(UUID id) {
        super(id);
        port = new OutPort();
    }

    protected void emit(Message message) {
        if (port == null) {
            log.error("출력 포트가 초기화되지 않았습니다. NodeId: {}", getId());
            throw new IllegalStateException("출력 포트가 초기화되지 않았습니다");
        }
        if (message == null) {
            log.error("전송할 메시지가 null입니다. NodeId: {}", getId());
            throw new IllegalArgumentException("메시지는 null일 수 없습니다");
        }

        try {
            log.debug("메시지 전송 시작. NodeId: {}, MessageId: {}", getId(), message.getId());
            port.propagate(message);
            log.debug("메시지 전송 완료. NodeId: {}, MessageId: {}", getId(), message.getId());
        } catch (Exception e) {
            log.error("메시지 전송 중 오류 발생. NodeId: {}, MessageId: {}", getId(), message.getId(), e);
            throw new RuntimeException("메시지 전송 중 오류가 발생했습니다", e);
        }
    }

    // 변경사항...
    protected abstract Message createMessage();

    @Override
    public void run() {
        while (true) {
            try {
                Message message = createMessage(); // 메시지 생성 로직을 자식 클래스에 위임
                log.info("메시지 출력!!! @@@ !!!!");
                emit(message);
            } catch (Exception e) {
                log.error("run 실행 중 오류 발생. NodeId: {}", getId(), e);
            }
        }
    }

    public OutPort getPort() {
        return port;
    }
}
