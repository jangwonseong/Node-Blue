package com.samsa.core.port;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import com.samsa.core.Message;
import com.samsa.core.Pipe;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OutPort {
    private UUID id;
    private List<Pipe> pipes;

    public OutPort() {
        this(UUID.randomUUID());
    }

    public OutPort(UUID id) {
        this.id = id;
        pipes = new CopyOnWriteArrayList<>();
    }

    public void propagate(Message message) {
        if (Objects.isNull(message)) {
            log.error("전파할 메시지가 null입니다. OutPortId: {}", id);
            throw new IllegalArgumentException("Message cannot be null");
        }

        for (Pipe pipe : pipes) {
            if (pipe == null)
                continue;
            try {
                pipe.offer(message);
                log.debug("파이프로 메시지 전송 완료. PipeId: {}, pipe size: {}", pipe.getId(), pipe.size());
            } catch (Exception e) {
                log.error("파이프로 메시지 전송 실패. PipeId: {}", pipe.getId(), e);
            }
        }
    }

    public boolean canAcceptData() {
        return pipes.stream().anyMatch(pipe -> Objects.nonNull(pipe) && !pipe.isFull());
    }

    public void addPipe(Pipe pipe) {
        if (Objects.isNull(pipe)) {
            throw new IllegalArgumentException("Pipe cannot be null");
        }
        pipes.add(pipe);
    }

    public void removePipe(Pipe pipe) {
        if (Objects.isNull(pipe)) {
            throw new IllegalArgumentException("Pipe cannot be null");
        }
        pipes.remove(pipe);
    }

    public UUID getId() {
        return id;
    }

    public List<Pipe> getPipes() {
        return pipes;
    }
}
