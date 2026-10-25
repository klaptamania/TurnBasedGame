package com.klapt.entity;

import com.klapt.movement.Move;
import lombok.Getter;

@Getter
public class Enemy extends Entity {
    Move[] moves;

    public Enemy() {
        super();
    }


}
