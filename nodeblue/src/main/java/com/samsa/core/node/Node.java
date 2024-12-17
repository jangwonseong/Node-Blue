package com.samsa.core.node;

import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Node implements Runnable {
    public enum NodeStatus {
        CREATED, RUNNING, STOPPED, ERROR
    }

    private UUID id;
    private NodeStatus status = NodeStatus.CREATED;

    protected Node() {
        this(UUID.randomUUID());
    }

    protected Node(UUID id) {
        if (Objects.isNull(id)) {
            log.error("Node ID cannot be null");
            throw new IllegalArgumentException("Node ID cannot be null");
        }
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        if (Objects.isNull(id)) {
            log.error("Node ID cannot be null");
            throw new IllegalArgumentException("Node ID cannot be null");
        }
        this.id = id;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        if (Objects.isNull(status)) {
            log.error("Node Status cannot be null");
            throw new IllegalArgumentException("Node Status cannot be null");
        }
        this.status = status;
    }
}
