package com.klapt.skill;

import com.klapt.movement.Move;
import lombok.Getter;

@Getter
public class Skill extends Move{
    String effect, condition;
    float chance; //100f
    int duration;
    int cost;

    @Override
    public int getWeight() {
        return Math.round(getChance());
    }
}
