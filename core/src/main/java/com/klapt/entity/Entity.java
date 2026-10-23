package com.klapt.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.klapt.equipment.EquipmentBehaviour;
import com.klapt.equipment.Item;
import com.klapt.interfacePack.Gacha;
import com.klapt.main.GameScreen;
import com.klapt.main.Main;
import com.klapt.skill.Skill;
import com.klapt.manager.SkillManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@lombok.Data
public class Entity implements Gacha {
    //==================Stats====================//

    int maxHp, hp;
    int con, dex, intel, str, wis;
    int conBonus, dexBonus, intelBonus, strBonus, wisBonus;

    int skillPoint, spRegen, maxSkillPoint;

    //================Condition==================//
    boolean isAlive = true, beingHit = false, castSkill = false;
    boolean alreadyAttack = false, attackHit = false, passiveActivated = false;
    int stunned = -1;

    String name;
    String textureName;
    String weaponPath;
    String passivePath;
    String[] armorPaths = new String[5];
    String[] activeSkillPaths = new String[5];

    EntityTexture entityTexture;

    String move;

    int index;

    float skillCastSpeed;
    float actionTime = 0;

    public enum State {IDLE, CASTING, RECOVERY}
    State currentState = State.IDLE;

    //================Equipment=================//
    Item weapon;
    Item[] armor = new Item[5];

    //=================Skill====================//
    Skill passive;
    List<Skill> activeSkill = new ArrayList<>();
    HashMap<String, Integer> status = new HashMap<>();
    List<String> removeKeySet = new ArrayList<>();

    //===============Animation==================//

    float hurtCounter = 0;
    float x, y;

    TextureRegion[] frames = new TextureRegion[5];
    Animation<TextureRegion> basicAttack;
    float stateCounter = 0;
    boolean attackAnimation = false;

    public Entity() {
        for (int i = 0; i < 5; i++) {
            String name = "BA" + Integer.toString(i + 1);
            frames[i] = new TextureRegion(Main.getAm().getTexture(name));
        }

        basicAttack = new Animation<TextureRegion>(0.155f, frames);
    }

    public static float calculateStats(int stat, float baseRate) {
        if (stat <= 50) {
            return stat * baseRate;
        } else if (stat <= 100) {
            return (50 * baseRate) + (stat - 50) * (baseRate / 2f);
        } else {
            return (50 * baseRate) + (50 * (baseRate / 2f)) + (stat - 100) * (baseRate / 4f);
        }
    }

    //==================Getters==================//
    public int getCon() {
        return con + conBonus;
    }

    public int getDex() {
        return dex + dexBonus;
    }

    public int getIntel() {
        return intel + intelBonus;
    }

    public int getStr() {
        return str + strBonus;
    }

    public int getWis() {
        return wis + wisBonus;
    }

    public void addStrBonus(int amount) {
        strBonus += amount;
    }

    public void addDexBonus(int amount) {
        dexBonus += amount;
    }

    public void addConBonus(int amount) {
        conBonus += amount;
    }

    public boolean isThisState(State state) { return currentState == state; }


    //Condition

    public void addWisBonus(int amount) {
        wisBonus += amount;
    }

    public void addIntelBonus(int amount) {
        intelBonus += amount;
    }

    public void addHurtCounter(float amount) {
        hurtCounter += amount;
    }

    public boolean containStatus(String name) {
        return status.containsKey(name);
    }

    public void addStatusCondition(String name, int duration) {
        status.put(name, duration);
    }

    public boolean isRemoval() {
        return !removeKeySet.isEmpty();
    }

    public void addRemoveKeySet(String name) {
        removeKeySet.add(name);
    }

    public EntityTexture getTexture() {
        return entityTexture;
    }

    public void stunRecover() {
        stunRecover(1);
    }

    public void stunRecover(int amount) {
        stunned -= amount;
    }

    public void increaseHP(int amount) {
        hp = Math.max(hp + amount, maxHp);
    }

    public void callOfDeath() {
        isAlive = false;
    }

    public void reduceSP(int amount) {
        skillPoint = Math.max(0, skillPoint - amount);
    }

    public int getAccuracy() {
        float accuracy;

        if (getDex() <= 50) {
            accuracy = getDex() * 0.6f;
        } else if (getDex() <= 100) {
            accuracy = 50 * 0.6f + ((getDex() - 50) * 0.35f);
        } else accuracy = (50 * 0.6f) + (50 * 0.35f) + ((getDex() - 100) * 0.2f);

        return Math.round(accuracy);
    }

    public int getCritRate() {
        float dexCrit = calculateStats(getDex(), 0.7f);
        float intelCrit = calculateStats(getIntel(), 0.3f);

        return Math.min(Math.round(dexCrit + intelCrit), 95);
    }

    public int getCritBuffer() {
        float baseCritDmg = 150f;

        float wisBonus = calculateStats(getWis(), 0.5f);

        float strBonus = calculateStats(getStr(), 0.25f);

        float totalMultiplier = baseCritDmg + wisBonus + strBonus;

        return Math.round(totalMultiplier);
    }

    public int getDodgeRate() {
        float dodgeRate = calculateStats(getDex(), 0.65f);
        float weightPenalty = Math.max(0.2f, 1.0f - (getCon() * 0.0025f));

        float finalDodge = dodgeRate * weightPenalty;

        return Math.min(Math.round(finalDodge), 95);
    }

    public void skillActivation(List<Entity> allies, List<Entity> enemies, int userIndex, boolean isAllyTurn) {
//        SkillManager.skillActivation(allies, enemies, userIndex, isAllyTurn);
    }

    public void load(int index) {
        this.index = index;
        name += " " + Integer.toString(index);

        if (weaponPath != null && !weaponPath.isEmpty()) {
            weapon = GameScreen.json.fromJson(Item.class, Gdx.files.internal(weaponPath));
        }

        if (passivePath != null && !passivePath.isEmpty()) {
            passive = GameScreen.json.fromJson(Skill.class, Gdx.files.internal(passivePath));
        }

        if (activeSkillPaths != null) {
            for (String activeSkillPath : activeSkillPaths) {
                if (activeSkillPath == null || activeSkillPath.isEmpty()) continue;
                activeSkill.add(GameScreen.json.fromJson(Skill.class, Gdx.files.internal(activeSkillPath)));
            }
        }

        if (armorPaths != null) {
            for (String armorPath : armorPaths) {
                if (armorPath == null || armorPath.isEmpty()) continue;
                Item temp = GameScreen.json.fromJson(Item.class, Gdx.files.internal(armorPath));
                if (temp != null && temp.getType() != null) {
                    switch (temp.getType()) {
                        case "Head":
                            armor[0] = temp;
                            break;
                        case "Arm":
                            armor[1] = temp;
                            break;
                        case "Body":
                            armor[2] = temp;
                            break;
                        case "Leg":
                            armor[3] = temp;
                            break;
                        case "Feet":
                            armor[4] = temp;
                            break;
                    }
                }
            }
        }

        if (textureName != null && !textureName.isEmpty()) {
            entityTexture = new EntityTexture(textureName, weapon.getName(), armor);
        }

        //Increase HP due to CON
        maxHp += Math.round(calculateStats(con, 4f));
        hp = maxHp;

        //Increase Skill Point and SP Regeneration due to Int
        maxSkillPoint = skillPoint;
        maxSkillPoint += Math.round(intel / 5f);
        skillPoint = maxSkillPoint;

        spRegen += Math.round(intel / 10f);

        //Setup SkillCasting Speed due to percent

        if(!EquipmentBehaviour.checkItemRequire(this, this.getWeapon())){
            skillCastSpeed = 1.1f;
            return;
        }

        float percent = 0;
        for(int i = 0; i < 5; i++){
            switch (i){
                case 0:
                    if(!this.getWeapon().getStrScale().contentEquals("--")){
                        percent += EquipmentBehaviour.scaleCalculator(this.getStr(), this.getWeapon().getStrScale());
                    }
                    break;
                case 1:
                    if(!this.getWeapon().getDexScale().contentEquals("--")){
                        percent += EquipmentBehaviour.scaleCalculator(this.getDex(), this.getWeapon().getDexScale());
                    }
                    break;
                case 2:
                    if(!this.getWeapon().getIntScale().contentEquals("--")){
                        percent += EquipmentBehaviour.scaleCalculator(this.getIntel(), this.getWeapon().getIntScale());
                    }
                    break;
                case 3:
                    if(!this.getWeapon().getConScale().contentEquals("--")){
                        percent += EquipmentBehaviour.scaleCalculator(this.getCon(), this.getWeapon().getConScale());
                    }
                    break;
                case 4:
                    if(!this.getWeapon().getWisScale().contentEquals("--")){
                        percent += EquipmentBehaviour.scaleCalculator(this.getWis(), this.getWeapon().getWisScale());
                    }
                    break;
            }

            skillCastSpeed = (100 - percent) / 100f;
        }
    }

    public void removeStatus(String name) {
        status.remove(name);
    }

    @Override
    public int getWeight() {
        return getCon();
    }

    public List<Skill> getAllSkill() {
        List<Skill> skillLst = new ArrayList<>();
        if (this.getActiveSkill() != null) skillLst.addAll(this.getActiveSkill());

        if (this.getWeapon() != null && this.getWeapon().haveActiveSkill()) {
            skillLst.addAll(this.getWeapon().getActiveSkill());
        }
        return skillLst;
    }

}
