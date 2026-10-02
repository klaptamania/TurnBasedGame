package com.klapt.skill;

import com.klapt.interfacePack.Gacha;

public class Skill implements Gacha {
    String name, effect, condition;
    float chance; //100f
    int duration;

    int damage;

    int cost;

    boolean used = false;
    int cooldown, cdLeft = 0;

    //Getter

    public String getName() {
        return name;
    }

    public String getEffect() {
        return effect;
    }

    public int getDamage() {
        return damage;
    }

    public float getChance() {
        return chance;
    }

    public String getCondition() {
        return condition;
    }

    public int getDuration() {
        return duration;
    }

    public int getCooldown() {
        return cooldown;
    }

    public boolean isUsed() {
        return used;
    }

    public int getCdLeft() {
        return cdLeft;
    }

    public int getCost() {
        return  cost;
    }

    public void setUsed(boolean used) { this.used = used;}

    //

    public void startCD() { cdLeft = cooldown; }

    public void onCD() {
        if(!used) return;

        cdLeft--;
        if(cdLeft == 0){
            used = false;
        }
    }

    @Override
    public int getWeight() {
        return Math.round(getChance());
    }
}
