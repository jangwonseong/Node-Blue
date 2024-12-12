package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RangeNode extends InOutNode {

    private double inputMin;
    private double inputMax;
    private double outputMin;
    private double outputMax;
    private boolean constrainToTarget;

    /**
     * RangeNode 생성자입니다.
     *
     * @param id 노드의 고유 식별자
     * @param inputMin 입력 값의 최소 범위
     * @param inputMax 입력 값의 최대 범위
     * @param outputMin 출력 값의 최소 범위
     * @param outputMax 출력 값의 최대 범위
     * @param constrainToTarget 매핑된 값을 출력 범위 내로 제한할지 여부를 나타내는 플래그
     */
    public RangeNode(String id, double inputMin, double inputMax, double outputMin,
            double outputMax, boolean constrainToTarget) {
        super(id);
        this.inputMin = inputMin;
        this.inputMax = inputMax;
        this.outputMin = outputMin;
        this.outputMax = outputMax;
        this.constrainToTarget = constrainToTarget;
    }

    /**
     * 메시지를 처리하는 메소드입니다. 전달받은 메시지의 payload가 숫자일 경우 해당 값을 설정된 범위에 따라 매핑하고, constrainToTarget 옵션이
     * 활성화되어 있다면 출력 범위 내로 제한합니다. 매핑된 값은 새로운 메시지로 변환하여 emit 메소드를 통해 출력됩니다.
     * 
     * @param message 처리할 메시지
     */
    @Override
    public void onMessage(Message message) {
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
                log.info("RangeNode[{}]: Mapped {} to {}", getId(), inputValue, mappedValue);

                // 매핑된 값으로 새로운 메시지 생성 및 전송
                Message outputMessage = new Message(mappedValue);
                emit(outputMessage);
            } else {
                // payload 타입이 Number가 아닌 경우 로그에 에러 기록
                log.error("RangeNode[{}]: 잘못된 payload 타입. Number 타입이 예상되었으나, {} 타입이 입력됨", getId(),
                        payload.getClass().getName());
            }
        } catch (Exception e) {
            // 예외 발생 시 처리
            handleError(e);
        }
    }

    /**
     * 주어진 값을 새로운 범위로 매핑합니다.
     * 
     * 공식: (value - inputMin) / (inputMax - inputMin) * (outputMax - outputMin) + outputMin
     * 
     * 이 공식은 입력 값(value)을 입력 범위(inputMin ~ inputMax)에서 출력 범위(outputMin ~ outputMax)로 선형 변환합니다.
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
