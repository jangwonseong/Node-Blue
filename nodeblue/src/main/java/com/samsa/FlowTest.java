package com.samsa;

import com.samsa.core.Flow;
import com.samsa.core.FlowPool;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlowTest {
    public static void main(String[] args) {
        try {
            FlowPool flowPool = new FlowPool();
            Flow flow = FlowLoader.loadFlowFromJson(
                    "/home/nhnacademy/Desktop/Samsa/Node-Blue/nodeblue/src/main/resources/flow.json"
            );

            flowPool.addFlow(flow);
            flowPool.run();
            log.info("Flow execution completed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
