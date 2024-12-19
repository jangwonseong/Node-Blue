package com.samsa.core;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import lombok.extern.slf4j.Slf4j;

/**
 * {@code Pipe} 클래스는 노드 간의 메시지 전달을 위한 통신 채널을 제공합니다. 스레드 안전한 메시지 큐를 구현하여 노드 간 비동기 통신을 지원합니다.
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
 * @version 1.1
 */
@Slf4j
public class Pipe {

    /** 파이프의 고유 식별자 */
    private final UUID id;

    /** 메시지를 저장하는 블로킹 큐 */
    private final BlockingQueue<Message> queue;

    /** 기본 파이프 용량 */
    private static final int DEFAULT_CAPACITY = 1024;

    /**
     * 기본 용량(1024)으로 새로운 파이프를 생성합니다.
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
        log.debug("새로운 Pipe가 생성되었습니다. ID: {}, 용량: {}", id, capacity);
    }

    /**
     * 메시지를 파이프에 추가합니다. 파이프가 가득 찬 경우 공간이 생길 때까지 대기합니다.
     *
     * @param message 전송할 메시지
     * @return 메시지 추가 성공 여부
     * @throws IllegalArgumentException message가 null일 경우 예외 발생
     */
    public synchronized boolean offer(Message message) {
        if (Objects.isNull(message)) {
            log.error("전송하려는 메시지가 null입니다.");
            throw new IllegalArgumentException("Message cannot be null");
        }

        // 큐가 가득 찬 경우 대기
        while (isFull()) {
            try {
                log.debug("파이프가 가득 찼습니다. 공간이 생길 때까지 대기 중... 파이프 ID: {}", id);
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("스레드가 인터럽트되었습니다. 파이프 ID: {}", id, e);
            }
        }

        boolean result = queue.offer(message);
        if (result) {
            log.debug("메시지가 파이프에 추가되었습니다. 파이프 ID: {}, 메시지 ID: {}", id, message.getId());
        }
        notifyAll();
        return result;
    }

    /**
     * 메시지를 파이프에서 가져옵니다. 파이프가 비어있을 경우 데이터가 들어올 때까지 대기합니다.
     *
     * @return 수신된 메시지
     */
    public synchronized Message poll() {
        // 큐가 비어있는 경우 대기
        while (isEmpty()) {
            try {
                log.debug("파이프가 비어 있습니다. 메시지가 들어올 때까지 대기 중... 파이프 ID: {}", id);
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("스레드가 인터럽트되었습니다. 파이프 ID: {}", id, e);
            }
        }

        Message message = queue.poll();
        if (message != null) {
            log.debug("메시지가 파이프에서 수신되었습니다. 파이프 ID: {}, 메시지 ID: {}", id, message.getId());
        }
        notifyAll();
        return message;
    }

    /**
     * 파이프가 비어 있는지 확인합니다.
     *
     * @return 파이프가 비어 있으면 true, 아니면 false
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * 파이프가 가득 찼는지 확인합니다.
     *
     * @return 파이프가 가득 차면 true, 아니면 false
     */
    public boolean isFull() {
        return queue.remainingCapacity() == 0;
    }

    /**
     * 파이프에 현재 저장된 메시지 수를 반환합니다.
     *
     * @return 현재 메시지 수
     */
    public int size() {
        return queue.size();
    }

    /**
     * 파이프에 저장된 모든 메시지를 제거합니다.
     */
    public void clear() {
        queue.clear();
        log.debug("파이프가 비워졌습니다. 파이프 ID: {}", id);
    }

    /**
     * 파이프의 고유 ID를 반환합니다.
     *
     * @return 파이프 ID
     */
    public UUID getId() {
        return id;
    }

    /**
     * 파이프의 상태를 문자열로 반환합니다.
     *
     * @return 파이프 상태 정보
     */
    @Override
    public String toString() {
        return String.format("Pipe[id=%s, size=%d, capacity=%d]", id, queue.size(),
                queue.size() + queue.remainingCapacity());
    }
}
