package com.samsa.node.inout;

import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;

public class FunctionNode extends InOutNode {

    @FunctionalInterface
    public interface MessageHandler {
        void handle(Message message);
    }

    private final MessageHandler messageHandler;

    public FunctionNode(MessageHandler messageHandler) {
        super();
        this.messageHandler = messageHandler;
    }

    @Override
    protected void onMessage(Message message) {
        messageHandler.handle(message);
        emit(message);
    }
}
