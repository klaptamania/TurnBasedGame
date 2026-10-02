package com.klapt.utilityTool;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.klapt.main.Main;

import java.util.HashMap;

public class AnimationHolder {
    public static final HashMap<String, Animation<TextureRegion>> container = new HashMap<>();
    static float frameDuration = 0.155f;
    // Constructor
    public static void loadAllAnimations() {
        // Load Bleed Animation
        container.put("BLEED", new Animation<>(frameDuration, loadFrames("BleedSheet")));
        container.put("SEVERED_BLEED", new Animation<>(frameDuration, loadFrames("SeveredBleedSheet")));
        container.put("FIRE_BALL", new Animation<>(frameDuration, loadFrames("FireBallSheet")));
        container.put("DODGE", new Animation<>(frameDuration, loadFrames("DodgeSheet")));
        container.put("SELF_HEAL", new Animation<>(frameDuration, loadFrames("SelfHealSheet")));
        container.put("IGNITED", new Animation<>(frameDuration, loadFrames("IgnitedSheet")));
        container.put("THUNDERBOLT", new Animation<>(frameDuration, loadFrames("ThunderboltSheet")));
        container.put("IAIDO", new Animation<>(frameDuration, loadFrames("IaidoSheet")));
        container.put("REFLECTING", new Animation<>(frameDuration, loadFrames("ReflectingSheet")));
        container.put("REFLECT", new Animation<>(frameDuration, loadFrames("ReflectSheet")));
        container.put("STR_BUFF", new Animation<>(frameDuration, loadFrames("STRBuffSheet")));
        container.put("STR_UN_BUFF", new Animation<>(frameDuration, loadFrames("STRUnBuffSheet")));
        container.put("STR_BUFF_ACTIVATE", new Animation<>(frameDuration, loadFrames("STRBuffActivateSheet")));
        container.put("THRUST", new Animation<>(frameDuration, loadFrames("ThrustSheet")));
        container.put("QUICK_SNAP", new Animation<>(frameDuration, loadFrames("QuickSnapSheet")));
        container.put("REVIVE", new Animation<>(frameDuration, loadFrames("ReviveSheet")));
    }

    public static Animation<TextureRegion> get(String key) {
        return container.get(key);
    }


    private static TextureRegion[][] getSheet(String fileName){
        return TextureRegion.split(Main.getAm().getTexture(fileName), 64, 64);
    }

    private static TextureRegion[] loadFrames(String sheetName) {
        TextureRegion[][] tmp = getSheet(sheetName);
        TextureRegion[] frames = new TextureRegion[5];

        int index = 0;
        for (int i = 0; i < tmp.length && index < 1; i++) {
            for (int j = 0; j < tmp[i].length && index < 5; j++) {
                frames[index++] = tmp[i][j];
            }
        }
        return frames;
    }
}
