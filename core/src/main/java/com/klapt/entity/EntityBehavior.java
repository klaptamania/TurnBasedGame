package com.klapt.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.klapt.equipment.EquipmentBehaviour;
import com.klapt.main.GameScreen;
import com.klapt.rendering.EffectRenderer;
import com.klapt.rendering.EntityRenderer;
import com.klapt.skill.Effect;
import com.klapt.manager.MoveManager;
import com.klapt.utilityTool.DamageText;
import com.klapt.utilityTool.LineDrawer;

import java.util.HashMap;

public class EntityBehavior {

    public static void reduceHP(Entity entity, Entity source,int amount, DamageText.TypeText type){
        if(entity.containStatus("CounterAttack") && source != null){
            System.out.println("COUNTER ATTACK!");
            entity.setAlreadyAttack(false);
            attack(entity,source, false);
            entity.setAlreadyAttack(false);
            amount = Math.round(amount * 0.5f);
        }

        entity.setHp(Math.max(0, entity.getHp() - amount));
        entity.setBeingHit(true);
        DamageText.createDamageText(amount, type, entity.x, entity.y + GameScreen.imageTile);

        if(entity.getHp() == 0){
            if(entity.getPassive()!= null && entity.getPassive().getName().contentEquals("REVIVE")){
                MoveManager.passiveActivation(entity, null);

                System.out.println(entity.getHp());

                if(entity.getHp() != 0){return;}
            }
            entity.callOfDeath();
            System.out.println(entity.getName() + " fainted.");
        }
    }

    public static void attack(Entity current, Entity target, boolean isAllyTurn) {
        Entity receiver = target;
        Entity user = current;
        if(current.isAlreadyAttack()) return;

        //Calc dodgeRate and reduced it due to attacker's accuracy
        float rDodgeRate = receiver.getDodgeRate() * (100f / (100f + current.getAccuracy()));
        float finalReceiverDodgeRate = MathUtils.clamp(rDodgeRate / 100f, 0f, 0.95f);

        current.setAlreadyAttack(true);
        current.setAttackAnimation(true);

        //get offset for attacks image
        int offsetA = 0, offsetD = 0;
        if(isAllyTurn){
            offsetA = GameScreen.imageTile;
        }else offsetD = GameScreen.imageTile;

        user = MoveManager.activeReflection(user, receiver);

        if(user != current){
            if(isAllyTurn){
                offsetA = 0;
                offsetD = GameScreen.imageTile;
            }else {
                offsetA = GameScreen.imageTile;
                offsetD = 0;
            }

            receiver = current;
            finalReceiverDodgeRate = 0;
        }

        LineDrawer.container.add(new LineDrawer(user.getX() + GameScreen.imageTile / 2f , user.getY() + offsetA, receiver.getX() + GameScreen.imageTile / 2f, receiver.getY() + offsetD, 2f, new Color(1,0,0,1)));

        if(MathUtils.randomBoolean(finalReceiverDodgeRate) && !receiver.containStatus("Reflection") && !receiver.containStatus("CounterAttack")){
            System.out.println(receiver.getName() + " dodged the " + user.getName() + "'s attack!");
            EntityRenderer.dodgeAnimation(receiver);
            user.setAttackHit(false);
        }else{
            //Attack hit
            user.setAttackHit(true);
            //check this attack is Crit or not
            float critChance = MathUtils.clamp( user.getCritRate() / 100f, 0, 1);

            int damage = EquipmentBehaviour.damageCalculate(user);
            boolean isCrit = false;

            if(MathUtils.randomBoolean(critChance)){
                damage = Math.round(damage * (user.getCritBuffer() / 100f));
                isCrit = true;
            }

            //Reduce Damage due to Physical Def
            int damageReduced = MathUtils.clamp( Math.round(damage * (1 - EquipmentBehaviour.physicalDefCalculate(receiver) / 100f)), 0, 10000);

            //Reduce Damage due to Con
            float conPercent = Math.min(Entity.calculateStats(receiver.getCon(), 0.25f), 45f);
            damageReduced = MathUtils.clamp(Math.round(damageReduced * (1 - conPercent / 100f)), 0, 10000);

            if(isCrit){
                //Lost HP due to Reduced Damage
                reduceHP(receiver, user,damageReduced, DamageText.TypeText.CRIT);
                System.out.println("CRITICAL! " + user.getName() + " attack " + receiver.getName() + " for " + damageReduced + " damage!");
                LineDrawer.container.add(new LineDrawer(user.getX() + GameScreen.imageTile / 2f , user.getY() + offsetA, receiver.getX() + GameScreen.imageTile / 2f, receiver.getY() + offsetD, 2f, new Color(1,0,0,1)));
            }else{
                reduceHP(receiver, user,damageReduced, DamageText.TypeText.NORMAL);
                System.out.println(user.getName() + " attack " + receiver.getName() + " for " + damageReduced + " damage!");
                LineDrawer.container.add(new LineDrawer(user.getX() + GameScreen.imageTile / 2f , user.getY() + offsetA, receiver.getX() + GameScreen.imageTile / 2f, receiver.getY() + offsetD, 2f, new Color(1,1,1,1)));
            }

            MoveManager.passiveActivation(current, receiver);
        }
    }

    public static void statusProcessing(Entity entity){
        HashMap<String, Integer> status = entity.getStatus();

        if(status.isEmpty() || !entity.isAlive()) return;


        for(String name : status.keySet()){
            if(!entity.isAlive()) break;

            //Reduce duration
            int duration = status.get(name) - 1;
            status.put(name, duration);
            if(duration <= 0){
                entity.addRemoveKeySet(name);
            }
            int amount = 0;
            //Process Status
            String[] container = name.split(" ");
            switch(container[0]){
                case "BLEED":
                    amount = Math.round(entity.getMaxHp() * 0.03f);
//                    System.out.println(getName() + " lost " + amount + " due to BLEED (" + duration + " turns left)");
                    System.out.printf("%s lost %d HP due to BLEED (%d turns left).\n", entity.getName(), amount, duration);
                    reduceHP(entity, null,amount, DamageText.TypeText.BLEED);

                    EffectRenderer.effectsAnim.add(new Effect(entity.getX(), entity.getY(), "BLEED"));

                    break;
                case "STUN":
                    break;
                case "IGNITED":
                    amount = Math.round(entity.getMaxHp() * 0.08f);

                    System.out.printf("%s lost %d HP from FIRE (%d turns left)\n", entity.getName(), amount, duration);
                    reduceHP(entity, null,amount, DamageText.TypeText.IGNITED);
                    EffectRenderer.effectsAnim.add(new Effect(entity.getX(), entity.getY(), "IGNITED"));

                    break;
                case "STR_BUFFED":
                    if(duration == 0){
                        amount = Integer.parseInt(container[1]);
                        entity.addStrBonus(Math.max(entity.getStrBonus() - amount, 0));
                        System.out.println("STR DEBUFFS LOG: Lost " + amount);
                        EffectRenderer.effectsAnim.add(new Effect(entity.getX(), entity.getY(), "STR_UN_BUFF"));
                    }else EffectRenderer.effectsAnim.add(new Effect(entity.getX(), entity.getY(), "STR_BUFF_ACTIVATE"));
                    break;
                case "DEX_REDUCE":
                    if(duration == 0){
                        amount = Integer.parseInt(container[1]);
                        entity.addDexBonus(Math.max(entity.getDexBonus() - amount, 0));
                        System.out.println("DEX DEBUFFS LOG: Gain " + amount);
                    }
                    break;
            }
        }
    }

    public static boolean stunProgress(Entity entity){
        if(entity.getStunned() > 0){
            entity.stunRecover();
            System.out.println(entity.getName() + " is stunned. (Turns left: " + entity.getStunned() + ").");
            return true;
        }else if (entity.getStunned() == 0){
            entity.stunRecover();
            System.out.println(entity.getName() + " recovered from stun.");
            return false;
        }
        return false;
    }
    public static void clearingStatus(Entity entity){
        if(!entity.isRemoval()) return;

        for(var v : entity.getRemoveKeySet()){
            entity.removeStatus(v);
        }

        entity.getRemoveKeySet().clear();
    }

    public static void statsChanged(Entity entity, String type, int amount){
        switch (type){
            case "CON":
                entity.addConBonus(amount);
                break;
            case "WIS":
                entity.addWisBonus(amount);
                break;
            case "INT":
                entity.addIntelBonus(amount);
                break;
            case "STR":
                entity.addStrBonus(amount);
                break;
            case "DEX":
                entity.addDexBonus(amount);
                break;
        }
    }
    public static void regenSP(Entity entity) { entity.skillPoint = Math.min(entity.skillPoint + entity.spRegen, entity.maxSkillPoint); }

}
