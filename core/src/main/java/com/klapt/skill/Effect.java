package com.klapt.skill;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.klapt.main.GameScreen;
import com.klapt.utilityTool.AnimationHolder;
import com.klapt.utilityTool.DamageText;

public class Effect {
    protected final float x;
    protected final float y;
    protected float stateTime = 0f; // Mỗi Effect tự quản lý thời gian của chính nó!
    protected boolean finished = false;
    protected String type;

    public Effect(float x, float y, String type){
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public boolean isFinished() { return finished;}

    public void update(float deltaTime) {
        if(finished) return;

        stateTime += deltaTime;

        Animation<TextureRegion> anim = switch (type){
            case "BLEED" -> AnimationHolder.get("BLEED"); //MrBEast
            case "SEVERED_BLEED" -> AnimationHolder.get("SEVERED_BLEED");
            case "FIRE_BALL" -> AnimationHolder.get("FIRE_BALL");
            case "DODGE" -> AnimationHolder.get("DODGE");
            default -> AnimationHolder.get("BLEED");
        };



        if (anim.isAnimationFinished(stateTime)) {
            finished = true;
        }
    }

    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = AnimationHolder.get(type).getKeyFrame(stateTime);
        batch.draw(currentFrame, x, y, GameScreen.imageTile, GameScreen.imageTile);
    }
}
