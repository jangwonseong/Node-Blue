package com.samsa.core;

import java.util.ArrayList;
import java.util.List;

public class FlowPool implements Runnable {
    private List<Flow> flows = new ArrayList<>();

    public void addFlow(Flow flow) {
        flows.add(flow);
    }

    @Override
    public void run() {
        flows.forEach(flow -> new Thread(flow).start());
    }
}
