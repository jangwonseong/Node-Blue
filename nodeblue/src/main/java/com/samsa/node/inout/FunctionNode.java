package com.samsa.node.inout;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;
import com.samsa.annotation.NodeType;

import lombok.extern.slf4j.Slf4j;

@NodeType("FunctionNode")
@Slf4j
public class FunctionNode extends InOutNode {

    @FunctionalInterface
    public interface MessageHandler {
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
     */
    public FunctionNode(MessageHandler messageHandler) {
        super();
        this.messageHandler = messageHandler != null ? messageHandler : 
            message -> log.info("기본 메시지 핸들러: {}", message.getPayload());
    }

    @Override
    protected void onMessage(Message message) {
        if (message == null) {
            log.warn("Null 메시지를 받았습니다");
            return;
        }
        
        try {
            messageHandler.handle(message);
            emit(message);
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
