package com.samsa;

import com.samsa.core.Flow;
import com.samsa.core.FlowPool;
import com.samsa.node.inout.ModbusToMqttNode;

import lombok.extern.slf4j.Slf4j;

/**
 * Modbus 데이터를 읽고 MQTT 브로커로 전송하는 클래스입니다.
 * 
 * <p>이 클래스는 Modbus 장치의 데이터를 읽어 JSON 형식으로 변환한 후, 
 * MQTT 브로커에 발행하는 역할을 수행합니다. 
 * Modbus 주소와 오프셋 정보를 설정 파일에서 로드하고, 
 * 데이터를 지속적으로 읽어 MQTT로 전송합니다.</p>
 */
@Slf4j
public class ModbusToMqttFlow{

    static Flow createFlow() {
        // 노드 생성
        ModbusToMqttNode node = null;
        try {
            node = new ModbusToMqttNode(1, "192.168.70.203", 502, "tcp://192.168.71.213:1883");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Flow 설정
        Flow flow = new Flow();
        flow.addNode(node);
        return flow;
    }

    public static void main(String[] args) {
        ModbusToMqttFlow modbusToMqttFlow = new ModbusToMqttFlow();
        Flow flow = modbusToMqttFlow.createFlow();
        FlowPool flow2 = new FlowPool();
        flow2.addFlow(flow);
        Thread thread = new Thread(flow2);
        thread.start();
    }
}
