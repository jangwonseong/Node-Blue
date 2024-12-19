package com.samsa.core.node;

import java.util.UUID;
import com.samsa.core.Message;
import com.samsa.core.port.InPort;
import com.samsa.core.port.OutPort;
import lombok.extern.slf4j.Slf4j;

/**
 * 입력 및 출력 노드를 나타내는 추상 클래스. 이 클래스는 메시지를 수신하고 처리한 후 출력으로 전달하는 동작을 정의합니다.
 */
@Slf4j
public abstract class InOutNode extends Node {

    /**
     * 입력 메시지를 받을 포트.
     */
    private final InPort inPort;

    /**
     * 처리된 메시지를 전송할 포트.
     */
    private final OutPort outPort;

    /**
     * 기본 생성자. 고유 식별자를 자동으로 생성합니다.
     */
    protected InOutNode() {
        this(UUID.randomUUID());
    }

    /**
     * 고유 식별자를 지정하여 노드를 생성합니다.
     * 
     * @param id 고유 식별자
     */
    protected InOutNode(UUID id) {
        super(id);
        this.inPort = new InPort();
        this.outPort = new OutPort();
    }

    /**
     * 포트에서 메시지를 소비합니다.
     * 
     * @return 소비된 메시지
     * @throws IllegalStateException 포트가 초기화되지 않은 경우 발생
     */
    private Message receive() {
        return inPort.consume();
    }

    /**
     * 메시지를 출력 포트를 통해 전송합니다.
     * 
     * @param message 전송할 메시지
     * @throws IllegalStateException 출력 포트가 초기화되지 않은 경우 발생
     * @throws IllegalArgumentException 메시지가 null인 경우 발생
     */
    protected void emit(Message message) {
        if (message == null) {
            log.error("전송할 메시지가 null입니다. NodeId: {}", getId());
            throw new IllegalArgumentException("메시지는 null일 수 없습니다.");
        }

        try {
            log.debug("메시지 전송 시작. NodeId: {}, MessageId: {}", getId(), message.getId());
            outPort.propagate(message);
            log.debug("메시지 전송 완료. NodeId: {}, MessageId: {}", getId(), message.getId());
        } catch (Exception e) {
            log.error("메시지 전송 중 오류 발생. NodeId: {}, MessageId: {}", getId(), message.getId(), e);
            throw new RuntimeException("메시지 전송 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 메시지를 처리하기 위한 메서드. 기본 동작은 메시지를 출력 포트로 전달하는 것입니다. 하위 클래스에서 이 메서드를 오버라이드하여 추가적인 처리 로직을 구현할 수
     * 있습니다.
     * 
     * @param message 처리할 메시지
     */
    protected void onMessage(Message message) {
        emit(message);
    }

    /**
     * 노드를 실행하여 지속적으로 메시지를 처리합니다. 예외가 발생해도 실행은 중단되지 않습니다.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                log.info("노드 실행 시작. NodeId: {}", getId());

                // 1. 메시지 수신
                Message inputMessage = receive();
                log.debug("메시지 수신 완료. NodeId: {}, MessageId: {}", getId(), inputMessage.getId());

                // 2. 메시지 처리
                onMessage(inputMessage);

                log.info("노드 실행 완료. NodeId: {}", getId());
            } catch (Exception e) {
                log.error("노드 실행 중 오류 발생. NodeId: {}", getId(), e);
            }
        }
    }

    /**
     * 입력 포트를 반환합니다.
     * 
     * @return 입력 포트
     */
    public InPort getInPort() {
        return inPort;
    }

    /**
     * 출력 포트를 반환합니다.
     * 
     * @return 출력 포트
     */
    public OutPort getOutPort() {
        return outPort;
    }
}
