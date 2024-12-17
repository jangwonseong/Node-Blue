package com.samsa;

import com.samsa.core.Flow;
import com.samsa.core.FlowPool;
import com.samsa.core.Pipe;
import com.samsa.node.in.DebugNode;
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

        // MySQL 연결을 처리할 FunctionNode 생성
        FunctionNode mysqlFunction = new FunctionNode(message -> {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://192.168.71.213:3306/IOT",
                    "root",
                    "P@ssw0rd"
                );
                
                String sql = "INSERT INTO modbus_data (value, timestamp) VALUES (?, NOW())";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setShort(1, (Short)message.getPayload());
                stmt.executeUpdate();
                
                stmt.close();
                conn.close();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 파이프 생성 및 연결
        Pipe modbusToDelay = new Pipe();
        Pipe delayToMysql = new Pipe();
        Pipe mysqlToDebug = new Pipe();

        modbusNode.getPort().addPipe(modbusToDelay);
        delayNode.getInPort().addPipe(modbusToDelay);
        
        delayNode.getOutPort().addPipe(delayToMysql);
        mysqlFunction.getInPort().addPipe(delayToMysql);
        
        mysqlFunction.getOutPort().addPipe(mysqlToDebug);
        debugNode.getPort().addPipe(mysqlToDebug);

        // Flow 설정
        Flow flow = new Flow();
        flow.addNode(modbusNode);
        flow.addNode(delayNode);
        flow.addNode(mysqlFunction);
        flow.addNode(debugNode);

        flowPool.addFlow(flow);
        flowPool.run();
    }
}
