package com.samsa.node.inout;

import java.util.UUID;
import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;
import lombok.extern.slf4j.Slf4j;

/**
 * {@code RangeNode} 클래스는 입력 값을 주어진 범위에서 매핑하여 출력 범위로 변환하는 노드입니다.
 *
 * <p>입력 값이 {@code inputMin}과 {@code inputMax} 사이에 있는 경우 이를 비율에 따라
 * {@code outputMin}과 {@code outputMax} 사이의 값으로 매핑합니다. 또한, {@code constrainToTarget}이
 * {@code true}인 경우 매핑된 값이 출력 범위를 벗어나지 않도록 제한합니다.</p>
 *
 * <p>이 클래스는 메시지 처리 시스템에서 값 변환 및 범위 제한이 필요한 경우 유용하게 사용됩니다.</p>
 */
@Slf4j
public class RangeNode extends InOutNode {

    /** 입력 범위의 최소값. */
    private double inputMin;

    /** 입력 범위의 최대값. */
    private double inputMax;

    /** 출력 범위의 최소값. */
    private double outputMin;

    /** 출력 범위의 최대값. */
    private double outputMax;

    /** 매핑된 값이 출력 범위를 벗어나지 않도록 제한할지 여부. */
    private boolean constrainToTarget;

    /**
     * 입력 범위와 출력 범위를 지정하여 {@code RangeNode}를 생성합니다.
     *
     * @param inputMin         입력 범위의 최소값.
     * @param inputMax         입력 범위의 최대값.
     * @param outputMin        출력 범위의 최소값.
     * @param outputMax        출력 범위의 최대값.
     * @param constrainToTarget 매핑된 값이 출력 범위를 벗어나지 않도록 제한할지 여부.
     * @throws IllegalArgumentException 입력 범위 또는 출력 범위가 잘못된 경우 예외가 발생합니다.
     */
    public RangeNode(double inputMin, double inputMax, double outputMin, double outputMax, 
            boolean constrainToTarget) {
        validateRange(inputMin, inputMax, outputMin, outputMax);
        this.inputMin = inputMin;
        this.inputMax = inputMax;
        this.outputMin = outputMin;
        this.outputMax = outputMax;
        this.constrainToTarget = constrainToTarget;
    }

    /**
     * 지정된 UUID와 범위를 사용하여 {@code RangeNode}를 생성합니다.
     *
     * @param id              노드의 고유 식별자(UUID).
     * @param inputMin         입력 범위의 최소값.
     * @param inputMax         입력 범위의 최대값.
     * @param outputMin        출력 범위의 최소값.
     * @param outputMax        출력 범위의 최대값.
     * @param constrainToTarget 매핑된 값이 출력 범위를 벗어나지 않도록 제한할지 여부.
     * @throws IllegalArgumentException 입력 범위 또는 출력 범위가 잘못된 경우 예외가 발생합니다.
     */
    public RangeNode(UUID id, double inputMin, double inputMax, double outputMin, double outputMax, 
            boolean constrainToTarget) {
        super(id);
        validateRange(inputMin, inputMax, outputMin, outputMax);
        this.inputMin = inputMin;
        this.inputMax = inputMax;
        this.outputMin = outputMin;
        this.outputMax = outputMax;
        this.constrainToTarget = constrainToTarget;
    }

    /**
     * 메시지를 처리할 때 입력 값을 매핑하고, 매핑된 값을 메시지의 페이로드로 설정합니다.
     *
     * @param message 처리할 메시지; {@code null}일 수 없습니다.
     * <p>메시지의 페이로드는 {@code Number} 타입이어야 하며, 입력 범위에 따라 매핑됩니다.</p>
     */
    @Override
    protected void onMessage(Message message) {
        if (message == null || message.getPayload() == null) {
            log.error("메시지 또는 페이로드가 null입니다. NodeId: {}", getId());
            return;
        }

        Object payload = message.getPayload();
        if (!(payload instanceof Number)) {
            log.error("잘못된 페이로드 타입. NodeId: {}, Expected: Number, Actual: {}", 
                getId(), payload.getClass().getName());
            return;
        }

        try {
            double inputValue = ((Number) payload).doubleValue();
            double mappedValue = mapValue(inputValue);
            
            if (constrainToTarget) {
                mappedValue = Math.max(outputMin, Math.min(outputMax, mappedValue));
            }
            
            log.debug("값 매핑 완료. NodeId: {}, Input: {}, Output: {}", 
                getId(), inputValue, mappedValue);
            
            message.setPayload(mappedValue);
            super.onMessage(message);
            
        } catch (Exception e) {
            log.error("값 매핑 중 오류 발생. NodeId: {}, MessageId: {}", 
                getId(), message.getId(), e);
        }
    }

    /**
     * 주어진 값을 입력 범위에서 출력 범위로 매핑합니다.
     *
     * @param value 매핑할 입력 값.
     * @return 출력 범위로 매핑된 값.
     */
    private double mapValue(double value) {
        return (value - inputMin) / (inputMax - inputMin) * (outputMax - outputMin) + outputMin;
    }

    /**
     * 입력 및 출력 범위가 올바른지 확인합니다.
     *
     * @param inputMin  입력 범위의 최소값.
     * @param inputMax  입력 범위의 최대값.
     * @param outputMin 출력 범위의 최소값.
     * @param outputMax 출력 범위의 최대값.
     * @throws IllegalArgumentException 입력 또는 출력 범위가 잘못된 경우 예외가 발생합니다.
     */
    private void validateRange(double inputMin, double inputMax, double outputMin, double outputMax) {
        if (inputMin >= inputMax) {
            throw new IllegalArgumentException(
                String.format("잘못된 입력 범위: [%f - %f]", inputMin, inputMax));
        }
        if (outputMin >= outputMax) {
            throw new IllegalArgumentException(
                String.format("잘못된 출력 범위: [%f - %f]", outputMin, outputMax));
        }
    }


    // Getter and Setter methods
    public double getInputMin() { return inputMin; }
    public double getInputMax() { return inputMax; }
    public double getOutputMin() { return outputMin; }
    public double getOutputMax() { return outputMax; }
    public boolean isConstrainToTarget() { return constrainToTarget; }

    public void setInputMin(double inputMin) {
        validateRange(inputMin, this.inputMax, this.outputMin, this.outputMax);
        this.inputMin = inputMin;
    }

    public void setInputMax(double inputMax) {
        validateRange(this.inputMin, inputMax, this.outputMin, this.outputMax);
        this.inputMax = inputMax;
    }

    public void setOutputMin(double outputMin) {
        validateRange(this.inputMin, this.inputMax, outputMin, this.outputMax);
        this.outputMin = outputMin;
    }

    public void setOutputMax(double outputMax) {
        validateRange(this.inputMin, this.inputMax, this.outputMin, outputMax);
        this.outputMax = outputMax;
    }

    public void setConstrainToTarget(boolean constrainToTarget) {
        this.constrainToTarget = constrainToTarget;
    }
}
