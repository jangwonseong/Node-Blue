package com.samsa.core.port;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.samsa.core.Message;
import com.samsa.core.Pipe;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InPort {
    private UUID id;
    private List<Pipe> pipes;

    public InPort() {
        this(UUID.randomUUID());
    }

    public InPort(UUID id) {
        this.id = id;
        pipes = new ArrayList<>();
    }

    public Message consume() {
        log.info("메세지 생김");
        for (Pipe pipe : pipes) {
            if (Objects.isNull(pipe)) {
                continue;
            }
            Message message = pipe.poll();
            if (Objects.nonNull(message)) {
                return message;
            }
        }

        return null;
    }

    public boolean hasAvailableData() {
        return pipes.stream().anyMatch(pipe -> Objects.nonNull(pipe) && !pipe.isEmpty());
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
