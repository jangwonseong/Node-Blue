package com.samsa.node.in;

import java.util.UUID;
import com.samsa.core.InNode;
import com.samsa.core.InPort;
import com.samsa.core.Message;
import com.samsa.core.Node;
import lombok.extern.slf4j.Slf4j;

/**
 * 디버깅 목적으로 메시지를 로깅하는 입력 노드입니다.
 * 다양한 로그 레벨과 메타데이터 포함 옵션을 지원합니다.
 *
 * @author samsa
 * @version 1.0
 */
@Slf4j
public class DebugNode extends InNode {
    private boolean includeMetadata = false;
    private String logLevel = "INFO";

    /**
     * 기본 생성자입니다. 랜덤 UUID를 사용하여 노드를 생성합니다.
     * 기본 로그 레벨은 "INFO"로 설정됩니다.
     */
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
     * 메시지 로깅 시 메타데이터 포함 여부를 설정합니다.
     *
     * @param include true인 경우 메타데이터를 로그에 포함, false인 경우 제외
     */
    public void setIncludeMetadata(boolean include) {
        this.includeMetadata = include;
    }

    /**
     * 로그 레벨을 설정합니다.
     * 
     * @param level 설정할 로그 레벨 ("DEBUG", "INFO", "WARN", "ERROR" 중 하나)
     * @throws IllegalArgumentException 유효하지 않은 로그 레벨이 지정된 경우
     */
    public void setLogLevel(String level) {
        if (!isValidLogLevel(level)) {
            throw new IllegalArgumentException("Invalid log level: " + level);
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
        return upperLevel.equals("DEBUG") ||
                upperLevel.equals("INFO") ||
                upperLevel.equals("WARN") ||
                upperLevel.equals("ERROR");
    }

    /**
     * 메시지를 수신하여 설정된 로그 레벨로 로깅합니다.
     * null 메시지나 null 페이로드의 경우 경고 로그를 생성합니다.
     *
     * @param message 처리할 메시지
     * @throws IllegalStateException 메시지 처리 중 오류가 발생한 경우
     */
    @Override
    public void onMessage(Message message) {
        try {
            if (message == null) {
                log.warn("Node[{}] - Received null message", getId());
                return;
            }

            Object payload = message.getPayload();
            if (payload == null) {
                log.warn("Node[{}] - Message contains null payload", getId());
                return;
            }

            String logMessage = formatLogMessage(message);
            logMessage(logMessage);

        } catch (Exception e) {
            log.error("Node[{}] - Error processing message", getId(), e);
            handleError(e);
            throw new IllegalStateException("Failed to process message", e);
        }
    }

    /**
     * 현재 노드의 상태를 반환합니다.
     *
     * @return 노드의 현재 상태
     */
    public NodeStatus getStatus() {
        return status;
    }

    /**
     * 메시지를 로그 형식으로 포맷팅합니다.
     *
     * @param message 포맷팅할 메시지
     * @return 포맷팅된 로그 메시지
     */
    private String formatLogMessage(Message message) {
        StringBuilder sb = new StringBuilder();
        sb.append("Node[").append(getId()).append("] - ");
        sb.append("Payload: ").append(message.getPayload());

        if (includeMetadata) {
            sb.append(", Metadata: ").append(message.getMetadata());
        }

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

    /**
     * 에러를 처리하고 로깅합니다.
     * 부모 클래스의 에러 처리를 수행한 후 추가적인 디버그 정보를 로깅합니다.
     *
     * @param error 처리할 에러
     * @throws IllegalArgumentException error가 null인 경우
     */
    @Override
    public void handleError(Throwable error) {
        if (error == null) {
            throw new IllegalArgumentException("Error cannot be null");
        }
        super.handleError(error);
        log.error("Node[{}] - Debug error details: {}", getId(), error.getMessage());
    }
}