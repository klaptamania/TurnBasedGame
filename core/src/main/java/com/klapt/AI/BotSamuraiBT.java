package com.klapt.AI;

import com.klapt.entity.Enemy;

public class BotSamuraiBT {
    Enemy bot;
    private Node nodeRoot;

    public BotSamuraiBT(Enemy bot) {
        this.bot = bot;
    }

    private void Construct(){
        Node checkHit = new ActionNode(this::beingHit);
        Node increaseChance = new ActionNode(this::IncreaseChance);
    }

    //Action Node
    private Node.NodeState beingHit(){
        return bot.isBeingHit() ? Node.NodeState.SUCCESS : Node.NodeState.FAILED;
    }

    private Node.NodeState IncreaseChance(){
        return Node.NodeState.RUNNING;
    }

}
