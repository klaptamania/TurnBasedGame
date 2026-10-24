package com.klapt.movement;

import com.klapt.interfacePack.Gacha;
import lombok.Setter;

@lombok.Data
public class Move implements Gacha {
    protected String name;
    protected int moveWeight;
    protected int damage;
    protected float skillCastSpeed, skillCastNeed, skillRecoverTime;
    protected int cooldown, cdLeft = 0;

    @Setter
    boolean used = false;

    public Move(int moveWeight){
        this.moveWeight = moveWeight;
    }

    public Move(){}

    @Override
    public int getWeight() {
        return moveWeight;
    }

    public void onCD() {
        if (!used) return;

        cdLeft--;
        if (cdLeft == 0) {
            used = false;
        }
    }
    public void startCD() {
        cdLeft = cooldown + 1;
    }
}
