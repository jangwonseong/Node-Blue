package com.samsa.core;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import lombok.extern.slf4j.Slf4j;

/**
 * 노드 간의 메시지 전달을 위한 파이프를 구현합니다.
 * FIFO(First In First Out) 방식의 메시지 큐를 사용합니다.
 */
@Slf4j
public class Pipe {
    /** 파이프의 고유 식별자 */
    private final UUID id;
    /** 메시지를 저장하는 큐 */
    private final BlockingQueue<Message> queue;
    /** 기본 큐 용량 */
    private static final int DEFAULT_CAPACITY = 1024;

    /**
     * 기본 용량의 파이프를 생성합니다.
     */
    public Pipe() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * 지정된 용량의 파이프를 생성합니다.
     *
     * @param capacity 파이프의 최대 용량
     * @throws IllegalArgumentException 용량이 0 이하인 경우
     */
    public Pipe(int capacity) {
        if (capacity <= 0) {
            log.error("파이프 용량은 0보다 커야 합니다: {}", capacity);
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.id = UUID.randomUUID();
        this.queue = new ArrayBlockingQueue<>(capacity);
        log.debug("파이프 생성됨. ID: {}, 용량: {}", id, capacity);
    }

    /**
     * 메시지를 파이프에 넣습니다. 큐가 가득 찬 경우 false를 반환합니다.
     */
    public boolean offer(Message message) {
        if (Objects.isNull(message)) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        return queue.offer(message);
    }

    /**
     * 파이프에서 메시지를 가져옵니다. 큐가 비어있는 경우 null을 반환합니다.
     */
    public Message poll() {
        return queue.poll();
    }

    /**
     * 파이프가 비어있는지 확인합니다.
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * 파이프가 가득 찼는지 확인합니다.
     */
    public boolean isFull() {
        return queue.remainingCapacity() == 0;
    }

    /**
     * 현재 파이프에 있는 메시지 수를 반환합니다.
     */
    public int size() {
        return queue.size();
    }

    /**
     * 파이프의 고유 식별자를 반환합니다.
     */
    public UUID getId() {
        return id;
    }

    /**
     * 파이프를 비웁니다.
     */
    public void clear() {
        queue.clear();
    }

    /**
     * 현재 파이프의 상태 정보를 문자열로 반환합니다.
     */
    @Override
    public String toString() {
        return String.format("Pipe[id=%s, size=%d, capacity=%d]",
                id, queue.size(), queue.size() + queue.remainingCapacity());
    }
}