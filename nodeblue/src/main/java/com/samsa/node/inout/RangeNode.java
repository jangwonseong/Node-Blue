package com.samsa.node.inout;

import java.util.UUID;
import com.samsa.core.Message;
import com.samsa.core.node.InOutNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RangeNode extends InOutNode {
    private double inputMin;
    private double inputMax;
    private double outputMin;
    private double outputMax;
    private boolean constrainToTarget;

    public RangeNode(double inputMin, double inputMax, double outputMin, double outputMax, 
            boolean constrainToTarget) {
        validateRange(inputMin, inputMax, outputMin, outputMax);
        this.inputMin = inputMin;
        this.inputMax = inputMax;
        this.outputMin = outputMin;
        this.outputMax = outputMax;
        this.constrainToTarget = constrainToTarget;
    }

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

    private double mapValue(double value) {
        return (value - inputMin) / (inputMax - inputMin) * (outputMax - outputMin) + outputMin;
    }

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
