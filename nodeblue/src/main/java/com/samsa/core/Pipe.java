package com.samsa.core;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Pipe 클래스는 노드 간의 메시지 전달을 위한 통신 채널을 제공합니다.
 * 스레드 안전한 메시지 큐를 구현하여 노드 간 비동기 통신을 지원합니다.
 * 
 * <p>
 * 주요 특징:
 * </p>
 * <ul>
 * <li>블로킹 큐를 사용한 스레드 안전 보장</li>
 * <li>고유 ID를 통한 파이프 식별</li>
 * <li>동기화된 메시지 전송 및 수신</li>
 * <li>용량 제한을 통한 백프레셔 지원</li>
 * </ul>
 *
 * @author samsa
 * @version 1.0
 */
public class Pipe {
    private final UUID id;
    private final BlockingQueue<Message> queue;
    private static final int DEFAULT_CAPACITY = 1024;

    /**
     * 기본 용량(1)으로 새로운 파이프를 생성합니다.
     */
    public Pipe() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * 지정된 용량으로 새로운 파이프를 생성합니다.
     *
     * @param capacity 파이프의 최대 메시지 수용 용량
     */
    public Pipe(int capacity) {
        this.id = UUID.randomUUID();
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    /**
     * 메시지를 파이프에 추가합니다. 파이프가 가득 찬 경우 공간이 생길 때까지 대기합니다.
     *
     * @param message 전송할 메시지
     * @return 메시지 추가 성공 여부
     * @throws IllegalArgumentException message가 null인 경우
     */
    public synchronized boolean offer(Message message) {
        if (Objects.isNull(message)) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        while (isFull()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        notifyAll();
        return queue.offer(message);
    }

    public synchronized Message poll() {
        while (isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        notifyAll();
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean isFull() {
        return queue.remainingCapacity() == 0;
    }

    public int size() {
        return queue.size();
    }

    public void clear() {
        queue.clear();
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("Pipe[id=%s, size=%d, capacity=%d]", id, queue.size(),
                queue.size() + queue.remainingCapacity());
    }
}
