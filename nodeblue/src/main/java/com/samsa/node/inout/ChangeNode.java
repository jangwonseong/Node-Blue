package com.samsa.node.inout;

import java.util.Map;
import java.util.UUID;

import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;

import lombok.extern.slf4j.Slf4j;

/**
 * 메시지의 메타데이터를 변경하는 노드입니다.
 * 지정된 키에 대해 새로운 값을 설정하는 기능을 제공합니다.
 * 
 * Node-RED의 change 노드와 비교했을 때 다음과 같은 제한사항이 있습니다:
 * - 속성 값 설정만 가능 (삭제, 이동, 이름 변경 불가)
 * - 한 번에 하나의 속성만 변경 가능 (여러 규칙 적용 불가)
 * - 단순한 값 설정만 지원 (JSONata 표현식, 타입 변환 등 불가)
 *
 * @author samsa
 * @version 1.0
 */

@Slf4j
public class ChangeNode extends InOutNode {
    private String metadataKey;
    private Object metadataValue;

    /**
     * 기본 생성자입니다.
     * 랜덤 UUID를 사용하여 노드를 생성합니다.
     */
    public ChangeNode() {
        super();
    }

    /**
     * 지정된 ID로 ChangeNode를 생성합니다.
     *
     * @param id 노드의 고유 식별자
     * @throws IllegalArgumentException id가 null인 경우
     */
    public ChangeNode(UUID id) {
        super(id);
    }

    /**
     * 변경할 메타데이터의 키를 설정합니다.
     *
     * @param key 메타데이터 키
     * @throws IllegalArgumentException key가 null이거나 빈 문자열인 경우
     */
    public void setMetadataKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            log.error("메타데이터 키가 null이거나 비어있습니다. NodeId: {}", getId());
            throw new IllegalArgumentException("메타데이터 키는 null이거나 비어있을 수 없습니다");
        }
        this.metadataKey = key.trim();
        log.debug("메타데이터 키 설정됨. NodeId: {}, Key: {}", getId(), this.metadataKey);
    }

    /**
     * 메타데이터에 설정할 값을 지정합니다.
     *
     * @param value 설정할 값
     */
    public void setMetadataValue(Object value) {
        this.metadataValue = value;
        log.debug("메타데이터 값 설정됨. NodeId: {}, Value: {}", getId(), value);
    }

    /**
     * 현재 설정된 메타데이터 키를 반환합니다.
     *
     * @return 설정된 메타데이터 키
     */
    public String getMetadataKey() {
        return metadataKey;
    }

    /**
     * 현재 설정된 메타데이터 값을 반환합니다.
     *
     * @return 설정된 메타데이터 값
     */
    public Object getMetadataValue() {
        return metadataValue;
    }

    /**
     * 메시지를 수신하여 지정된 메타데이터를 변경합니다.
     * 메타데이터 키와 값이 설정되어 있어야 합니다.
     *
     * @param message 처리할 메시지
     * @throws IllegalStateException 메타데이터 키나 값이 설정되지 않은 경우
     */
    protected void onMessage(Message message) {
        if (metadataKey == null) {
            log.error("메타데이터 키가 설정되지 않았습니다. NodeId: {}", this.getId());
            throw new IllegalStateException("메타데이터 키가 설정되지 않았습니다");
        }

        try {
            if (message == null) {
                log.warn("수신된 메시지가 null입니다. NodeId: {}", getId());
                return;
            }

            Map<String, Object> metadata = message.getMetadata();
            if (metadata == null) {
                log.warn("메시지의 메타데이터가 null입니다. NodeId: {}, MessageId: {}",
                        getId(), message.getId());
                return;
            }

            // 메타데이터 변경
            metadata.put(metadataKey, metadataValue);
            log.debug("메타데이터 변경됨. NodeId: {}, MessageId: {}, Key: {}, Value: {}",
                    getId(), message.getId(), metadataKey, metadataValue);

            // 변경된 메시지 전달
            super.onMessage(message);

        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생. NodeId: {}, MessageId: {}",
                    getId(), message != null ? message.getId() : "null", e);
            throw new RuntimeException("메시지 처리 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 노드의 현재 상태를 문자열로 반환합니다.
     *
     * @return 노드의 상태 정보를 포함한 문자열
     */
    @Override
    public String toString() {
        return String.format("ChangeNode[id=%s, metadataKey=%s, metadataValue=%s]",
                getId(), metadataKey, metadataValue);
    }
}