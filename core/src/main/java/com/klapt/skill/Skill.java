package com.klapt.skill;

import com.klapt.movement.Move;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Skill extends Move{
    String effect, condition;
    float chance; //100f
    int duration;
    int cost;

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
