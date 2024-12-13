package com.samsa.node.inout;

import com.samsa.core.InOutNode;
import com.samsa.core.InPort;
import com.samsa.core.OutPort;
import com.samsa.core.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.UUID;

/**
 * 입력 값을 하나의 범위에서 다른 범위로 매핑하고, 선택적으로 결과를 대상 범위로 제한하는 노드입니다. 이 노드는 주로 다음과 같은 용도로 사용됩니다: - 센서 데이터의 정규화
 * - 아날로그 신호의 디지털 변환 - 값의 스케일 조정 - 범위 기반 데이터 변환
 */
@Slf4j
public class RangeNode extends InOutNode {

    /** 입력 범위의 최소값 */
    private double inputMin;

    /** 입력 범위의 최대값 */
    private double inputMax;

    /** 출력 범위의 최소값 */
    private double outputMin;

    /** 출력 범위의 최대값 */
    private double outputMax;

    /** 출력값을 지정된 범위로 제한할지 여부 */
    private boolean constrainToTarget;

    /**
     * RangeNode를 생성합니다. 이 생성자는 모든 매개변수를 지정하여 RangeNode를 초기화합니다. 입력값은 inputMin과 inputMax 사이의 범위에서
     * outputMin과 outputMax 사이의 범위로 매핑됩니다.
     *
     * @param id 노드의 고유 ID
     * @param inPort 입력 포트. 숫자 타입의 메시지를 받습니다.
     * @param outPort 출력 포트. 매핑된 숫자 값을 전달합니다.
     * @param inputMin 입력 범위의 최소값. inputMax보다 작아야 합니다.
     * @param inputMax 입력 범위의 최대값. inputMin보다 커야 합니다.
     * @param outputMin 출력 범위의 최소값. outputMax보다 작아야 합니다.
     * @param outputMax 출력 범위의 최대값. outputMin보다 커야 합니다.
     * @param constrainToTarget true인 경우 출력값을 outputMin과 outputMax 사이로 제한합니다.
     * @throws IllegalArgumentException 범위 값이 잘못되었거나 포트가 null인 경우
     */
    public RangeNode(UUID id, InPort inPort, OutPort outPort, double inputMin, double inputMax,
            double outputMin, double outputMax, boolean constrainToTarget) {
        super(id, inPort, outPort);
        validateRange(inputMin, inputMax, outputMin, outputMax);
        this.inputMin = inputMin;
        this.inputMax = inputMax;
        this.outputMin = outputMin;
        this.outputMax = outputMax;
        this.constrainToTarget = constrainToTarget;
        log.info(
                "RangeNode 생성됨. ID: {}, InputRange: [{} - {}], OutputRange: [{} - {}], ConstrainToTarget: {}",
                id, inputMin, inputMax, outputMin, outputMax, constrainToTarget);
    }

    /**
     * 지정된 ID 없이 RangeNode를 생성합니다.
     */
    public RangeNode(InPort inPort, OutPort outPort, double inputMin, double inputMax,
            double outputMin, double outputMax, boolean constrainToTarget) {
        this(UUID.randomUUID(), inPort, outPort, inputMin, inputMax, outputMin, outputMax,
                constrainToTarget);
    }

    /**
     * 메시지를 처리하여 입력 범위에서 출력 범위로 페이로드를 매핑합니다. 이 메서드는 다음과 같은 순서로 동작합니다: 1. 입력 메시지의 유효성을 검사합니다. 2.
     * 페이로드가 Number 타입인지 확인합니다. 3. 값을 입력 범위에서 출력 범위로 매핑합니다. 4. constrainToTarget이 true인 경우 결과를 출력
     * 범위로 제한합니다. 5. 매핑된 값을 새 메시지로 생성하여 전파합니다.
     *
     * @param message 처리할 메시지. 페이로드는 Number 타입이어야 합니다.
     */
    @Override
    public void onMessage(Message message) {
        if (Objects.isNull(message)) {
            log.warn("Null 메시지를 받았습니다. NodeId: {}", getId());
            return;
        }

        try {
            Object payload = message.getPayload();
            if (payload instanceof Number) {
                double inputValue = ((Number) payload).doubleValue();
                double mappedValue = mapValue(inputValue);

                if (constrainToTarget) {
                    mappedValue = Math.max(outputMin, Math.min(outputMax, mappedValue));
                }

                log.info("RangeNode[{}]: {}를 {}로 매핑", getId(), inputValue, mappedValue);
                Message outputMessage = new Message(mappedValue, message.getMetadata());
                emit(outputMessage);
            } else {
                log.error("RangeNode[{}]: 잘못된 페이로드 타입. Number가 예상되었으나 {}가 수신됨", getId(),
                        payload.getClass().getName());
            }
        } catch (Exception e) {
            log.error("RangeNode[{}]: 메시지 처리 중 오류", getId(), e);
        }
    }

    /**
     * 노드의 처리 로직을 실행합니다. 이 메서드는 노드가 시작되면 호출되며, 다음과 같은 순서로 동작합니다: 1. 노드를 시작 상태로 변경합니다. 2. 실행 중인 동안
     * 계속해서 메시지를 수신하고 처리합니다. 3. 노드가 중지되면 실행을 종료합니다.
     */
    @Override
    public void run() {
        start();
        while (getStatus() == NodeStatus.RUNNING) {
            Message message = receive();
            if (message != null) {
                onMessage(message);
            }
        }
        stop();
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
     * 주어진 값을 입력 범위에서 출력 범위로 매핑합니다. 이 메서드는 선형 보간법(linear interpolation)을 사용하여 값을 변환합니다.
     * 
     * 변환 과정: 1. 입력값에서 입력 범위의 최소값을 뺍니다: (value - inputMin) 2. 입력 범위의 크기로 나누어 정규화합니다: (value -
     * inputMin) / (inputMax - inputMin) 3. 출력 범위의 크기를 곱합니다: * (outputMax - outputMin) 4. 출력 범위의
     * 최소값을 더합니다: + outputMin
     *
     * @param value 매핑할 값
     * @return 매핑된 값. constrainToTarget이 true인 경우 출력 범위 내로 제한됩니다.
     */
    private double mapValue(double value) {
        return (value - inputMin) / (inputMax - inputMin) * (outputMax - outputMin) + outputMin;
    }

    /**
     * 범위 값을 검증합니다.
     *
     * @param inputMin 입력 최소값
     * @param inputMax 입력 최대값
     * @param outputMin 출력 최소값
     * @param outputMax 출력 최대값
     * @throws IllegalArgumentException 잘못된 범위 값인 경우
     */
    private void validateRange(double inputMin, double inputMax, double outputMin,
            double outputMax) {
        if (inputMin >= inputMax) {
            log.error("잘못된 입력 범위: [{} - {}]", inputMin, inputMax);
            throw new IllegalArgumentException("Input range is invalid");
        }

        if (outputMin >= outputMax) {
            log.error("잘못된 출력 범위: [{} - {}]", outputMin, outputMax);
            throw new IllegalArgumentException("Output range is invalid");
        }
    }

    public double getInputMin() {
        return inputMin;
    }

    public void setInputMin(double inputMin) {
        validateRange(inputMin, inputMax, outputMin, outputMax);
        this.inputMin = inputMin;
    }

    public double getInputMax() {
        return inputMax;
    }

    public void setInputMax(double inputMax) {
        validateRange(inputMin, inputMax, outputMin, outputMax);
        this.inputMax = inputMax;
    }

    public double getOutputMin() {
        return outputMin;
    }

    public void setOutputMin(double outputMin) {
        validateRange(inputMin, inputMax, outputMin, outputMax);
        this.outputMin = outputMin;
    }

    public double getOutputMax() {
        return outputMax;
    }

    public void setOutputMax(double outputMax) {
        validateRange(inputMin, inputMax, outputMin, outputMax);
        this.outputMax = outputMax;
    }

    public boolean isConstrainToTarget() {
        return constrainToTarget;
    }

    public void setConstrainToTarget(boolean constrainToTarget) {
        this.constrainToTarget = constrainToTarget;
    }
}
