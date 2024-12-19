package com.samsa.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * FlowPool 클래스는 여러 Flow 객체들을 관리하고 병렬로 실행하는 컨테이너입니다. 각 Flow는 독립적인 스레드에서 실행되며, FlowPool은 이들의 생명주기를
 * 관리합니다.
 * 
 * FlowPool에 등록된 Flow들은 각기 다른 스레드에서 병렬로 실행되어 비동기적으로 처리할 수 있습니다.
 * 
 * @author samsa
 * @version 1.1
 */
public class FlowPool implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(FlowPool.class);

    /** 실행할 Flow 객체들을 저장하는 리스트 */
    private final List<Flow> flows = new ArrayList<>();
    private List<Thread> flowThreads = new ArrayList<>();
    private volatile boolean running = true;

    /**
     * FlowPool에 새로운 Flow를 추가합니다.
     * 
     * @param flow 추가할 Flow 객체
     * @throws IllegalArgumentException flow가 null일 경우 예외 발생
     */
    public void addFlow(Flow flow) {
        if (flow == null) {
            logger.error("Flow 객체는 null일 수 없습니다.");
            throw new IllegalArgumentException("Flow 객체는 null일 수 없습니다.");
        }
        flows.add(flow);
        logger.info("Flow가 추가되었습니다. 현재 등록된 Flow 개수: {}", flows.size());
    }

    /**
     * FlowPool에 포함된 모든 Flow를 병렬로 실행합니다. 각 Flow는 독립적인 스레드에서 실행됩니다.
     * 
     * @throws IllegalStateException Flow가 하나도 등록되지 않았을 경우 예외 발생
     */
    @Override
    public void run() {
        if (flows.isEmpty()) {
            logger.error("실행할 Flow가 없습니다.");
            throw new IllegalStateException("실행할 Flow가 없습니다.");
        }

        logger.info("Flow들을 병렬로 실행합니다.");
        flows.forEach(flow -> {
            try {
                new Thread(flow).start();
                logger.info("Flow가 스레드에서 실행 중입니다.");
            } catch (Exception e) {
                logger.error("Flow 실행 중 오류가 발생했습니다. Flow ID");
            }
        });
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
                                                      