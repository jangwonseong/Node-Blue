package com.samsa.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OutPort {
    private UUID id;
    private Node owner;
    private List<Pipe> pipes;

    public OutPort(Node node) {
        this(UUID.randomUUID(), node);
    }

    public OutPort(UUID id, Node node) {
        if (Objects.isNull(node)) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        this.id = id;
        this.owner = node;
        this.pipes = new ArrayList<>();
    }
     public void propagate(Message message) {
        if (Objects.isNull(message)) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        
        pipes.forEach(pipe -> pipe.offer(message));  
    }

    /**
     * 연결된 파이프들이 데이터를 수용할 수 있는지 확인합니다.
     */
    public boolean canAcceptData() {
        return pipes.stream().anyMatch(pipe -> !pipe.isFull());  
    }
    public void addPipe(Pipe pipe) {
        if (Objects.isNull(pipe)) {
            throw new IllegalArgumentException("Pipe cannot be null");
        }
        pipes.add(pipe);
    }

    public void removePipe(Pipe pipe) {
        if (Objects.nonNull(pipe)) {
            pipes.remove(pipe);
        }
    }
}