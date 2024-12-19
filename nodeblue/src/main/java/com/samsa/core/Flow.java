package com.samsa.core;

import java.util.ArrayList;
import java.util.List;
import com.samsa.core.node.Node;

/**
 * Flow 클래스는 노드들의 실행 흐름을 관리하는 컨테이너입니다. 여러 노드들을 추가하고 병렬로 실행할 수 있는 기능을 제공합니다.
 * 
 * 각 노드는 독립적인 스레드에서 실행되며, Flow는 이러한 노드들의 생명주기를 관리합니다.
 *
 * @author samsa
 * @version 1.0
 */
public class Flow implements Runnable {
    /** 실행할 노드들의 목록 */
    private final List<Node> nodes = new ArrayList<>();

    /**
     * 현재 Flow에 포함된 모든 노드를 반환합니다.
     *
     * @return 노드 목록
     */
    public List<Node> getNodes() {
        return List.copyOf(nodes);
    }

    /**
     * Flow에 새로운 노드를 추가합니다.
     *
     * @param node 추가할 노드
     * @throws IllegalArgumentException 노드가 null인 경우
     */
    public void addNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("노드는 null일 수 없습니다.");
        }
        nodes.add(node);
    }

    /**
     * Flow에 포함된 모든 노드를 병렬로 실행합니다. 각 노드는 독립적인 스레드에서 실행됩니다.
     */
    @Override
    public void run() {
        for (Node node : nodes) {
            new Thread(node).start();
        }
    }
}
