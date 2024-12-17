package com.samsa.core.node;

import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Node 클래스는 메시지 처리 시스템의 기본 노드 구현을 제공하는 추상 클래스입니다.
 * 모든 구체적인 노드 클래스의 기반이 되며, 노드의 기본 속성과 동작을 정의합니다.
 * 
 * <p>
 * 주요 특징:
 * </p>
 * <ul>
 * <li>고유 ID를 통한 노드 식별: UUID를 사용하여 각 노드를 고유하게 식별</li>
 * <li>노드 상태 관리: CREATED, RUNNING, STOPPED, ERROR 상태 추적</li>
 * <li>Runnable 인터페이스 구현: 비동기 실행 지원</li>
 * <li>로깅 기능 통합: SLF4J를 통한 로깅 지원</li>
 * </ul>
 *
 * <p>
 * 노드 상태 흐름:
 * </p>
 * 
 * <pre>
 * CREATED → RUNNING → STOPPED
 *    ↓          ↓
 *    └──→    ERROR
 * </pre>
 *
 * <p>
 * 사용 예시:
 * </p>
 * 
 * <pre>
 * public class CustomNode extends Node {
 *     public CustomNode() {
 *         super();
 *         setStatus(NodeStatus.CREATED);
 *     }
 *     
 *     {@literal @}Override
 *     public void run() {
 *         setStatus(NodeStatus.RUNNING);
 *         try {
 *             // 노드 로직 구현
 *         } catch (Exception e) {
 *             setStatus(NodeStatus.ERROR);
 *         }
 *     }
 * }
 * </pre>
 *
 * @author samsa
 * @version 1.0
 * @see Runnable
 * @see UUID
 */
@Slf4j
public abstract class Node implements Runnable {
    /**
     * 노드의 가능한 상태를 정의하는 열거형입니다.
     */
    public enum NodeStatus {
        /** 노드가 생성된 초기 상태 */
        CREATED,
        /** 노드가 실행 중인 상태 */
        RUNNING,
        /** 노드가 정상적으로 중지된 상태 */
        STOPPED,
        /** 노드에 오류가 발생한 상태 */
        ERROR
    }

    /** 노드의 고유 식별자 */
    private UUID id;

    /** 노드의 현재 상태 */
    private NodeStatus status = NodeStatus.CREATED;

    /**
     * 기본 생성자. 새로운 UUID로 노드를 생성합니다.
     */
    protected Node() {
        this(UUID.randomUUID());
    }

    /**
     * 지정된 ID로 노드를 생성합니다.
     *
     * @param id 노드의 고유 식별자
     * @throws IllegalArgumentException id가 null인 경우
     */
    protected Node(UUID id) {
        if (Objects.isNull(id)) {
            log.error("Node ID cannot be null");
            throw new IllegalArgumentException("Node ID cannot be null");
        }
        this.id = id;
    }

    /**
     * 노드의 고유 식별자를 반환합니다.
     *
     * @return 노드의 UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * 노드의 고유 식별자를 설정합니다.
     *
     * @param id 설정할 UUID
     * @throws IllegalArgumentException id가 null인 경우
     */
    public void setId(UUID id) {
        if (Objects.isNull(id)) {
            log.error("Node ID cannot be null");
            throw new IllegalArgumentException("Node ID cannot be null");
        }
        this.id = id;
    }

    /**
     * 노드의 현재 상태를 반환합니다.
     *
     * @return 현재 노드 상태
     */
    public NodeStatus getStatus() {
        return status;
    }

    /**
     * 노드의 상태를 설정합니다.
     *
     * @param status 설정할 노드 상태
     * @throws IllegalArgumentException status가 null인 경우
     */
    public void setStatus(NodeStatus status) {
        if (Objects.isNull(status)) {
            log.error("Node Status cannot be null");
            throw new IllegalArgumentException("Node Status cannot be null");
        }
        this.status = status;
    }
}
