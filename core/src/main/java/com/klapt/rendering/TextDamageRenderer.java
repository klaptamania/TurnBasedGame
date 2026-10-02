package com.klapt.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.klapt.utilityTool.DamageText;

import java.util.ArrayList;
import java.util.List;

public class TextDamageRenderer {
    public static List<DamageText> damageTexts = new ArrayList<>();
    static List<DamageText> garbageTextCollector = new ArrayList<>();
    private static final BitmapFont font = new BitmapFont();

    public static void loadFont(Color color){
        font.setColor(color);
    }


    public static void textDrawer(SpriteBatch batch) {
        if(damageTexts.isEmpty()) return;

        for(DamageText dt : damageTexts){
            dt.draw(batch, font);
        }
    }
    public static void garbageTextProcessor() {
        if(!garbageTextCollector.isEmpty()){
            damageTexts.removeAll(garbageTextCollector);
        }

        garbageTextCollector.clear();
    }

    public static void textProcessor(float deltaTime) {
        if(!damageTexts.isEmpty()) {
            for(var t : damageTexts){
                t.update(deltaTime);

                if(t.done()){
                    garbageTextCollector.add(t);
                }
            }
        }
    }
}
