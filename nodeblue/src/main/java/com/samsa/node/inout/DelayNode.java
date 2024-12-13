package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class DelayNode extends InOutNode {

    private long delayTime; // 메시지를 지연시킬 시간 (밀리초 단위)
    private final Queue<Message> queue; // 메시지를 보관하는 큐

    private final Timer timer; // 비동기 작업을 수행하기 위한 타이머



    /**
     * DelayNode 생성자입니다.
     *
     * @param id 노드의 고유 식별자
     * @param delayTime 메시지를 지연시킬 시간 (밀리초 단위)
     */
    public DelayNode(String id, long delayTime) {
        super(id);
        this.delayTime = delayTime;
        this.queue = new LinkedList<>();
        this.timer = new Timer(true); // 데몬 스레드로 타이머 실행
    }

    /**
     * 메시지를 큐에 추가하고 지연 처리를 시작합니다.
     *
     * @param message 처리할 메시지
     */
    @Override
    public void onMessage(Message message) {
        synchronized (queue) {
            queue.add(message);
            log.info("DelayNode[{}]: Received message {}", getId(), message.getPayload());
        }
        startDelay();
    }

    /**
     * 설정된 지연 시간 이후에 메시지를 처리하는 작업을 예약합니다.
     */
    private void startDelay() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                processNextMessage();
            }
        }, delayTime);
    }

    /**
     * 큐에서 메시지를 꺼내 처리합니다. 큐가 비어 있으면 아무 작업도 수행하지 않습니다.
     */
    private void processNextMessage() {
        Message message;
        synchronized (queue) {
            if (queue.isEmpty())
                return;
            message = queue.poll();
        }
        log.info("DelayNode[{}]: Processing message {}", getId(), message.getPayload());
        emit(message);
    }

    /**
     * 큐에 있는 모든 메시지를 즉시 처리합니다.
     */
    public void flush() {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                Message message = queue.poll();
                log.info("DelayNode[{}]: Flushing message {}", getId(), message.getPayload());
                emit(message);
            }
        }
    }

    /**
     * 큐를 초기화하여 모든 대기 중인 메시지를 제거합니다.
     */
    public void reset() {
        synchronized (queue) {
            queue.clear();
            log.info("DelayNode[{}]: Queue reset", getId());
        }
    }

    /**
     * 현재 설정된 지연 시간을 반환합니다.
     *
     * @return 지연 시간 (밀리초 단위)
     */
    public long getDelayTime() {
        return delayTime;
    }

    /**
     * 지연 시간을 설정합니다.
     *
     * @param delayTime 새로 설정할 지연 시간 (밀리초 단위)
     */
    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    // Getter Queue
    public Queue<Message> getQueue() {
        return queue;
    }
}
