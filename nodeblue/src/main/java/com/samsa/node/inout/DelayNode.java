package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.InPort;
import com.samsa.core.OutPort;
import com.samsa.core.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 일정 시간 메시지 처리를 지연시키는 노드입니다. 이 노드는 입력으로 받은 메시지를 지정된 시간 동안 지연시킨 후 출력으로 전달합니다. 주로 시뮬레이션, 프로세스 제어, 또는
 * 네트워크 지연 모방 등에 사용될 수 있습니다.
 */
@Slf4j
public class DelayNode extends InOutNode {
    /** 메시지 처리를 지연시킬 시간 (밀리초) */
    private final long delayMillis;

    /**
     * DelayNode를 생성합니다.
     *
     * @param id 노드의 고유 ID
     * @param inPort 입력 포트. 메시지를 받아들이는 데 사용됩니다.
     * @param outPort 출력 포트. 지연 후 메시지를 전달하는 데 사용됩니다.
     * @param delayMillis 메시지 처리 지연 시간 (밀리초)
     * @throws IllegalArgumentException 지연 시간이 음수이거나 포트가 null인 경우
     */
    public DelayNode(UUID id, InPort inPort, OutPort outPort, long delayMillis) {
        super(id);
        setInPort(inPort);
        setOutPort(outPort);
        if (delayMillis < 0) {
            log.error("지연 시간이 음수입니다: {} ms", delayMillis);
            throw new IllegalArgumentException("Delay time cannot be negative");
        }
        this.delayMillis = delayMillis;
        log.info("DelayNode 생성됨. ID: {}, Delay: {} ms", id, delayMillis);
    }



    /**
     * 지정된 ID 없이 DelayNode를 생성합니다. UUID는 자동으로 생성됩니다.
     *
     * @param inPort 입력 포트
     * @param outPort 출력 포트
     * @param delayMillis 메시지 처리 지연 시간 (밀리초)
     */
    public DelayNode(InPort inPort, OutPort outPort, long delayMillis) {
        this(UUID.randomUUID(), inPort, outPort, delayMillis);
    }


    /**
     * 메시지를 처리하고 지정된 시간만큼 지연시킨 후 전파합니다. 이 메서드는 메시지를 받아 지정된 시간 동안 대기한 후, 출력 포트로 전달합니다.
     *
     * @param message 처리할 메시지
     */
    @Override
    public void onMessage(Message message) {
        if (Objects.isNull(message)) {
            log.warn("Null 메시지를 받았습니다. NodeId: {}", getId());
            return;
        }

        try {
            log.debug("메시지 지연 처리 시작. NodeId: {}, MessageId: {}", getId(), message.getId());
            TimeUnit.MILLISECONDS.sleep(delayMillis);
            emit(message);
            log.debug("메시지 지연 처리 완료. NodeId: {}, MessageId: {}", getId(), message.getId());
        } catch (InterruptedException e) {
            log.error("지연 처리 중 인터럽트 발생. NodeId: {}, MessageId: {}", getId(), message.getId(), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 노드의 처리 로직을 실행합니다. 이 메서드는 노드가 시작되면 호출되며, 노드가 중지될 때까지 계속해서 메시지를 처리합니다.
     */
    @Override
    public void run() {
        start();
        while (getStatus() == NodeStatus.RUNNING) {
            Message message = receive();
            if (message != null) {
                onMessage(message);
            }
        }
        stop();
    }

    /**
     * 설정된 지연 시간을 반환합니다.
     *
     * @return 지연 시간 (밀리초)
     */
    public long getDelayMillis() {
        return delayMillis;
    }

    /**
     * 현재 노드의 상태를 반환합니다. 노드의 상태는 CREATED, RUNNING, STOPPED, ERROR 중 하나일 수 있습니다.
     *
     * @return 노드의 현재 상태
     */
    public NodeStatus getStatus() {
        return status;
    }
}
