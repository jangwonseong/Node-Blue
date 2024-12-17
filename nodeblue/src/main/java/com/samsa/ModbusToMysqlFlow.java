package com.samsa;

import com.samsa.core.Flow;
import com.samsa.core.FlowPool;
import com.samsa.core.Pipe;
import com.samsa.node.in.DebugNode;
import com.samsa.node.in.MySqlNode;
import com.samsa.node.inout.DelayNode;
import com.samsa.node.inout.FunctionNode;
import com.samsa.node.out.ModbusNode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
public class ModbusToMysqlFlow {
    public static void main(String[] args) {
        FlowPool flowPool = new FlowPool();
        
        // 노드 생성
        ModbusNode modbusNode = new ModbusNode("192.168.70.203", 502, 1, 0, 2);
        DelayNode delayNode = new DelayNode(1000);
        DebugNode debugNode = new DebugNode();
        
        // MySQL 노드 생성
        MySqlNode mysqlNode = new MySqlNode(
            "com.mysql.cj.jdbc.Driver",
            "jdbc:mysql://192.168.71.213:3306/IOT",
            "root",
            "P@ssw0rd",
            "INSERT INTO modbus (modbus_id, modbus_name, value, timestamp) VALUES (1, 'ModbusDevice-203', ?, NOW())"
        );

        // MQTT 브로커 노드 생성
        MqttBrokerNode brokerNode = new MqttBrokerNode(
            "tcp://192.168.70.213:1883", 
            "502",
            "modbus/data"
        );

        // 파이프 생성 및 연결
        Pipe modbusToDelay = new Pipe();
        Pipe delayToBroker = new Pipe();
        Pipe brokerToMysql = new Pipe();
        Pipe mysqlToDebug = new Pipe();

        // Modbus -> Delay -> Broker
        modbusNode.getPort().addPipe(modbusToDelay);
        delayNode.getInPort().addPipe(modbusToDelay);
        delayNode.getOutPort().addPipe(delayToBroker);
        brokerNode.getInPort().addPipe(delayToBroker);
        
        // Broker -> MySQL -> Debug
        brokerNode.getOutPort().addPipe(brokerToMysql);
        mysqlNode.getPort().addPipe(brokerToMysql);
        mysqlNode.getPort().addPipe(mysqlToDebug);
        debugNode.getPort().addPipe(mysqlToDebug);

        // Flow 설정
        Flow flow = new Flow();
        flow.addNode(modbusNode);
        flow.addNode(delayNode);
        flow.addNode(brokerNode);
        flow.addNode(mysqlNode);
        flow.addNode(debugNode);
        
        // Flow를 JSON 파일로 저장
        FlowWriter.writeFlowToJson(flow, "outputFlow.json");

        // Flow 실행
        flowPool.addFlow(flow);
        flowPool.run();
    }
}
