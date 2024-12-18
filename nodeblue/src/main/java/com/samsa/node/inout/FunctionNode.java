package com.samsa.node.inout;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;
import com.samsa.annotation.NodeType;
import lombok.extern.slf4j.Slf4j;



/**
 * {@code FunctionNode} 클래스는 사용자 정의 로직을 처리할 수 있도록 함수형 인터페이스 기반 메시지 핸들러를 제공하는 노드입니다.
 */
@NodeType("FunctionNode")
@Slf4j
public class FunctionNode extends InOutNode {

    /**
     * {@code MessageHandler}는 메시지를 처리하기 위한 함수형 인터페이스입니다.
     */
    @FunctionalInterface
    public interface MessageHandler {
        /**
         * 메시지를 처리하는 메서드입니다.
         *
         * @param message 처리할 메시지 객체
         */
        void handle(Message message);
    }

    private final MessageHandler messageHandler;

    /**
     * Jackson 역직렬화를 위한 기본 생성자
     */
    @JsonCreator
    public FunctionNode() {
        super();
        this.messageHandler = message -> {
            log.info("기본 메시지 핸들러: {}", message.getPayload());
        };
    }

    /**
     * 커스텀 메시지 핸들러를 사용하는 생성자
     * 새로운 {@code FunctionNode} 인스턴스를 생성합니다.
     *
     * @param messageHandler 메시지를 처리할 사용자 정의 핸들러
     */
    public FunctionNode(MessageHandler messageHandler) {
        super();
        this.messageHandler = messageHandler != null ? messageHandler : 
            message -> log.info("기본 메시지 핸들러: {}", message.getPayload());
    }

    /**
     * 메시지를 수신하고 사용자 정의 핸들러를 실행한 후 다음 노드로 메시지를 전달합니다.
     *
     * @param message 처리할 메시지 객체
     */
    @Override
    protected void onMessage(Message message) {
        if (message == null) {
            log.warn("Null 메시지를 받았습니다");
            return;
        }
        try {
            log.info("메시지 ID: {} - 처리 시작", message.getId());
            messageHandler.handle(message); // 함수형 인터페이스 호출
        } catch (Exception e) {
            log.error("FunctionNode에서 메시지 처리 중 오류 발생. NodeId: {}, MessageId: {}", getId(),
                    message.getId(), e);
        }
        log.info("메시지 ID: {} - 처리 완료", message.getId());
        emit(message); // 원본 또는 수정된 메시지를 다음 노드로 전달
    }
}
