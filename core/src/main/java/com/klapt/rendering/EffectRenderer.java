package com.klapt.rendering;

import com.klapt.skill.Effect;

import java.util.ArrayList;
import java.util.List;

public class EffectRenderer {
    public static List<Effect> effectsAnim = new ArrayList<>();
    static List<Effect> garbageEffectCollector = new ArrayList<>();

    public static void effectsAnimationProcessor(float deltaTime) {
        if(effectsAnim.isEmpty()) return;

        for(Effect e : effectsAnim){
            if(e.isFinished()){
                garbageEffectCollector.add(e);
            }else{
                e.update(deltaTime);
            }
        }
    }

    public static void garbageEffectProcessor(){
        if(!garbageEffectCollector.isEmpty()){
            effectsAnim.removeAll(garbageEffectCollector);
        }
        garbageEffectCollector.clear();
    }
}
