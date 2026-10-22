package com.klapt.AI;

import java.util.List;

public class Selector extends Node{
    public Selector(List<Node> children) {super(children);}

    @Override
    public NodeState Evaluate() {
        for(var node : children){
            switch(node.Evaluate()){
                case NodeState.FAILED:
                    continue;
                case NodeState.SUCCESS:
                    state = NodeState.SUCCESS;
                    return state;
                case NodeState.RUNNING:
                    state = NodeState.RUNNING;
                    return state;
            }
        }
        state = NodeState.FAILED;
        return state;
    }
}
