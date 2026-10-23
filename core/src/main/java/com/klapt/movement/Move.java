package com.klapt.movement;

import com.klapt.interfacePack.Gacha;

@lombok.Data
public class Move implements Gacha {
    protected String name;
    protected int moveWeight;
    protected int damage;
    protected float skillCastSpeed, skillCastNeed, skillRecoverTime;
    protected int cooldown, cdLeft = 0;

    public Move(int moveWeight){
        this.moveWeight = moveWeight;
    }

    public Move(){}

    @Override
    public int getWeight() {
        return moveWeight;
    }
}
