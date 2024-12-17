package com.samsa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsa.core.Flow;
import com.samsa.core.FlowPool;
import com.samsa.core.Pipe;
import com.samsa.node.in.DebugNode;
import com.samsa.node.inout.DelayNode;
import com.samsa.node.inout.FunctionNode;
import com.samsa.node.out.ModbusNode;
import com.samsa.node.out.MqttInNode;

public class Main {

    public static void main(String[] args) {
        FlowPool flowPool = new FlowPool();

        MqttInNode mqttInNode =
                new MqttInNode("tcp://192.168.70.203:1883", "odngodng", new String[] {"data/#"});
        DelayNode delayNode = new DelayNode(1000);
        DebugNode debugNode = new DebugNode();
        FunctionNode functionNode = new FunctionNode(message -> {
            Map<String, Object> newPayload = new HashMap<>();
            Map<String, String> tagMap = new HashMap<>();

            String payload = message.getPayload().toString().substring(1,
                    message.getPayload().toString().length() - 1);

            String topic = payload.split(",", 2)[0];
            String data = payload.split(",", 2)[1].trim();

            if (topic.contains("lora") || topic.contains("power_meter")) {
                return;
            }

            tagMap.put("deviceName", topic.split("/")[10]);
            newPayload.put("measurement", "sensor");
            newPayload.put("tags", tagMap);
            newPayload.put("field", Double.parseDouble(data.split(",")[1].split(":")[1].substring(0,
                    data.split(",")[1].split(":")[1].length() - 1)));
            newPayload.put("time", Integer.parseInt(data.split(",")[0].split(":")[1]));

            message.setPayload(newPayload);
        });

        Pipe mqttInToDelay = new Pipe();
        Pipe delayToFunction = new Pipe();
        Pipe functionToDebug = new Pipe();

        mqttInNode.getPort().addPipe(mqttInToDelay);
        delayNode.getInPort().addPipe(mqttInToDelay);

        delayNode.getOutPort().addPipe(delayToFunction);
        functionNode.getInPort().addPipe(delayToFunction);

        functionNode.getOutPort().addPipe(functionToDebug);
        debugNode.getPort().addPipe(functionToDebug);

        Flow mqttFlow = new Flow();

        mqttFlow.addNode(debugNode);
        mqttFlow.addNode(mqttInNode);
        mqttFlow.addNode(delayNode);
        mqttFlow.addNode(functionNode);

        flowPool.addFlow(mqttFlow);


        flowPool.run();
    }
} 
