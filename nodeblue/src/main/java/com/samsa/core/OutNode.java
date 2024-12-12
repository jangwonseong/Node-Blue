package com.samsa.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class OutNode extends Node {
    private final List<Pipe> outputPipes = new ArrayList<>();

    protected OutNode(UUID id) {
        this.id = id;
    }

    public void emit(Message message) {
    for (Pipe pipe : getPipes()) {
        if (pipe.isConnected()) {
            pipe.send(message);
        }
    }
}

    // 외부 입력이 주목적이므로 onMessage는 비활성화
    @Override
    public final void onMessage(Message message) {
        throw new UnsupportedOperationException("Output node cannot receive messages");
    }

    public void addPipe(Pipe pipe) {
        outputPipes.add(pipe);
    }

    public void removePipe(Pipe pipe) {
        outputPipes.remove(pipe);
    }

    protected List<Pipe> getPipes() {
        return Collections.unmodifiableList(outputPipes);
    }
}
