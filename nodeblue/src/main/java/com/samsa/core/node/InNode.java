package com.samsa.core.node;

import java.util.UUID;
import com.samsa.core.Message;
import com.samsa.core.port.InPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class InNode extends Node {
    private final InPort port;

    protected InNode() {
        this(UUID.randomUUID());
    }

    protected InNode(UUID id) {
        super(id);
        port = new InPort();
    }

    private Message receive() {
        if (port == null) {
            log.error("입력 포트가 초기화되지 않았습니다. NodeId: {}", getId());
            throw new IllegalStateException("입력 포트가 초기화되지 않았습니다");
        }

        return port.consume();
    }

    protected abstract void onMessage(Message message);

    @Override
    public void run() {
        while (true) {
            try {
                Message message = receive(); // 파이프에 있는 메세지를 꺼낸다.
                onMessage(message);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("run 실행 중 오류 발생. NodeId: {}", getId(), e);
            }
        }
    }

    public InPort getPort() {
        return port;
    }
}
