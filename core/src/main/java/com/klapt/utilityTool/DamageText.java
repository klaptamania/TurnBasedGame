package com.klapt.utilityTool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.klapt.main.GameScreen;
import com.klapt.rendering.TextDamageRenderer;

public class DamageText {
    String text;
    float x,y, vx,vy;
    Color color;

    public enum TypeText {NORMAL, CRIT, HEAL, BLEED, PARALYZE, IGNITED, SEVERED_BLEED, PHYSICAL};

    public DamageText(String text, float x, float y, String hex) {
        this.text = text;
        this.x = x;
        this.y = y;
        color = Color.valueOf(hex);

        this.vx = MathUtils.random(-60f, 60f);
        this.vy = MathUtils.random(80f, 120f);
    }

    float alpha = 1.f;
    float lifeTime = 1f;

    public void update(float deltaTime){
        alpha -= deltaTime / lifeTime;

        x += vx * deltaTime;
        y += vy * deltaTime;
        vy -= 200 * deltaTime;

        if(alpha < 0) alpha = 0;
    }

    public void draw(SpriteBatch batch, BitmapFont font){
        Color old = batch.getColor();

        color.a = alpha;

        font.setColor(color);

        font.draw(batch, text, x, y);
        font.setColor(old);
    }

    public static void createDamageText(int damage, DamageText.TypeText type, float x, float y){
        String color = switch (type) {
            case TypeText.NORMAL -> "#ffffff"; //White
            case TypeText.CRIT -> "#ff0000"; //Red
            case TypeText.HEAL -> "#0dff00"; //Green
            case TypeText.BLEED -> "#420000"; //Dark Red
            case TypeText.IGNITED -> "#ffba3c"; //Orange
            case TypeText.PARALYZE -> "#00FFFF"; //Light blue
            case TypeText.SEVERED_BLEED -> "#420000"; //Light blue
            case TypeText.PHYSICAL -> "#3f3f3f"; //Light blue
        };
        TextDamageRenderer.damageTexts.add(new DamageText(Integer.toString(damage), x, y, color));
    }

    public boolean done() { return alpha <= 0;}
}
