package com.samsa;

import com.samsa.core.Flow;
import com.samsa.core.FlowPool;
import com.samsa.core.node.InNode;
public class FlowTest {
    public static void main(String[] args) {
        try {
            FlowPool flowPool = new FlowPool();
            Flow flow = FlowLoader.loadFlowFromJson(
                    "/home/nhnacademy/Desktop/Samsa/Node-Blue/nodeblue/src/main/resources/flow.json"
            );

            flowPool.addFlow(flow);
            flowPool.run();
            System.out.println("Flow execution completed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
