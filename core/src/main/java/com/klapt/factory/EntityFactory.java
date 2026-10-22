package com.klapt.factory;

import com.badlogic.gdx.Gdx;
import com.klapt.entity.Enemy;
import com.klapt.entity.Entity;
import com.klapt.main.GameScreen;

public class EntityFactory {
    public static Entity getEntityFromJson(String filePath, int index, boolean isAlly){
        Entity entity = isAlly ? GameScreen.json.fromJson(Entity.class, Gdx.files.internal(filePath)) : GameScreen.json.fromJson(Enemy.class, Gdx.files.internal(filePath));
        entity.load(index);
        return entity;
    }
}
