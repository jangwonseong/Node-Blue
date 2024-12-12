package com.samsa.node.inout;

import com.samsa.core.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RangeNode {

    private String id;
    private double inputMin;
    private double inputMax;
    private double outputMin;
    private double outputMax;
    private boolean constrainToTarget;

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
     * 메시지를 처리하고 매핑된 결과를 반환합니다.
     *
     * @param message 처리할 메시지
     * @return 매핑된 결과 값
     */
    public Double processMessage(Message message) {
        try {
            // 메시지에서 payload를 추출
            Object payload = message.getPayload();
            if (payload instanceof Number) {
                double inputValue = ((Number) payload).doubleValue();

                // 입력 값을 새로운 범위로 매핑
                double mappedValue = mapValue(inputValue);

                // constrainToTarget가 활성화된 경우, 대상 범위 내로 값 제한
                if (constrainToTarget) {
                    mappedValue = Math.max(outputMin, Math.min(outputMax, mappedValue));
                }

                // 매핑 결과를 로그로 출력
                log.info("RangeNode[{}]: Mapped {} to {}", id, inputValue, mappedValue);

                return mappedValue;
            } else {
                log.error("RangeNode[{}]: 잘못된 payload 타입. Number 타입이 예상되었으나, {} 타입이 입력됨", id,
                        payload.getClass().getName());
                return null;
            }
        } catch (Exception e) {
            log.error("RangeNode[{}]: 메시지 처리 중 오류 발생", id, e);
            return null;
        }
    }

    /**
     * 주어진 값을 새로운 범위로 매핑합니다.
     *
     * 공식: (value - inputMin) / (inputMax - inputMin) * (outputMax - outputMin) + outputMin
     *
     * @param value 매핑할 입력 값
     * @return 매핑된 값
     */
    private double mapValue(double value) {
        return (value - inputMin) / (inputMax - inputMin) * (outputMax - outputMin) + outputMin;
    }

    // RangeNode 속성의 getter와 setter
    public double getInputMin() {
        return inputMin;
    }

    public void setInputMin(double inputMin) {
        this.inputMin = inputMin;
    }

    public double getInputMax() {
        return inputMax;
    }

    public void setInputMax(double inputMax) {
        this.inputMax = inputMax;
    }

    public double getOutputMin() {
        return outputMin;
    }

    public void setOutputMin(double outputMin) {
        this.outputMin = outputMin;
    }

    public double getOutputMax() {
        return outputMax;
    }

    public void setOutputMax(double outputMax) {
        this.outputMax = outputMax;
    }

    public boolean isConstrainToTarget() {
        return constrainToTarget;
    }

    public void setConstrainToTarget(boolean constrainToTarget) {
        this.constrainToTarget = constrainToTarget;
    }
}
