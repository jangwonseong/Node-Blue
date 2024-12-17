package com.samsa.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Message 클래스는 노드 간에 전달되는 메시지를 나타냅니다.
 * 각 메시지는 고유한 ID, 페이로드, 그리고 메타데이터를 포함합니다.
 * 
 * <p>
 * 메시지는 노드 간 통신의 기본 단위이며, 다음과 같은 특징을 가집니다:
 * </p>
 * <ul>
 * <li>고유한 UUID를 통한 메시지 식별</li>
 * <li>임의의 타입의 페이로드 지원</li>
 * <li>메타데이터를 통한 부가 정보 전달</li>
 * <li>방어적 복사를 통한 메타데이터 보호</li>
 * </ul>
 *
 * @author samsa
 * @version 1.0
 */
@Slf4j
public class Message {
    /** 메시지의 고유 식별자 */
    private final UUID id;

    /** 메시지의 실제 데이터 */
    private Object payload;

    /** 메시지의 부가 정보 */
    private final Map<String, Object> metadata;

    /**
     * 기본 메시지를 생성합니다.
     *
     * @param payload 메시지 내용
     * @throws IllegalArgumentException payload가 null인 경우
     */
    public Message(Object payload) {
        if (payload == null) {
            log.error("페이로드가 null입니다");
            throw new IllegalArgumentException("페이로드는 null일 수 없습니다");
        }
        this.id = UUID.randomUUID();
        this.payload = payload;
        this.metadata = new HashMap<>();
        log.debug("메시지 생성됨. ID: {}, Payload 타입: {}", id, payload.getClass().getSimpleName());
    }

    /**
     * 메타데이터를 포함한 메시지를 생성합니다.
     *
     * @param payload  메시지 내용
     * @param metadata 메시지 부가 정보
     * @throws IllegalArgumentException payload 또는 metadata가 null인 경우
     */
    public Message(Object payload, Map<String, Object> metadata) {
        if (payload == null) {
            log.error("페이로드가 null입니다");
            throw new IllegalArgumentException("페이로드는 null일 수 없습니다");
        }
        if (metadata == null) {
            log.error("메타데이터가 null입니다");
            throw new IllegalArgumentException("메타데이터는 null일 수 없습니다");
        }
        this.id = UUID.randomUUID();
        this.payload = payload;
        this.metadata = new HashMap<>(metadata);
        log.debug("메타데이터 포함 메시지 생성됨. ID: {}, Payload 타입: {}, Metadata 크기: {}", id,
                payload.getClass().getSimpleName(), metadata.size());
    }

    /**
     * ID를 지정하여 메시지를 생성합니다.
     *
     * @param id       메시지 식별자
     * @param payload  메시지 내용
     * @param metadata 메시지 부가 정보
     * @throws IllegalArgumentException 어느 하나라도 null인 경우
     */
    public Message(UUID id, Object payload, Map<String, Object> metadata) {
        if (id == null) {
            log.error("ID가 null입니다");
            throw new IllegalArgumentException("ID는 null일 수 없습니다");
        }
        if (payload == null) {
            log.error("페이로드가 null입니다. MessageId: {}", id);
            throw new IllegalArgumentException("페이로드는 null일 수 없습니다");
        }
        if (metadata == null) {
            log.error("메타데이터가 null입니다. MessageId: {}", id);
            throw new IllegalArgumentException("메타데이터는 null일 수 없습니다");
        }
        this.id = id;
        this.payload = payload;
        this.metadata = new HashMap<>(metadata);
        log.debug("전체 지정 메시지 생성됨. ID: {}, Payload 타입: {}, Metadata 크기: {}", id,
                payload.getClass().getSimpleName(), metadata.size());
    }

    public UUID getId() {
        return id;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata); // 방어적 복사
    }

    @Override
    public String toString() {
        return String.format("Message[id=%s, payload=%s, metadata=%s]", id, payload, metadata);
    }
}
