package com.samsa.node.inout;

import com.samsa.core.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * 입력 값을 하나의 범위에서 다른 범위로 매핑하고, 선택적으로 결과를 대상 범위로 제한하는 노드입니다.
 */
@Slf4j
public class RangeNode {

    private String id;
    private double inputMin;
    private double inputMax;
    private double outputMin;
    private double outputMax;
    private boolean constrainToTarget;

    /**
     * 지정된 매개변수로 RangeNode를 생성합니다.
     *
     * @param id 이 노드의 고유 식별자
     * @param inputMin 입력 범위의 최소값
     * @param inputMax 입력 범위의 최대값
     * @param outputMin 출력 범위의 최소값
     * @param outputMax 출력 범위의 최대값
     * @param constrainToTarget 매핑된 값을 출력 범위로 제한할지 여부
     */
    public RangeNode(String id, double inputMin, double inputMax, double outputMin,
            double outputMax, boolean constrainToTarget) {
        this.id = id;
        this.inputMin = inputMin;
        this.inputMax = inputMax;
        this.outputMin = outputMin;
        this.outputMax = outputMax;
        this.constrainToTarget = constrainToTarget;
    }

    /**
     * 메시지를 처리하여 입력 범위에서 출력 범위로 페이로드를 매핑합니다.
     *
     * @param message 처리할 메시지
     * @return 매핑된 값, 또는 페이로드가 유효한 숫자가 아닐 경우 {@code null}
     */
    public Double processMessage(Message message) {
        try {
            // 메시지에서 페이로드 추출
            Object payload = message.getPayload();
            if (payload instanceof Number) {
                double inputValue = ((Number) payload).doubleValue();

                // 입력 값을 출력 범위로 매핑
                double mappedValue = mapValue(inputValue);

                // 대상 범위로 제한이 활성화되어 있으면 값을 제한
                if (constrainToTarget) {
                    mappedValue = Math.max(outputMin, Math.min(outputMax, mappedValue));
                }

                // 매핑 결과 로그 출력
                log.info("RangeNode[{}]: {}를 {}로 매핑", id, inputValue, mappedValue);

                return mappedValue;
            } else {
                log.error("RangeNode[{}]: 잘못된 페이로드 타입. Number가 예상되었으나 {}가 수신됨", id,
                        payload.getClass().getName());
                return null;
            }
        } catch (Exception e) {
            log.error("RangeNode[{}]: 메시지 처리 중 오류", id, e);
            return null;
        }
    }

    /**
     * 주어진 값을 입력 범위에서 출력 범위로 매핑합니다.
     *
     * 수식: (값 - 입력 최소값) / (입력 최대값 - 입력 최소값) * (출력 최대값 - 출력 최소값) + 출력 최소값
     *
     * @param value 매핑할 값
     * @return 매핑된 값
     */
    private double mapValue(double value) {
        return (value - inputMin) / (inputMax - inputMin) * (outputMax - outputMin) + outputMin;
    }

    /**
     * 입력 범위의 최소값을 가져옵니다.
     *
     * @return 입력 범위의 최소값
     */
    public double getInputMin() {
        return inputMin;
    }

    /**
     * 입력 범위의 최소값을 설정합니다.
     *
     * @param inputMin 설정할 입력 최소값
     */
    public void setInputMin(double inputMin) {
        this.inputMin = inputMin;
    }

    /**
     * 입력 범위의 최대값을 가져옵니다.
     *
     * @return 입력 범위의 최대값
     */
    public double getInputMax() {
        return inputMax;
    }

    /**
     * 입력 범위의 최대값을 설정합니다.
     *
     * @param inputMax 설정할 입력 최대값
     */
    public void setInputMax(double inputMax) {
        this.inputMax = inputMax;
    }

    /**
     * 출력 범위의 최소값을 가져옵니다.
     *
     * @return 출력 범위의 최소값
     */
    public double getOutputMin() {
        return outputMin;
    }

    /**
     * 출력 범위의 최소값을 설정합니다.
     *
     * @param outputMin 설정할 출력 최소값
     */
    public void setOutputMin(double outputMin) {
        this.outputMin = outputMin;
    }

    /**
     * 출력 범위의 최대값을 가져옵니다.
     *
     * @return 출력 범위의 최대값
     */
    public double getOutputMax() {
        return outputMax;
    }

    /**
     * 출력 범위의 최대값을 설정합니다.
     *
     * @param outputMax 설정할 출력 최대값
     */
    public void setOutputMax(double outputMax) {
        this.outputMax = outputMax;
    }

    /**
     * 매핑된 값이 대상 범위로 제한되는지 여부를 확인합니다.
     *
     * @return 값이 대상 범위로 제한되면 {@code true}, 그렇지 않으면 {@code false}
     */
    public boolean isConstrainToTarget() {
        return constrainToTarget;
    }

    /**
     * 매핑된 값을 대상 범위로 제한할지 여부를 설정합니다.
     *
     * @param constrainToTarget {@code true}일 경우 값을 제한하고, {@code false}일 경우 제한하지 않음
     */
    public void setConstrainToTarget(boolean constrainToTarget) {
        this.constrainToTarget = constrainToTarget;
    }
}
