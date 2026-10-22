package com.klapt.AI;

import java.util.List;

public class Sequence extends Node {
    public Sequence(List<Node> children) {super(children);}

    @Override
    public NodeState Evaluate() {
        boolean anyChildrenRunning = false;
        for(var node : children){
            switch(node.Evaluate()){
                case NodeState.FAILED:
                    state = NodeState.FAILED;
                    return state;
                case NodeState.SUCCESS:
                    continue;
                case NodeState.RUNNING:
                    anyChildrenRunning = true;
                    continue;
            }
        }

        state = anyChildrenRunning ? NodeState.RUNNING : NodeState.SUCCESS;
        return state;
    }
}
