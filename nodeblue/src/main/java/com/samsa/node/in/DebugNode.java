package com.samsa.node.in;

import java.util.UUID;
import com.samsa.core.Message;
import com.samsa.core.node.InNode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.samsa.annotation.NodeType;

import lombok.extern.slf4j.Slf4j;

/**
 * 디버깅 목적으로 수신된 메시지를 로깅하는 입력 노드입니다. 설정 가능한 로그 레벨과 메타데이터 포함 옵션을 제공하여 유연한 디버깅을 지원합니다.
 * 
 * 이 노드는 다음과 같은 기능을 제공합니다: - 다양한 로그 레벨 지원 (DEBUG, INFO, WARN, ERROR) - 메타데이터 포함/제외 옵션 - null 메시지 및
 * 페이로드 처리 - 예외 상황에 대한 오류 로깅
 * 
 * @author samsa
 * @version 1.0
 */
@NodeType("DebugNode")
@Slf4j
public class DebugNode extends InNode {
    private String logLevel = "INFO";

    /**
     * 기본 생성자입니다. 랜덤 UUID를 사용하여 노드를 생성합니다. 기본 로그 레벨은 "INFO"로 설정됩니다.
     */
    @JsonCreator
    public DebugNode() {
        super();
    }

    /**
     * 지정된 ID로 DebugNode를 생성합니다.
     *
     * @param id 노드의 고유 식별자 (UUID 형식의 문자열)
     * @throws IllegalArgumentException id가 유효한 UUID 형식이 아닌 경우
     */
    public DebugNode(UUID id) {
        super(id);
    }

    /**
     * 로그 레벨을 설정합니다.
     * 
     * @param level 설정할 로그 레벨 ("DEBUG", "INFO", "WARN", "ERROR" 중 하나)
     * @throws IllegalArgumentException 유효하지 않은 로그 레벨이 지정된 경우
     */
    public void setLogLevel(String level) {
        if (!isValidLogLevel(level)) {
            throw new IllegalArgumentException("유효하지 않은 로그 레벨: " + level);
        }
        this.logLevel = level.toUpperCase();
    }

    /**
     * 로그 레벨의 유효성을 검사합니다.
     *
     * @param level 검사할 로그 레벨
     * @return 유효한 로그 레벨인 경우 true, 그렇지 않은 경우 false
     */
    private boolean isValidLogLevel(String level) {
        if (level == null)
            return false;
        String upperLevel = level.toUpperCase();
        return upperLevel.equals("DEBUG") || upperLevel.equals("INFO") || upperLevel.equals("WARN")
                || upperLevel.equals("ERROR");
    }

    /**
     * 메시지를 수신하여 설정된 로그 레벨로 로깅합니다. null 메시지나 null 페이로드의 경우 경고 로그를 생성합니다.
     *
     * @param message 처리할 메시지
     * @throws IllegalStateException 메시지 처리 중 오류가 발생한 경우
     */
    @Override
    public void onMessage(Message message) {
        try {
            if (message == null) {
                log.warn("노드[{}] - null 메시지가 수신되었습니다", getId());
                return;
            }

            Object payload = message.getPayload();
            if (payload == null) {
                log.warn("노드[{}] - 메시지에 null 페이로드가 포함되어 있습니다", getId());
                return;
            }

            String logMessage = formatLogMessage(message);
            logMessage(logMessage);

        } catch (Exception e) {
            log.error("노드[{}] - 메시지 처리 중 오류 발생", getId(), e);
            throw new IllegalStateException("메시지 처리에 실패했습니다", e);
        }
    }

    /**
     * 메시지를 로그 형식으로 포맷팅합니다.
     *
     * @param message 포맷팅할 메시지
     * @return 포맷팅된 로그 메시지
     */
    private String formatLogMessage(Message message) {
        StringBuilder sb = new StringBuilder();
        sb.append("노드[").append(getId()).append("] - ");
        sb.append("페이로드: ").append(message.getPayload());
        return sb.toString();
    }

    /**
     * 지정된 메시지를 현재 설정된 로그 레벨로 로깅합니다.
     *
     * @param message 로깅할 메시지
     */
    private void logMessage(String message) {
        switch (logLevel) {
            case "DEBUG":
                log.debug(message);
                break;
            case "INFO":
                log.info(message);
                break;
            case "WARN":
                log.warn(message);
                break;
            case "ERROR":
                log.error(message);
                break;
        }
    }
}
