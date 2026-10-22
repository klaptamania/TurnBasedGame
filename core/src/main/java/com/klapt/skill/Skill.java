package com.klapt.skill;

import com.klapt.interfacePack.Gacha;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Skill implements Gacha {
    String name, effect, condition;
    float chance; //100f
    float skillCastSpeed, skillCastNeed, skillRecoverTime;
    int duration;
    int damage;
    int cost;
    int cooldown, cdLeft = 0;

    @Setter
    boolean used = false;


    public int getCooldown() {
        return cooldown + 1;
    }


    public void startCD() {
        cdLeft = cooldown + 1;
    }

    public void onCD() {
        if (!used) return;

        cdLeft--;
        if (cdLeft == 0) {
            used = false;
        }
    }

    @Override
    public int getWeight() {
        return Math.round(getChance());
    }
}
