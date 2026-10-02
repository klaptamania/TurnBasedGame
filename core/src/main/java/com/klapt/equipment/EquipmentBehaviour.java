package com.klapt.equipment;

import com.badlogic.gdx.math.MathUtils;
import com.klapt.entity.Entity;

public class EquipmentBehaviour {

    public static int physicalDefCalculate(Entity entity){
        int def = 0;

        for(var item : entity.getArmor()) {
            if (item != null && checkItemRequire(entity, item)) {
                def += item.getPhysicalDef();
            }
        }

        return MathUtils.clamp(def, 0, 95);
    }

    public static int magicDefCalculate(Entity entity){
        int mDef = 0;

        for(var item : entity.getArmor()) {
            if (item != null && checkItemRequire(entity, item)) {
                mDef += item.getMagicalDef();
            }
        }

        return MathUtils.clamp(mDef, 0, 95);
    }

    public static int damageCalculate(Entity user){
        Item weapon = user.getWeapon();
        boolean hasRequire = checkItemRequire(user, weapon);

        int bufferedDamage = hasRequire ? weapon.getBaseDamage() : Math.round(weapon.getBaseDamage() * 0.5f);


        int bonusDamage = 0;

        if(hasRequire){
            if(!weapon.getConScale().contentEquals("--")){
                bonusDamage += scaleCalculator(user.getCon(), weapon.getConScale());
            }
            if(!weapon.getDexScale().contentEquals("--")){
                bonusDamage += scaleCalculator(user.getDex(), weapon.getDexScale());
            }
            if(!weapon.getIntScale().contentEquals("--")){
                bonusDamage += scaleCalculator(user.getIntel(), weapon.getIntScale());
            }
            if(!weapon.getStrScale().contentEquals("--")){
                bonusDamage += scaleCalculator(user.getStr(), weapon.getStrScale());
            }
            if(!weapon.getWisScale().contentEquals("--")){
                bonusDamage += scaleCalculator(user.getWis(), weapon.getWisScale());
            }
        }

        bufferedDamage += bonusDamage;

        return bufferedDamage;
    }


    public static boolean checkItemRequire(Entity entity, Item item) {
        if (entity == null || item == null) return false;
        return entity.getCon() >= item.getRequiredCon()
            && entity.getDex() >= item.getRequiredDex()
            && entity.getIntel() >= item.getRequiredInt()
            && entity.getStr() >= item.getRequiredStr()
            && entity.getWis() >= item.getRequiredWis();
    }

    static float scaleConverter(String scale){
        return switch (scale){
            case "S"  -> 0.80f; // Max stat (999) ~800 dmg
            case "A+" -> 0.70f; // Max stat (999) ~700 dmg
            case "A"  -> 0.60f; // Max stat (999) ~600 dmg
            case "A-" -> 0.52f; // Max stat (999) ~520 dmg
            case "B+" -> 0.44f; // Max stat (999) ~440 dmg
            case "B"  -> 0.36f; // Max stat (999) ~360 dmg
            case "B-" -> 0.28f; // Max stat (999) ~280 dmg
            case "C+" -> 0.21f; // Max stat (999) ~210 dmg
            case "C"  -> 0.15f; // Max stat (999) ~150 dmg
            case "C-" -> 0.09f; // Max stat (999) ~90 dmg
            case "D"  -> 0.04f; // Max stat (999) ~40 dmg

            default -> 0;
        };
    }

    static int scaleCalculator(int value, String scale){
        return Math.round(value * scaleConverter(scale));
    }
}

