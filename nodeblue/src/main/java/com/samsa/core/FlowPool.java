package com.samsa.core;

import java.util.ArrayList;
import java.util.List;

/**
 * FlowPool 클래스는 여러 Flow들을 관리하고 실행하는 상위 컨테이너입니다.
 * 복수의 Flow를 등록하고 이들을 병렬로 실행할 수 있는 기능을 제공합니다.
 * 
 * 각 Flow는 독립적인 스레드에서 실행되며, FlowPool은 이러한 Flow들의
 * 생명주기를 관리합니다.
 *
 * @author samsa
 * @version 1.0
 */
public class FlowPool implements Runnable {
    /** 실행할 Flow들의 목록 */
    private List<Flow> flows = new ArrayList<>();

    /**
     * FlowPool에 새로운 Flow를 추가합니다.
     *
     * @param flow 추가할 Flow
     */
    public void addFlow(Flow flow) {
        flows.add(flow);
    }

    /**
     * FlowPool에 포함된 모든 Flow를 병렬로 실행합니다.
     * 각 Flow는 독립적인 스레드에서 실행됩니다.
     */
    @Override
    public void run() {
        flows.forEach(flow -> new Thread(flow).start());
    }
}
