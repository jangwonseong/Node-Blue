package com.samsa.core.node;

import java.util.UUID;
import com.samsa.core.Message;
import com.samsa.core.port.OutPort;
import lombok.extern.slf4j.Slf4j;

/**
 * 메시지를 생성하고 출력하는 노드의 기본 구현을 제공하는 추상 클래스입니다.
 * 이 클래스는 메시지 생성과 전송에 대한 기본적인 프레임워크를 제공하며,
 * 실제 메시지 생성 로직은 하위 클래스에서 구현해야 합니다.
 * 
 * <p>주요 기능:
 * <ul>
 *   <li>메시지 생성 및 전송</li>
 *   <li>출력 포트 관리</li>
 *   <li>에러 처리 및 로깅</li>
 * </ul>
 * 
 * <p>사용 예시:
 * <pre>
 * public class CustomOutNode extends OutNode {
 *     {@literal @}Override
 *     protected Message createMessage() {
 *         // 메시지 생성 로직 구현
 *         return new Message(...);
 *     }
 * }
 * </pre>
 */
@Slf4j
public abstract class OutNode extends Node {
    private final OutPort port;

    /**
     * 기본 생성자입니다.
     * 새로운 UUID를 생성하여 노드를 초기화합니다.
     */
    protected OutNode() {
        this(UUID.randomUUID());
    }

    /**
     * 지정된 ID로 노드를 생성하는 생성자입니다.
     * 
     * @param id 노드의 고유 식별자
     */
    protected OutNode(UUID id) {
        super(id);
        port = new OutPort();
    }

    /**
     * 메시지를 출력 포트를 통해 전송합니다.
     * 
     * @param message 전송할 메시지
     * @throws IllegalStateException 출력 포트가 초기화되지 않은 경우
     * @throws IllegalArgumentException 메시지가 null인 경우
     * @throws RuntimeException 메시지 전송 중 오류가 발생한 경우
     */
    protected void emit(Message message) {
        if (port == null) {
            log.error("출력 포트가 초기화되지 않았습니다. NodeId: {}", getId());
            throw new IllegalStateException("출력 포트가 초기화되지 않았습니다");
        }
        if (message == null) {
            log.error("전송할 메시지가 null입니다. NodeId: {}", getId());
            throw new IllegalArgumentException("메시지는 null일 수 없습니다");
        }

        try {
            log.debug("메시지 전송 시작. NodeId: {}, MessageId: {}", getId(), message.getId());
            port.propagate(message);
            log.debug("메시지 전송 완료. NodeId: {}, MessageId: {}", getId(), message.getId());
        } catch (Exception e) {
            log.error("메시지 전송 중 오류 발생. NodeId: {}, MessageId: {}", getId(), message.getId(), e);
            throw new RuntimeException("메시지 전송 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 전송할 메시지를 생성합니다.
     * 이 메서드는 하위 클래스에서 구현해야 합니다.
     * 
     * @return 생성된 메시지
     */
    protected abstract Message createMessage();

    /**
     * 노드의 메인 실행 로직을 구현합니다.
     * 무한 루프로 실행되며, 메시지를 생성하고 전송하는 작업을 수행합니다.
     * 
     * <p>실행 과정:
     * <ol>
     *   <li>메시지 생성 ({@link #createMessage()} 호출)</li>
     *   <li>생성된 메시지 전송</li>
     *   <li>오류 발생 시 로깅 후 계속 실행</li>
     * </ol>
     */
    @Override
    public void run() {
        while (true) {
            try {
                Message message = createMessage();
                log.info("메시지 출력");
                emit(message);
            } catch (Exception e) {
                log.error("run 실행 중 오류 발생. NodeId: {}", getId(), e);
            }
        }
    }

    /**
     * 노드의 출력 포트를 반환합니다.
     * 
     * @return 노드의 출력 포트
     */
    public OutPort getPort() {
        return port;
    }
}
