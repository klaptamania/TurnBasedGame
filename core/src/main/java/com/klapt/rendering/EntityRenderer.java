package com.klapt.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.klapt.entity.Entity;
import com.klapt.entity.EntityTexture;
import com.klapt.equipment.Item;
import com.klapt.main.GameScreen;
import com.klapt.main.Main;
import com.klapt.skill.Effect;

public class EntityRenderer {
    public static void draw(SpriteBatch batch, Entity entity, float x, float y, float deltaTime){

        if(!entity.isAlive()){
            batch.draw(Main.getAm().getTexture("Bone"), x, y, GameScreen.imageTile, GameScreen.imageTile);
        }else{
            if(entity.isBeingHit()){
                entity.addHurtCounter(deltaTime);
                x += MathUtils.random(-10,10);

                if(entity.getHurtCounter() >= 0.25f){
                    entity.addHurtCounter(-0.25f);
                    entity.setBeingHit(false);
                }
            }

            EntityTexture texture = entity.getTexture();
            if(entity.getTexture() != null) batch.draw(texture.getTexture(), x, y, GameScreen.imageTile, GameScreen.imageTile);

            for(Item i : texture.getArmor()){
                if(i != null){
                    if(i.getType().contentEquals("Head") && i.getName().contentEquals("Wizard Hat")){
                        batch.draw(i.getTexture(), x, y, GameScreen.imageTile, GameScreen.imageTile + 16);
                        continue;
                    }
                    batch.draw(i.getTexture(), x, y, GameScreen.imageTile, GameScreen.imageTile);
                }
            }

            if(texture.getWeapon() != null) batch.draw(texture.getWeapon(), x, y, GameScreen.imageTile, GameScreen.imageTile);


            if(batch.getColor() != Color.WHITE) batch.setColor(Color.WHITE);

            if(entity.isAttackAnimation()){
                entity.setStateCounter(entity.getStateCounter() + deltaTime);
                drawBasicAttack(batch, entity);
            }

            drawHealthBar(batch, entity);
        }
    }

    public static void drawHealthBar(SpriteBatch batch, Entity entity){
        float barWidth = GameScreen.imageTile / 2f;
        float barHeight = 42;

        float hpPercent = MathUtils.clamp((float) entity.getHp() / entity.getMaxHp(), 0f, 1f);

        float currentX = entity.getX() + barWidth /2f;

        batch.draw(Main.getAm().getTexture("HealthGrayBar"), currentX, entity.getY() - 25, barWidth, barHeight);
        batch.draw(Main.getAm().getTexture("HealthBar"), currentX, entity.getY() - 25, barWidth * hpPercent, barHeight);

    }

    public static void dodgeAnimation(Entity entity) { EffectRenderer.effectsAnim.add(new Effect(entity.getX() , entity.getY(), "DODGE"));}

    public static void drawBasicAttack(SpriteBatch batch, Entity entity){
        TextureRegion currentFrame = entity.getBasicAttack().getKeyFrame(entity.getStateCounter());
        batch.draw(currentFrame, entity.getX(), entity.getY(), GameScreen.imageTile - 20 ,GameScreen.imageTile - 20);

        if(entity.getStateCounter() >= 0.775f) {
            entity.setStateCounter(0);
            entity.setAttackAnimation(false);
        }
    }
}
