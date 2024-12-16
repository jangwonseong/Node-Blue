package com.samsa.node.inout;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.samsa.core.InOutNode;
import com.samsa.core.InPort;
import com.samsa.core.Message;
import com.samsa.core.OutPort;

import lombok.extern.slf4j.Slf4j;

/**
 * YAML 파서 노드 클래스. 메시지의 페이로드를 YAML 문자열과 객체 간 변환합니다.
 * <p>
 * 이 클래스는 메시지의 페이로드가 YAML 문자열인 경우 이를 지정된 클래스의 객체로 변환하며, 객체인 경우 YAML 문자열로 직렬화하여 출력합니다.
 * </p>
 */
@Slf4j
public class YamlParserNode extends InOutNode {

    private final Class<?> targetClass;
    private static final YAMLMapper YAML_MAPPER = new YAMLMapper();

    /**
     * YamlParserNode 객체를 생성합니다.
     *
     * @param id 노드의 고유 식별자
     * @param inPort 입력 포트
     * @param outPort 출력 포트
     * @param targetClass YAML 문자열을 변환할 대상 클래스
     * @throws IllegalArgumentException 입력 포트, 출력 포트, 또는 타겟 클래스가 null인 경우
     */
    public YamlParserNode(UUID id, Class<?> targetClass) {
        super(id);
        if (targetClass == null) {
            log.error("타겟 클래스가 null입니다. NodeId: {}", id);
            throw new IllegalArgumentException("타겟 클래스는 null일 수 없습니다.");
        }
        this.targetClass = targetClass;
    }

    /**
     * 메시지를 수신하고 페이로드를 변환하여 방출합니다.
     *
     * @param message 수신된 메시지
     * @throws IllegalArgumentException 메시지가 null인 경우
     */
    @Override
    public void onMessage(Message message) {
        if (message == null) {
            log.error("처리할 메시지가 null입니다. NodeId: {}", getId());
            throw new IllegalArgumentException("메시지는 null일 수 없습니다.");
        }

        try {
            Object result;

            if (message.getPayload() instanceof String yamlString) {
                // YAML 문자열을 객체로 변환
                result = parseYamlToObject(yamlString);
            } else {
                // 객체를 YAML 문자열로 변환
                result = convertObjectToYaml(message.getPayload());
            }

            // 성공적으로 변환된 메시지 방출
            emit(new Message(result));
        } catch (JsonProcessingException e) {
            log.error("YAML 처리 중 오류 발생. NodeId: {}, MessageId: {}, 오류: {}", getId(),
                    message.getId(), e.getMessage(), e);
            // JsonProcessingException을 RuntimeException으로 감싸서 던짐
            throw new RuntimeException("YAML 처리 중 오류 발생", e);
        } catch (Exception e) {
            log.error("메시지 처리 중 예기치 못한 오류 발생. NodeId: {}, MessageId: {}, 오류: {}", getId(),
                    message.getId(), e.getMessage(), e);
        }
    }


    /**
     * 노드 실행 시작 시 초기 메시지를 처리합니다.
     *
     * <p>
     * 이 메서드는 노드가 실행되면 입력 포트에서 첫 메시지를 수신하고 이를 처리합니다.
     * </p>
     */
    @Override
    public void start() {
        try {
            onMessage(receive());
        } catch (Exception e) {
            log.error("노드 시작 중 메시지 처리 오류 발생. NodeId: {}, 오류: {}", getId(), e.getMessage(), e);
        }
    }

    /**
     * YAML 문자열을 지정된 객체로 변환합니다.
     *
     * @param yamlString YAML 문자열
     * @return 변환된 객체
     * @throws JsonProcessingException YAML 파싱 중 오류가 발생한 경우
     */
    private Object parseYamlToObject(String yamlString) throws JsonProcessingException {
        log.debug("YAML 문자열을 객체로 변환 중. NodeId: {}, YAML: {}", getId(), yamlString);
        return YAML_MAPPER.readValue(yamlString, targetClass);
    }

    /**
     * 객체를 YAML 문자열로 변환합니다.
     *
     * @param payload 변환할 객체
     * @return YAML 문자열
     * @throws JsonProcessingException YAML 직렬화 중 오류가 발생한 경우
     */
    private String convertObjectToYaml(Object payload) throws JsonProcessingException {
        log.debug("객체를 YAML 문자열로 변환 중. NodeId: {}, Payload: {}", getId(), payload);
        return YAML_MAPPER.writeValueAsString(payload);
    }
}
