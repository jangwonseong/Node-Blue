package com.samsa;

import java.util.ArrayList;
import java.util.List;

import com.samsa.core.Flow;
import com.samsa.core.Pipe;
import com.samsa.core.node.InOutNode;
import com.samsa.core.node.Node;
import com.samsa.core.node.OutNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlowStreamAnalyzer {
    
    public static class StreamInfo {
        private final String nodeId;
        private final List<String> inputStreams = new ArrayList<>();
        private final List<String> outputStreams = new ArrayList<>();
        
        public StreamInfo(String nodeId) {
            this.nodeId = nodeId;
        }
        
        public void addInputStream(String pipeId) {
            inputStreams.add(pipeId);
        }
        
        public void addOutputStream(String pipeId) {
            outputStreams.add(pipeId);
        }
        
        @Override
        public String toString() {
            return String.format("Node[%s] - Inputs: %s, Outputs: %s", 
                nodeId, inputStreams, outputStreams);
        }
    }
    
    public List<StreamInfo> analyzeFlow(Flow flow) {
        List<StreamInfo> streamInfos = new ArrayList<>();
        
        for (Node node : flow.getNodes()) {
            StreamInfo info = new StreamInfo(node.getId().toString());
            
            // 입력 스트림 분석
            if (node instanceof InOutNode) {
                InOutNode inOutNode = (InOutNode) node;
                for (Pipe pipe : inOutNode.getInPort().getPipes()) {
                    info.addInputStream(pipe.getId().toString());
                }
                for (Pipe pipe : inOutNode.getOutPort().getPipes()) {
                    info.addOutputStream(pipe.getId().toString());
                }
            }
            
            // 출력 전용 노드 분석
            if (node instanceof OutNode) {
                OutNode outNode = (OutNode) node;
                for (Pipe pipe : outNode.getPort().getPipes()) {
                    info.addOutputStream(pipe.getId().toString());
                }
            }
            
            streamInfos.add(info);
        }
        
        return streamInfos;
    }
    
    public void printFlowStructure(Flow flow) {
        List<StreamInfo> streamInfos = analyzeFlow(flow);
        log.info("=== Flow Structure Analysis ===");
        for (StreamInfo info : streamInfos) {
            log.info(info.toString());
        }
    }
}
