package com.klapt.AI;

import java.util.function.Supplier;

public class ActionNode extends Node{
    private final Supplier<NodeState> action;

    public ActionNode(Supplier<NodeState> action) {this.action = action; }

    @Override
    public NodeState Evaluate() {
        return action != null ? action.get() : NodeState.FAILED;
    }
}
