package com.samsa;

import com.samsa.core.Flow;
import com.samsa.core.Pipe;
import com.samsa.node.in.DebugNode;
import com.samsa.node.inout.FunctionNode;
import com.samsa.node.out.ModbusNode;

public class Main {

    public static void main(String[] args) {
        ModbusNode modbusNode = new ModbusNode("192.168.70.203", 502, 1, 0, 5);
        DebugNode debugNode = new DebugNode();
        FunctionNode functionNode = new FunctionNode(message -> {
            message.setPayload(message.getPayload() + "afadfadfadf");
        });

        Pipe modbusToFunction = new Pipe();
        Pipe functionToDebug = new Pipe();

        Flow flow = new Flow();
        
        modbusNode.getPort().addPipe(modbusToFunction);
        functionNode.getInPort().addPipe(modbusToFunction);

        functionNode.getOutPort().addPipe(functionToDebug);
        debugNode.getPort().addPipe(functionToDebug);

        flow.addNode(debugNode);
        flow.addNode(functionNode);
        flow.addNode(modbusNode);

        new Thread(flow).start();
    }
}
