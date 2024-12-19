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
    private List<Flow> flows = new ArrayList<>();
    private List<Thread> flowThreads = new ArrayList<>();
    private volatile boolean running = true;

    public void addFlow(Flow flow) {
        flows.add(flow);
    }

    @Override
    public void run() {
        flowThreads.clear();
        running = true;
        
        flows.forEach(flow -> {
            Thread thread = new Thread(flow);
            flowThreads.add(thread);
            thread.start();
        });
    }

    /**
     * FlowPool에 포함된 모든 Flow의 실행을 중지합니다.
     * 각 Flow의 스레드를 인터럽트하고 리소스를 정리합니다.
     */
    public void stop() {
        running = false;
        flowThreads.forEach(Thread::interrupt);
        flowThreads.clear();
        flows.clear();
    }
}
                                                      