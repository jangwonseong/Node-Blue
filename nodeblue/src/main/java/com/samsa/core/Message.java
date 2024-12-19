package com.samsa.core;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * {@code Message} 클래스는 노드 간에 전달되는 메시지를 나타냅니다. 각 메시지는 고유한 ID, 페이로드, 그리고 메타데이터를 포함합니다.
 * 
 * <p>
 * 메시지는 노드 간 통신의 기본 단위로 사용되며, 다음과 같은 특징을 가집니다:
 * </p>
 * <ul>
 * <li>고유한 UUID를 통한 메시지 식별</li>
 * <li>임의의 타입의 페이로드 지원</li>
 * <li>메타데이터를 통한 부가 정보 전달</li>
 * <li>방어적 복사를 통한 메타데이터 보호</li>
 * </ul>
 *
 * @author samsa
 * @version 1.1
 */
@Slf4j
public class Message {

    /**
     * 메시지의 고유 식별자
     */
    private final UUID id;

    /**
     * 메시지의 실제 데이터
     */
    private Object payload;

    /**
     * 기본 메시지를 생성합니다.
     * 
     * 메시지 ID는 자동으로 생성되며, 페이로드를 전달받습니다.
     *
     * @param payload 메시지 내용
     * @throws IllegalArgumentException payload가 null일 경우 예외 발생
     */
    public Message(Object payload) {
        this(UUID.randomUUID(), payload);
    }

    /**
     * ID를 지정하여 메시지를 생성합니다.
     *
     * @param id 메시지 식별자
     * @param payload 메시지 내용
     * @throws IllegalArgumentException id 또는 payload가 null인 경우 예외 발생
     */
    public Message(UUID id, Object payload) {
        if (id == null) {
            log.error("ID가 null입니다.");
            throw new IllegalArgumentException("ID는 null일 수 없습니다.");
        }
        if (payload == null) {
            log.error("페이로드가 null입니다. Message ID: {}", id);
            throw new IllegalArgumentException("페이로드는 null일 수 없습니다.");
        }
        this.id = id;
        this.payload = payload;
        log.debug("메시지가 생성되었습니다. ID: {}, Payload 타입: {}", id, payload.getClass().getSimpleName());
    }

    /**
     * 메시지의 고유 ID를 반환합니다.
     *
     * @return 메시지 ID
     */
    public UUID getId() {
        return id;
    }

    /**
     * 메시지의 페이로드를 반환합니다.
     *
     * @return 메시지 페이로드
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * 메시지의 페이로드를 설정합니다.
     *
     * @param payload 새로 설정할 페이로드
     * @throws IllegalArgumentException payload가 null일 경우 예외 발생
     */
    public void setPayload(Object payload) {
        if (payload == null) {
            log.error("페이로드를 null로 설정하려고 시도했습니다. Message ID: {}", id);
            throw new IllegalArgumentException("페이로드는 null일 수 없습니다.");
        }
        this.payload = payload;
        log.debug("메시지의 페이로드가 업데이트되었습니다. ID: {}, 새로운 Payload 타입: {}", id,
                payload.getClass().getSimpleName());
    }

    /**
     * 메시지 객체의 문자열 표현을 반환합니다.
     * 
     * 메시지의 ID와 페이로드 정보가 포함됩니다.
     *
     * @return 메시지의 문자열 표현
     */
    @Override
    public String toString() {
        return String.format("Message[id=%s, payload=%s]", id, payload);
    }
}
