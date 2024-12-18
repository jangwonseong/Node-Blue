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

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FlowLoaderReflection {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<String, String> NODE_PACKAGE_MAP = loadPackageMapping();
    private static final Map<String, Class<?>> NODE_CACHE = new HashMap<>();

    private FlowLoaderReflection() {
        throw new UnsupportedOperationException("FlowLoader cannot be instantiated");
    }

    public static Flow loadFlowFromJson(String filePath) {
        try {
            JsonNode root = mapper.readTree(new File(filePath));
            validateFlowStructure(root);
            return createFlow(root);
        } catch (Exception e) {
            log.error("Flow 로딩 중 오류 발생: {}", e.getMessage(), e);
            throw new FlowLoadException("Flow를 로드할 수 없습니다", e);
        }
    }

    private static void validateFlowStructure(JsonNode root) {
        if (!root.has("nodes") || !root.has("connections")) {
            throw new IllegalArgumentException("유효하지 않은 Flow 구조입니다");
        }
    }

    private static Flow createFlow(JsonNode root) throws Exception {
        Flow flow = new Flow();
        Map<String, Node> nodeMap = createNodes(root.get("nodes"), flow);
        createConnections(root.get("connections"), nodeMap);
        return flow;
    }

    private static Map<String, Node> createNodes(JsonNode nodesConfig, Flow flow) {
        Map<String, Node> nodeMap = new HashMap<>();
        for (JsonNode nodeConfig : nodesConfig) {
            Node node = createNodeWithReflection(nodeConfig);
            nodeMap.put(nodeConfig.get("id").asText(), node);
            flow.addNode(node);
        }
        return nodeMap;
    }

    private static Node createNodeWithReflection(JsonNode nodeConfig) {
        try {
            String type = nodeConfig.get("type").asText();
            Class<?> nodeClass = getNodeClass(type);
            JsonNode properties = nodeConfig.get("properties");
    
            // Add validation before conversion
            if (!Node.class.isAssignableFrom(nodeClass)) {
                throw new FlowLoadException("Invalid node class: " + nodeClass.getName());
            }
    
            return (Node) mapper.convertValue(properties, nodeClass);
        } catch (Exception e) {
            throw new FlowLoadException("Node creation failed: " + e.getMessage(), e);
        }
    }
    

    private static Class<?> getNodeClass(String type) throws ClassNotFoundException {
        if (NODE_CACHE.containsKey(type)) {
            return NODE_CACHE.get(type);
        }

        String packagePath = NODE_PACKAGE_MAP.getOrDefault(type, "com.samsa.node.default");
        String className = String.format("%s.%s", packagePath, type);
        Class<?> nodeClass = Class.forName(className);
        NODE_CACHE.put(type, nodeClass);
        return nodeClass;
    }

    private static Map<String, String> loadPackageMapping() {
        // Consider loading from configuration file
        return Map.of(
            "MqttInNode", "com.samsa.node.out",
            "InNode", "com.samsa.node.in",
            "OutNode", "com.samsa.node.out",
            "DelayNode", "com.samsa.node.inout",
            "DebugNode", "com.samsa.node.in",
            "FunctionNode", "com.samsa.node.inout"
        );
    }
    

    private static void createConnections(JsonNode connections, Map<String, Node> nodeMap) {
        for (JsonNode connection : connections) {
            String from = connection.get("from").asText();
            String to = connection.get("to").asText();

            try {
                connectNodes(nodeMap.get(from), nodeMap.get(to));
            } catch (Exception e) {
                log.warn("노드 연결 실패: from = {}, to = {}. 오류: {}", from, to, e.getMessage());
            }
        }
    }

    private static void connectNodes(Node fromNode, Node toNode) {
        if (fromNode == null || toNode == null) {
            throw new IllegalArgumentException("Nodes cannot be null");
        }
    
        OutPort outPort = getOutPort(fromNode);
        InPort inPort = getInPort(toNode);
    
        if (outPort == null || inPort == null) {
            throw new IllegalStateException(
                String.format("Invalid port configuration: from=%s, to=%s", 
                    fromNode.getId(), toNode.getId())
            );
        }
    
        Pipe pipe = new Pipe();
        outPort.addPipe(pipe);
        inPort.addPipe(pipe);
    }
    

    private static OutPort getOutPort(Node node) {
        if (node instanceof OutNode outNode) {
            return outNode.getPort();
        } else if (node instanceof InOutNode inOutNode) {
            return inOutNode.getOutPort();
        }
        return null;
    }

    private static InPort getInPort(Node node) {
        if (node instanceof InNode inNode) {
            return inNode.getPort();
        } else if (node instanceof InOutNode inOutNode) {
            return inOutNode.getInPort();
        }
        return null;
    }
}
                          