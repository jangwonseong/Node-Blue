package com.samsa.core;

import java.util.ArrayList;
import java.util.List;
import com.samsa.core.node.Node;

public class Flow implements Runnable {
    private List<Node> nodes = new ArrayList<>();

    public void addNode(Node node) {
        nodes.add(node);
    }

    @Override
    public void run() {
        nodes.forEach(node -> new Thread(node).start());
    }
}
