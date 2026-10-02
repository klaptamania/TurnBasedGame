package com.klapt.equipment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.klapt.main.GameScreen;
import com.klapt.main.Main;
import com.klapt.skill.Skill;

import java.util.ArrayList;
import java.util.List;

public class Item implements Json.Serializable{
    //=================Require===============//
    private int requiredCon, requiredDex, requiredInt, requiredStr, requiredWis;
    private String conScale, dexScale, intScale, strScale, wisScale;

    //==============Information==============//
    private String name;
    private String type;

    private transient Texture texture;

    //=================Stats=================//
    private int baseDamage;

    private int physicalDef, magicalDef;

    //================Skills================//
    String passivePath;
    String[] activeSkillPath = new String[5];

    Skill passive;
    List<Skill> activeSkill = new ArrayList<>();

    //Getter
    public String getName() { return name; }
    public int getBaseDamage() { return baseDamage; }
    public int getRequiredCon() { return requiredCon; }
    public int getRequiredDex() { return requiredDex; }
    public int getRequiredInt() { return requiredInt; }
    public int getRequiredStr() { return requiredStr; }
    public int getRequiredWis() { return requiredWis; }

    public String getType() { return type; }
    public int getPhysicalDef() { return physicalDef; }
    public int getMagicalDef() { return magicalDef; }
    public Texture getTexture() { return texture; }

    public String getConScale() {
        return conScale;
    }
    public String getDexScale() {
        return dexScale;
    }
    public String getIntScale() {
        return intScale;
    }
    public String getStrScale() {
        return strScale;
    }
    public String getWisScale() {
        return wisScale;
    }

    @Override
    public void write(Json json) {
        json.writeValue("name", name);
        json.writeValue("type", type);

        json.writeValue("requiredCon", requiredCon);
        json.writeValue("requiredDex", requiredDex);
        json.writeValue("requiredInt", requiredInt);

        json.writeValue("baseDamage", baseDamage);
        json.writeValue("physicalDef", physicalDef);
        json.writeValue("magicalDef", magicalDef);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.name = jsonData.getString("name", "Unknown Item");
        this.type = jsonData.getString("type", "None");

        this.requiredCon = jsonData.getInt("requiredCon", 0);
        this.requiredDex = jsonData.getInt("requiredDex", 0);
        this.requiredInt = jsonData.getInt("requiredInt", 0);
        this.requiredStr = jsonData.getInt("requiredStr", 0);
        this.requiredWis = jsonData.getInt("requiredWis", 0);

        this.conScale = jsonData.getString("conScale", "--");
        this.dexScale = jsonData.getString("dexScale", "--");
        this.intScale = jsonData.getString("intScale", "--");
        this.strScale = jsonData.getString("strScale", "--");
        this.wisScale = jsonData.getString("wisScale", "--");

        this.baseDamage = jsonData.getInt("baseDamage", 0);
        this.physicalDef = jsonData.getInt("physicalDef", 0);
        this.magicalDef = jsonData.getInt("magicalDef", 0);

        this.passivePath = jsonData.getString("passivePath", "");

        JsonValue skillPathsValue = jsonData.get("activeSkillPath");
        if (skillPathsValue != null) {
            this.activeSkillPath = skillPathsValue.asStringArray();

            for (String activeSkillPath : activeSkillPath) {
                if (activeSkillPath == null || activeSkillPath.isEmpty()) continue;
                activeSkill.add(GameScreen.json.fromJson(Skill.class, Gdx.files.internal(activeSkillPath)));
            }
        } else {
            this.activeSkillPath = new String[5];
        }

        this.texture = Main.getAm().getTexture(this.name);
    }


    public List<Skill> getActiveSkill() { return activeSkill; }

    public boolean haveActiveSkill() { return !activeSkill.isEmpty(); }
}
