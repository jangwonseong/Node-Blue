package com.samsa.core.node;

import java.util.UUID;

import com.samsa.core.Message;
import com.samsa.core.port.InPort;

import lombok.extern.slf4j.Slf4j;

/**
 * 입력 노드를 나타내는 추상 클래스. 이 클래스는 메시지를 소비하고 처리하는 동작을 정의합니다.
 */
@Slf4j
public abstract class InNode extends Node {

    /**
     * 입력 메시지를 받을 포트.
     */
    private final InPort port;

    /**
     * 기본 생성자. 고유 식별자를 자동으로 생성합니다.
     */
    protected InNode() {
        this(UUID.randomUUID());
    }

    /**
     * 고유 식별자를 지정하여 노드를 생성합니다.
     * 
     * @param id 고유 식별자
     */
    protected InNode(UUID id) {
        super(id);
        this.port = new InPort();
    }

    /**
     * 포트에서 메시지를 소비합니다.
     * 
     * @return 소비된 메시지
     * @throws IllegalStateException 포트가 초기화되지 않은 경우 발생
     */
    private Message receive() {
        if (port == null) {
            log.error("입력 포트가 초기화되지 않았습니다. NodeId: {}", getId());
            throw new IllegalStateException("입력 포트가 초기화되지 않았습니다.");
        }

        return port.consume();
    }

    /**
     * 메시지를 처리하기 위한 추상 메서드. 하위 클래스에서 구체적인 메시지 처리 로직을 구현해야 합니다.
     * 
     * @param message 처리할 메시지
     */
    protected abstract void onMessage(Message message);

    /**
     * 노드를 실행하여 지속적으로 메시지를 처리합니다. 예외가 발생해도 노드 실행은 중단되지 않습니다.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message message = receive();
                onMessage(message);
            } catch (Exception e) {
                log.error("run 실행 중 오류 발생. NodeId: {}", getId(), e);
            }
        }
    }

    /**
     * 입력 포트를 반환합니다.
     * 
     * @return 입력 포트
     */
    public InPort getPort() {
        return port;
    }
}
