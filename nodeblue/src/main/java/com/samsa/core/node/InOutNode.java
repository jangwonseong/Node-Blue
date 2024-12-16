package com.samsa.core.node;

import java.util.UUID;
import com.samsa.core.Message;
import com.samsa.core.port.InPort;
import com.samsa.core.port.OutPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class InOutNode extends Node {
    private final InPort inPort;
    private final OutPort outPort;

    protected InOutNode() {
        this(UUID.randomUUID());
    }

    protected InOutNode(UUID id) {
        super(id);
        inPort = new InPort();
        outPort = new OutPort();
    }

    private Message receive() {
        if (inPort == null) {
            log.error("입력 포트가 초기화되지 않았습니다. NodeId: {}", getId());
            throw new IllegalStateException("입력 포트가 초기화되지 않았습니다");
        }

        return inPort.consume();
    }

    private void emit(Message message) {
        if (outPort == null) {
            log.error("출력 포트가 초기화되지 않았습니다. NodeId: {}", getId());
            throw new IllegalStateException("출력 포트가 초기화되지 않았습니다");
        }
        if (message == null) {
            log.error("전송할 메시지가 null입니다. NodeId: {}", getId());
            throw new IllegalArgumentException("메시지는 null일 수 없습니다");
        }

        try {
            log.debug("메시지 전송 시작. NodeId: {}, MessageId: {}", getId(), message.getId());
            outPort.propagate(message);
            log.debug("메시지 전송 완료. NodeId: {}, MessageId: {}", getId(), message.getId());
        } catch (Exception e) {
            log.error("메시지 전송 중 오류 발생. NodeId: {}, MessageId: {}", getId(), message.getId(), e);
            throw new RuntimeException("메시지 전송 중 오류가 발생했습니다", e);
        }
    }

    protected void onMessage(Message message) {
        emit(message);
    };

    @Override
    public void run() {
        while (true) {
            try {
                log.info("노드 실행 시작. NodeId: {}", getId());

                // 1. 메시지 수신
                Message inputMessage = receive();
                log.debug("메시지 수신 완료. NodeId: {}, MessageId: {}", getId(), inputMessage.getId());

                // 2. 메시지 처리
                onMessage(inputMessage);

                log.info("노드 실행 완료. NodeId: {}", getId());

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("노드 실행 중 오류 발생. NodeId: {}", getId(), e);
            }
        }
    }

    public InPort getInPort() {
        return inPort;
    }

    public OutPort getOutPort() {
        return outPort;
    }
}
