package com.klapt.manager;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.klapt.entity.Entity;
import com.klapt.entity.EntityBehavior;
import com.klapt.equipment.EquipmentBehaviour;
import com.klapt.main.GameScreen;
import com.klapt.movement.Move;
import com.klapt.rendering.EffectRenderer;
import com.klapt.rendering.EntityRenderer;
import com.klapt.skill.Effect;
import com.klapt.skill.Skill;
import com.klapt.utilityTool.DamageText;
import com.klapt.utilityTool.LineDrawer;
import com.klapt.utilityTool.QuickGacha;

import java.util.ArrayList;
import java.util.List;

public class MoveManager {
    public static void passiveActivation(Entity user, Entity enemy) {
        Skill ps = user.getPassive();
        if (ps == null || ps.getName().contentEquals("")) return;

        String skill = ps.getName();
        int duration = ps.getDuration();
        float chance = ps.getChance() / 100f;

        //check if failed on inflict
        if (!MathUtils.randomBoolean(chance)) {
            return;
        }

        user.setPassiveActivated(true);

        switch (skill) {
            case "BLEED":
                //Inflict BLEED on attack
                if (user.isAttackHit()) {
                    if (enemy.containStatus(skill)) { //if enemy already BLEED, take extra damaged
                        int amount = Math.round(enemy.getMaxHp() * 0.10f);
                        EntityBehavior.reduceHP(enemy, user, amount, DamageText.TypeText.BLEED);
                        System.out.printf("SEVERED BLEED! %s lost %d HP!\n", enemy.getName(), amount);
                        EffectRenderer.effectsAnim.add(new Effect(enemy.getX(), enemy.getY(), "SEVERED_BLEED"));
                    } else {
                        System.out.printf("%s bleeds for %d turns!\n", enemy.getName(), duration);
                        EffectRenderer.effectsAnim.add(new Effect(enemy.getX(), enemy.getY(), "BLEED"));
                    }

                    //refresh/add status to enemy
                    enemy.addStatusCondition("BLEED", duration);
                }
                break;
            case "STUN":
                //Stun enemy
                if (user.isAttackHit()) {
                    enemy.setStunned(duration);
                    System.out.printf("%s stunned %s for %d turns\n", user.getName(), enemy.getName(), duration);
                }
                break;
            case "REVIVE":
                if (ps.isUsed() || user.getHp() > 0) return;
                System.out.printf("%s fainted! But they REJECTED the DEATH!\n", user.getName());
                EffectRenderer.effectsAnim.add(new Effect(user.getX(), user.getY() + 35, "REVIVE"));

                user.increaseHP(user.getMaxHp());
                ps.setUsed(true);
                ps.startCD();
                break;
        }
    }

    public static void onCDSkill(Entity user, float time, Move exception) {
        List<Skill> sLst = new ArrayList<>();
        List<Move> mLst = new ArrayList<>();

        if (user.getActiveSkill() != null) sLst.addAll(user.getActiveSkill());
        mLst = user.getMoveSet();

        if (user.getWeapon().haveActiveSkill()) {
            sLst.addAll(user.getWeapon().getActiveSkill());
        }

        if (user.getPassive() != null && user.getPassive().getName().contentEquals("REVIVE")) {
            sLst.add(user.getPassive());
        }

        for (Skill s : sLst) {
            if (s.isUsed()) {

                s.setCdLeft(Math.max(0, s.getCdLeft() - time));
                if(s.getCdLeft() <= 0) s.setUsed(false);
            }
        }

        for(Move m : mLst){
            if(m.isUsed()){

                m.setCdLeft(Math.max(0, m.getCdLeft() - time));
                if(m.getCdLeft() <= 0) m.setUsed(false);
            }
        }
    }

    public static void skillActivation(Entity user, Entity target, int index, boolean isAllyTurn) {
        if (user == null) return;

        if (user.containStatus("SILENCE")) {
            return;
        }

        // if user already cast a skill
        if (user.isCastSkill()) return;

        user.setCastSkill(true);

        if (target != null && !target.isAlive()) return;

        // get skills
        List<Skill> skillLst = new ArrayList<>();
        if (user.getActiveSkill() != null) skillLst.addAll(user.getActiveSkill());

        if (user.getWeapon() != null && user.getWeapon().haveActiveSkill()) {
            skillLst.addAll(user.getWeapon().getActiveSkill());
        }

        Skill s = skillLst.get(index);

        // if user dont hav any skill
        if (s == null) return;

        if (user.getSkillPoint() < s.getCost()) {
            System.out.printf("%s failed: Not enough SP!\n", user.getName());
            return;
        }

        if (s.isUsed()) {
            return;
        }

        // Calc dodgeRate and reduced it due to attacker's accuracy
        float rDodgeRate = target == null ? 0 : target.getDodgeRate() * (100f / (100f + user.getAccuracy()));
        float finalReceiverDodgeRate = MathUtils.clamp(rDodgeRate / 100f, 0f, 0.95f);

        int damage = s.getDamage();

        if (user.getWeapon() != null && user.getWeapon().getType().contentEquals("Super Light")) {
            damage = s.getDamage() + EquipmentBehaviour.damageCalculate(user) + Math.round(user.getWis() / 10f);
        }

        int offsetA = 0, offsetD = 0;
        if (isAllyTurn) {
            offsetA = GameScreen.imageTile;
        } else offsetD = GameScreen.imageTile;

        switch (s.getName()) {
            case "Fireball":
                if (user.getSkillPoint() >= s.getCost()) {
                    if (target == null) return;

                    s.startCD();
                    user.reduceSP(s.getCost());

                    System.out.printf("%s use FIRE BALL!\n", user.getName());
                    s.setUsed(true);

                    Entity originalUser = user;
                    user = activeReflection(user, target);
                    if (user != originalUser) {
                        target = originalUser;
                        if (isAllyTurn) {
                            offsetA = 0;
                            offsetD = GameScreen.imageTile;
                        } else {
                            offsetA = GameScreen.imageTile;
                            offsetD = 0;
                        }
                        finalReceiverDodgeRate = 0;
                    }

                    LineDrawer.container.add(new LineDrawer(user.getX() + GameScreen.imageTile / 2f, user.getY() + offsetA, target.getX() + GameScreen.imageTile / 2f, target.getY() + offsetD, 3f, Color.valueOf("#ffaf18")));

                    if (MathUtils.randomBoolean(finalReceiverDodgeRate) && !target.containStatus("Reflection") && !target.containStatus("CounterAttack")) {
                        System.out.println(target.getName() + " dodge the Fire Ball!");
                        EntityRenderer.dodgeAnimation(target);
                    } else {
                        // Reduce damage due to target Magic Defense
                        int damageReduced = MathUtils.clamp(Math.round(damage * (1 - EquipmentBehaviour.magicDefCalculate(target) / 100f)), 0, 10000);

                        // Reduce Damage due to Con
                        float conPercent = Math.min(Entity.calculateStats(target.getCon(), 0.25f), 45f);
                        damageReduced = MathUtils.clamp(Math.round(damageReduced * (1 - conPercent / 100f)), 0, 10000);

                        EntityBehavior.reduceHP(target, user, damageReduced, DamageText.TypeText.IGNITED);

                        EffectRenderer.effectsAnim.add(new Effect(target.getX(), target.getY(), "FIRE_BALL"));

                        if (target.isAlive() && target.containStatus("IGNITED")) {
                            int amount = Math.round(target.getMaxHp() * 0.12f);
                            EntityBehavior.reduceHP(target, user, amount, DamageText.TypeText.IGNITED);

                            System.out.printf("SEVERED BURN! %s lost %d HP!\n", target.getName(), amount);
                            return;
                        }
                        target.addStatusCondition("IGNITED", s.getDuration());
                        System.out.printf("%s is on fire!\n", target.getName());
                    }
                }
                break;

            case "Thunderbolt":
                if (user.getSkillPoint() >= s.getCost()) {
                    if (target == null) return;

                    s.startCD();
                    user.reduceSP(s.getCost());

                    System.out.printf("%s use Thunderbolt!\n", user.getName());

                    Entity originalUser = user;
                    user = activeReflection(user, target);
                    if (user != originalUser) {
                        target = originalUser;
                        finalReceiverDodgeRate = 0;

                        if (isAllyTurn) {
                            offsetA = 0;
                            offsetD = GameScreen.imageTile;
                        } else {
                            offsetA = GameScreen.imageTile;
                            offsetD = 0;
                        }
                    }

                    LineDrawer.container.add(new LineDrawer(user.getX() + GameScreen.imageTile / 2f, user.getY() + offsetA, target.getX() + GameScreen.imageTile / 2f, target.getY() + offsetD, 3f, Color.valueOf("#18fffe")));

                    if (target.isAlive()) {
                        if (MathUtils.randomBoolean(finalReceiverDodgeRate) && !target.containStatus("Reflection") && !target.containStatus("CounterAttack")) {
                            System.out.println(target.getName() + " dodge the Thunderbolt!");
                            EntityRenderer.dodgeAnimation(target);
                        } else {
                            float mDefPercent = EquipmentBehaviour.magicDefCalculate(target) / 100f;
                            float conPercent = Math.min(Entity.calculateStats(target.getCon(), 0.25f), 40.f) / 100f;

                            int damageReduced = MathUtils.clamp(Math.round(damage * (1 - mDefPercent) * (1 - conPercent)), 0, 10000);

                            EntityBehavior.reduceHP(target, user, damageReduced, DamageText.TypeText.PARALYZE);

                            EffectRenderer.effectsAnim.add(new Effect(target.getX(), target.getY(), "THUNDERBOLT"));

                            // FULMINATION
                            if (target.isAlive() && target.containStatus("PARALYZED")) {
                                float hpPercent = 0.10f;
                                int fulminationDamage = Math.round((target.getMaxHp() * hpPercent) * (1 - mDefPercent));

                                EntityBehavior.reduceHP(target, user, fulminationDamage, DamageText.TypeText.PARALYZE);
                            } else {
                                target.addStatusCondition("PARALYZED", s.getDuration());
                                System.out.printf("%s was struck by a thunderbolt for %d damage!\n", target.getName(), damageReduced);
                            }
                        }
                    }
                    s.setUsed(true);
                }
                break;

            case "Self Heal":
                s.startCD();
                user.reduceSP(s.getCost());
                s.setUsed(true);

                user.increaseHP(damage);

                DamageText.createDamageText(damage, DamageText.TypeText.HEAL, user.getX(), user.getY());
                user.setAlreadyAttack(true);

                System.out.printf("%s use Self Heal and healed for %d HP!\n", user.getName(), damage);
                EffectRenderer.effectsAnim.add(new Effect(user.getX(), user.getY(), "SELF_HEAL"));
                break;

            case "Buff Str":
                s.startCD();
                user.reduceSP(s.getCost());
                s.setUsed(true);

                EffectRenderer.effectsAnim.add(new Effect(user.getX(), user.getY(), "STR_BUFF"));

                int amountBC = Math.round(user.getStr() * 0.55f);

                EntityBehavior.statsChanged(user, "STR", amountBC);
                user.addStatusCondition("STR_BUFFED " + amountBC, s.getDuration());

                DamageText.createDamageText(amountBC, DamageText.TypeText.BLEED, user.getX(), user.getY());
                user.setAlreadyAttack(true);

                System.out.printf("%s use BUFF Strength increases it for %d!\n", user.getName(), amountBC);
                break;

            case "Quick Snap":
                s.startCD();
                user.reduceSP(s.getCost());
                s.setUsed(true);

                EffectRenderer.effectsAnim.add(new Effect(user.getX(), user.getY(), "QUICK_SNAP"));
                user.setAlreadyAttack(true);

                System.out.printf("%s use QUICK SNAP!\n", user.getName());
                if (target != null && target.isAlive()) {
                    target.addStatusCondition("SILENCE", s.getDuration());

                    float mDefPercent = EquipmentBehaviour.magicDefCalculate(target) / 100f;
                    float conPercent = Math.min(Entity.calculateStats(target.getCon(), 0.25f), 40.f) / 100f;

                    int damageReduced = MathUtils.clamp(Math.round(100 * (1 - mDefPercent) * (1 - conPercent)), 0, 10000);

                    EntityBehavior.reduceHP(target, user, damageReduced, DamageText.TypeText.CRIT);
                    System.out.printf("%s lost %d HP!\n", target.getName(), damageReduced);

                    int amountQS = Math.round(target.getDex() * 0.95f);

                    EntityBehavior.statsChanged(target, "DEX", -amountQS);
                    target.addStatusCondition("DEX_REDUCE " + amountQS, s.getDuration());
                    System.out.printf("%s lost %d DEX!\n", target.getName(), amountQS);
                }
                break;

            //================================================Physical===========================================================//
            case "Iaido":
                if (target == null) return;

                s.startCD();
                s.setUsed(true);
                user.reduceSP(s.getCost());

                System.out.printf("%s use Iaido!\n", user.getName());

                Entity originalUserIaido = user;
                user = activeReflection(user, target);
                if (user != originalUserIaido) {
                    target = originalUserIaido;
                    finalReceiverDodgeRate = 0;
                    if (isAllyTurn) {
                        offsetA = 0;
                        offsetD = GameScreen.imageTile;
                    } else {
                        offsetA = GameScreen.imageTile;
                        offsetD = 0;
                    }
                }

                LineDrawer.container.add(new LineDrawer(user.getX() + GameScreen.imageTile / 2f, user.getY() + offsetA, target.getX() + GameScreen.imageTile / 2f, target.getY() + offsetD, 3f, Color.valueOf("#b0b0b0")));

                if (MathUtils.randomBoolean(finalReceiverDodgeRate) && !target.containStatus("Reflection") && !target.containStatus("CounterAttack")) {
                    System.out.println(target.getName() + " dodge the Iaido!");
                    EntityRenderer.dodgeAnimation(target);
                } else {
                    user.setAlreadyAttack(true);

                    int physicalDamage = Math.round((EquipmentBehaviour.damageCalculate(user) + damage * 1.7f + user.getStr() * 0.45f + EquipmentBehaviour.physicalDefCalculate(target) * 0.5f));

                    float conPercent = Math.min(Entity.calculateStats(target.getCon(), 0.25f), 45f);
                    int damageReduced = MathUtils.clamp(Math.round(physicalDamage * (1 - conPercent / 100f)), 0, 10000);
                    System.out.printf("Iaido deal %d damage to %s!\n", damageReduced, target.getName());

                    EntityBehavior.reduceHP(target, user, damageReduced, DamageText.TypeText.PHYSICAL);

                    EffectRenderer.effectsAnim.add(new Effect(target.getX(), target.getY(), "IAIDO"));

                    if (target.isAlive() && target.containStatus("BLEED")) {
                        int amount = Math.round(target.getMaxHp() * 0.42f);

                        EntityBehavior.reduceHP(target, user, amount, DamageText.TypeText.CRIT);

                        System.out.printf("SEVERED BLEED! %s lost %d HP!\n", target.getName(), amount);
                    }
                }
                break;

            case "Reflection":
                s.startCD();
                s.setUsed(true);
                user.reduceSP(s.getCost());

                System.out.printf("%s use Reflection!\n", user.getName());
                user.setAlreadyAttack(true);

                EffectRenderer.effectsAnim.add(new Effect(user.getX(), user.getY(), "REFLECT"));
                user.addStatusCondition("Reflection", s.getDuration());
                break;

            case "Counter Attack":
                s.startCD();
                s.setUsed(true);
                user.reduceSP(s.getCost());
                user.setAlreadyAttack(false);

                EffectRenderer.effectsAnim.add(new Effect(user.getX(), user.getY(), "REFLECT"));
                user.addStatusCondition("CounterAttack", s.getDuration());
                break;

            case "Thrust":
                s.startCD();
                s.setUsed(true);
                user.reduceSP(s.getCost());

                System.out.printf("%s use Thrust!\n", user.getName());
                user.setAlreadyAttack(true);

                Entity originalUserThrust = user;
                user = activeReflection(user, target);
                if (user != originalUserThrust) {
                    target = originalUserThrust;
                    finalReceiverDodgeRate = 0;
                    if (isAllyTurn) {
                        offsetA = 0;
                        offsetD = GameScreen.imageTile;
                    } else {
                        offsetA = GameScreen.imageTile;
                        offsetD = 0;
                    }
                }

                LineDrawer.container.add(new LineDrawer(user.getX() + GameScreen.imageTile / 2f, user.getY() + offsetA, target.getX() + GameScreen.imageTile / 2f, target.getY() + offsetD, 3f, Color.valueOf("#b0b0b0")));

                if (MathUtils.randomBoolean(finalReceiverDodgeRate) && !target.containStatus("Reflection") && !target.containStatus("CounterAttack")) {
                    System.out.println(target.getName() + " dodge the Thrust!");
                    EntityRenderer.dodgeAnimation(target);
                } else {
                    user.setAlreadyAttack(true);

                    int physicalDamage = Math.round((EquipmentBehaviour.damageCalculate(user) + damage * 1.7f + user.getStr() * 0.45f + user.getDex() * 0.75f));

                    float critChance = MathUtils.clamp(user.getCritRate() / 100f + 0.3f, 0, 1);
                    boolean isCrit = false;

                    if (MathUtils.randomBoolean(critChance)) {
                        physicalDamage = Math.round(physicalDamage * (user.getCritBuffer() / 100f));
                        isCrit = true;
                    }

                    physicalDamage = MathUtils.clamp(Math.round(physicalDamage * (1 - EquipmentBehaviour.physicalDefCalculate(target) / 100f)), 0, 10000);
                    float conPercent = Math.min(Entity.calculateStats(target.getCon(), 0.25f), 45f);
                    int damageReduced = MathUtils.clamp(Math.round(physicalDamage * (1 - conPercent / 100f)), 0, 10000);

                    if (isCrit) {
                        System.out.printf("CRITICAL! Thrust deal %d damage to %s!\n", damageReduced, target.getName());

                        EntityBehavior.reduceHP(target, user, damageReduced, DamageText.TypeText.CRIT);
                    } else {
                        System.out.printf("Thrust deal %d damage to %s!\n", damageReduced, target.getName());

                        EntityBehavior.reduceHP(target, user, damageReduced, DamageText.TypeText.PHYSICAL);
                    }
                    EffectRenderer.effectsAnim.add(new Effect(target.getX(), target.getY(), "THRUST"));

                    if (target.isAlive() && target.containStatus("BLEED")) {
                        int amount = Math.round(target.getMaxHp() * 0.42f);

                        EntityBehavior.reduceHP(target, user, amount, DamageText.TypeText.CRIT);

                        System.out.printf("SEVERED BLEED! %s lost %d HP!\n", target.getName(), amount);
                    }
                }
                break;
        }
    }

    public static void moveActivation(Entity user, Entity target, int index, boolean isAllyTurn) {
        if (user == null) return;

        if (target == null || !target.isAlive()) return;

        // get skills
        List<Move> moveList = user.getMoveSet();

        if (user.getWeapon() != null && user.getWeapon().haveActiveSkill()) {
            moveList.addAll(user.getWeapon().getActiveSkill());
        }

        Move m = moveList.get(index);

        // if user dont hav any skill
        if (m == null) return;

        if (m.isUsed()) {
            return;
        }

        //Attack hit
        user.setAttackHit(true);
        //check this attack is Crit or not
        float critChance = MathUtils.clamp(user.getCritRate() / 100f, 0, 1);

        int damage = EquipmentBehaviour.damageCalculate(user);
        boolean isCrit = false;

        if (MathUtils.randomBoolean(critChance)) {
            damage = Math.round(damage * (user.getCritBuffer() / 100f));
            isCrit = true;
        }

        //Reduce Damage due to Physical Def
        int damageReduced = MathUtils.clamp(Math.round(damage * (1 - EquipmentBehaviour.physicalDefCalculate(target) / 100f)), 0, 10000);

        //Reduce Damage due to Con
        float conPercent = Math.min(Entity.calculateStats(target.getCon(), 0.25f), 45f);
        damageReduced = MathUtils.clamp(Math.round(damageReduced * (1 - conPercent / 100f)), 0, 10000);


        int offsetA = 0, offsetD = 0;
        if (isAllyTurn) {
            offsetA = GameScreen.imageTile;
        } else offsetD = GameScreen.imageTile;

        m.startCD();

        m.setUsed(true);

        Entity originalUser = user;
        user = activeReflection(user, target);
        if (user != originalUser) {
            target = originalUser;
            if (isAllyTurn) {
                offsetA = 0;
                offsetD = GameScreen.imageTile;
            } else {
                offsetA = GameScreen.imageTile;
                offsetD = 0;
            }
        }

        EntityBehavior.reduceHP(target, user, damageReduced, isCrit ? DamageText.TypeText.CRIT : DamageText.TypeText.NORMAL);
        LineDrawer.container.add(new LineDrawer(user.getX() + GameScreen.imageTile / 2f, user.getY() + offsetA, target.getX() + GameScreen.imageTile / 2f, target.getY() + offsetD, 3f, Color.valueOf("#ffffff")));
        System.out.printf("%s attack %s for %d damage!\n", user.getName(), target.getName(), damageReduced);
//                    EffectRenderer.effectsAnim.add(new Effect(target.getX(), target.getY(), "FIRE_BALL"));
    }

    public static Skill getRandomActiveSkill(List<Skill> listed) {
        List<Skill> unUsedSkills = new ArrayList<>();
        //Take all alive entity
        for (Skill skill : listed) {
            if (!skill.isUsed()) unUsedSkills.add(skill);
        }

        //Return -1 if there's no alive entity
        if (unUsedSkills.isEmpty()) return null;

        return new QuickGacha<Skill>(unUsedSkills).roll();
    }

    public static Entity activeReflection(Entity user, Entity target) {
        if (target != null && target.containStatus("Reflection")) {
            EffectRenderer.effectsAnim.add(new Effect(target.getX(), target.getY(), "REFLECTING"));
            System.out.printf("REFLECT! %s reflect %s's attack!\n", target.getName(), user.getName());
            target.removeStatus("Reflection");
            return target;
        }
        return user;
    }

}
