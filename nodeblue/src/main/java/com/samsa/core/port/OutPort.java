package com.samsa.core.port;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import com.samsa.core.Message;
import com.samsa.core.Pipe;
import lombok.extern.slf4j.Slf4j;

/**
 * OutPort 클래스는 노드의 출력 포트를 구현합니다. 여러 파이프로 메시지를 전파하고 관리하는 기능을 제공합니다.
 * 
 * <p>
 * 주요 기능:
 * </p>
 * <ul>
 * <li>다중 출력 파이프 지원: 여러 대상으로 동시에 메시지 전파</li>
 * <li>메시지 전파 및 배포: 연결된 모든 파이프로 메시지 복제 전송</li>
 * <li>파이프 동적 추가/제거: 런타임에 출력 대상 변경 가능</li>
 * <li>데이터 수용 가능 여부 확인: 파이프의 메시지 수용 가능 상태 확인</li>
 * <li>스레드 안전한 구현: CopyOnWriteArrayList 사용으로 동시성 보장</li>
 * </ul>
 *
 * <p>
 * 사용 예시:
 * </p>
 * 
 * <pre>
 * OutPort port = new OutPort();
 * port.addPipe(targetPipe);
 * if (port.canAcceptData()) {
 *     port.propagate(message);
 * }
 * </pre>
 *
 * @author samsa
 * @version 1.0
 * @see Pipe
 * @see Message
 * @see CopyOnWriteArrayList
 */
@Slf4j
public class OutPort {
    /** 포트의 고유 식별자 */
    private final UUID id;

    /** 출력 파이프들의 목록 (스레드 안전) */
    private final List<Pipe> pipes;

    /**
     * 기본 생성자. 새로운 UUID로 포트를 생성합니다.
     */
    public OutPort() {
        this(UUID.randomUUID());
    }

    /**
     * 지정된 ID로 포트를 생성합니다.
     *
     * @param id 포트의 고유 식별자
     */
    public OutPort(UUID id) {
        this.id = Objects.requireNonNull(id, "ID는 null일 수 없습니다.");
        this.pipes = new CopyOnWriteArrayList<>();
    }

    /**
     * 메시지를 모든 연결된 파이프로 전파합니다. 각 파이프로의 전송은 독립적으로 처리되며, 실패 시 로깅됩니다.
     *
     * @param message 전파할 메시지
     * @throws IllegalArgumentException message가 null인 경우
     */
    public void propagate(Message message) {
        if (Objects.isNull(message)) {
            log.error("전파할 메시지가 null입니다. OutPortId: {}", id);
            throw new IllegalArgumentException("Message는 null일 수 없습니다.");
        }

        for (Pipe pipe : pipes) {
            if (pipe == null) {
                log.warn("null 파이프가 목록에 존재합니다. 이를 건너뜁니다.");
                continue;
            }
            try {
                pipe.offer(message);
                log.debug("파이프로 메시지 전송 완료. PipeId: {}, pipe size: {}", pipe.getId(), pipe.size());
            } catch (Exception e) {
                log.error("파이프로 메시지 전송 실패. PipeId: {}", pipe.getId(), e);
            }
        }
    }

    /**
     * 하나 이상의 파이프가 데이터를 수용할 수 있는지 확인합니다.
     *
     * @return 최소 하나의 파이프가 데이터를 수용할 수 있으면 true, 그렇지 않으면 false
     */
    public boolean canAcceptData() {
        boolean canAccept =
                pipes.stream().anyMatch(pipe -> Objects.nonNull(pipe) && !pipe.isFull());
        log.debug("데이터 수용 가능 여부: {}", canAccept);
        return canAccept;
    }

    /**
     * 새로운 출력 파이프를 추가합니다.
     *
     * @param pipe 추가할 파이프
     * @throws IllegalArgumentException pipe가 null인 경우
     */
    public void addPipe(Pipe pipe) {
        if (Objects.isNull(pipe)) {
            log.error("추가하려는 파이프가 null입니다.");
            throw new IllegalArgumentException("Pipe는 null일 수 없습니다.");
        }
        pipes.add(pipe);
        log.info("파이프가 추가되었습니다. PipeId: {}", pipe.getId());
    }

    /**
     * 기존 출력 파이프를 제거합니다.
     *
     * @param pipe 제거할 파이프
     * @throws IllegalArgumentException pipe가 null인 경우
     */
    public void removePipe(Pipe pipe) {
        if (Objects.isNull(pipe)) {
            log.error("제거하려는 파이프가 null입니다.");
            throw new IllegalArgumentException("Pipe는 null일 수 없습니다.");
        }
        if (pipes.remove(pipe)) {
            log.info("파이프가 제거되었습니다. PipeId: {}", pipe.getId());
        } else {
            log.warn("제거하려는 파이프가 목록에 존재하지 않습니다. PipeId: {}", pipe.getId());
        }
    }

    /**
     * 포트의 고유 식별자를 반환합니다.
     *
     * @return 포트의 UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * 현재 연결된 모든 파이프의 목록을 반환합니다.
     *
     * @return 파이프 목록
     */
    public List<Pipe> getPipes() {
        return List.copyOf(pipes);
    }
}
