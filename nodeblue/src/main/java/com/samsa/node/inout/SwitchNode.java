package com.samsa.node.inout;

import java.util.Map;
import java.util.UUID;

import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;

import lombok.extern.slf4j.Slf4j;

/**
 * 메시지의 메타데이터를 기반으로 메시지 흐름을 제어하는 노드입니다.
 * 지정된 메타데이터 키가 존재하는 경우에만 메시지를 전달합니다.
 * 메타데이터 기반 메시지 필터링
 * 단일 출력 포트로 메시지 전달
 * 메타데이터 키의 존재 여부만 확인
 * 
 * @author samsa
 * @version 1.0
 */

@Slf4j
public class SwitchNode extends InOutNode {
    private String metadataKey;

    /**
     * 기본 생성자입니다.
     * 랜덤 UUID를 가진 새로운 SwitchNode를 생성합니다.
     */
    public SwitchNode() {
        super();
    }

    /**
     * 지정된 ID로 SwitchNode를 생성합니다.
     * 
     * @param id 노드의 고유 식별자
     * @throws IllegalArgumentException id가 null인 경우
     */
    public SwitchNode(UUID id) {
        super(id);
    }

    /**
     * 검사할 메타데이터 키를 설정합니다.
     * 
     * @param key 검사할 메타데이터 키
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
     * 현재 설정된 메타데이터 키를 반환합니다.
     * 
     * @return 설정된 메타데이터 키
     */
    public String getMetadataKey() {
        return metadataKey;
    }

    /**
     * 메시지를 수신하여 메타데이터를 검사하고 조건에 따라 전달합니다.
     * 지정된 메타데이터 키가 존재하는 경우에만 메시지를 전달합니다.
     * 
     * @param message 처리할 메시지
     * @throws IllegalStateException 메타데이터 키가 설정되지 않은 경우
     */
    @Override
    protected void onMessage(Message message) {
        if (metadataKey == null) {
            log.error("메타데이터 키가 설정되지 않았습니다. NodeId: {}", getId());
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

            if (metadata.containsKey(metadataKey)) {
                log.debug("메타데이터 키 존재함. 메시지 전달. NodeId: {}, MessageId: {}, Key: {}",
                        getId(), message.getId(), metadataKey);
                super.onMessage(message);
            } else {
                log.debug("메타데이터 키가 존재하지 않음. 메시지 폐기. NodeId: {}, MessageId: {}, Key: {}",
                        getId(), message.getId(), metadataKey);
            }

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
        return String.format("SwitchNode[id=%s, metadataKey=%s]", getId(), metadataKey);
    }
}