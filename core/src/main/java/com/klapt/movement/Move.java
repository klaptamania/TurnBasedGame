package com.klapt.movement;

import com.klapt.interfacePack.Gacha;

public class Move implements Gacha {
    String name;
    int moveWeight;

    public Move(int moveWeight){
        this.moveWeight = moveWeight;
    }

    public Move(){}

    public void setMoveWeight(int amount) { moveWeight = amount; }

    public String getName() { return name; }

    @Override
    public int getWeight() {
        return moveWeight;
    }
}
