package com.samsa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsa.core.Flow;
import com.samsa.core.Pipe;
import com.samsa.core.node.Node;
import com.samsa.core.node.InNode;
import com.samsa.core.node.OutNode;
import com.samsa.core.node.InOutNode;
import com.samsa.core.port.InPort;
import com.samsa.core.port.OutPort;
import com.samsa.node.in.DebugNode;
import com.samsa.node.inout.DelayNode;
import com.samsa.node.inout.FunctionNode;
import com.samsa.node.out.MqttInNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FlowLoader {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Flow loadFlowFromJson(String filePath) throws Exception {
        JsonNode root = mapper.readTree(new File(filePath));
        Flow flow = new Flow();

        Map<String, Node> nodeMap = new HashMap<>();

        // 노드 생성
        for (JsonNode nodeConfig : root.get("nodes")) {
            String type = nodeConfig.get("type").asText();
            String id = nodeConfig.get("id").asText();

            Node node = createNode(type, nodeConfig.get("properties"));
            nodeMap.put(id, node);
            flow.addNode(node);
        }

        // 연결 설정
        for (JsonNode connection : root.get("connections")) {
            String from = connection.get("from").asText();
            String to = connection.get("to").asText();

            OutPort outPort = getOutPort(nodeMap.get(from));
            InPort inPort = getInPort(nodeMap.get(to));

            if (outPort != null && inPort != null) {
                Pipe pipe = new Pipe();
                outPort.addPipe(pipe);
                inPort.addPipe(pipe);
            } else {
                throw new IllegalStateException("포트 연결 중 문제가 발생했습니다.");
            }
        }

        return flow;
    }

    private static Node createNode(String type, JsonNode properties) throws Exception {
        switch (type) {
            case "MqttInNode":
                return new MqttInNode(
                        properties.get("broker").asText(),
                        properties.get("clientId").asText(),
                        mapper.convertValue(properties.get("topics"), String[].class)
                );
            case "DelayNode":
                return new DelayNode(properties.get("delay").asInt());
            case "DebugNode":
                return new DebugNode();
            case "FunctionNode":
                return new FunctionNode(message -> {
                    // 예제 함수 구현
                    System.out.println("Payload: " + message.getPayload());
                });
            default:
                throw new IllegalArgumentException("Unsupported node type: " + type);
        }
    }

    private static OutPort getOutPort(Node node) {
        if (node instanceof OutNode) {
            return ((OutNode) node).getPort();
        } else if (node instanceof InOutNode) {
            return ((InOutNode) node).getOutPort();
        }
        return null;
    }

    private static InPort getInPort(Node node) {
        if (node instanceof InNode) {
            return ((InNode) node).getPort();
        } else if (node instanceof InOutNode) {
            return ((InOutNode) node).getInPort();
        }
        return null;
    }
}
