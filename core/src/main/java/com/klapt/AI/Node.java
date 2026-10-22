package com.klapt.AI;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    public enum NodeState { SUCCESS, RUNNING, FAILED};

    protected NodeState state;
    protected List<Node> children = new ArrayList<>();

    public Node() {}
    public Node(List<Node> children){
        this.children = children;
    }

    public abstract  NodeState Evaluate();
}
