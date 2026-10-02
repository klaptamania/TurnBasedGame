package com.klapt.entity;

import com.badlogic.gdx.graphics.Texture;
import com.klapt.equipment.Item;
import com.klapt.main.Main;

public class EntityTexture {
    Texture texture;
    Texture weapon;
    Item[] armor = new Item[4];

    public Texture getTexture() {
        return texture;
    }

    public Texture getWeapon() {
        return weapon;
    }

    public Item[] getArmor() {
        return armor;
    }

    public EntityTexture(String texture, String weaponName, Item[] items){
        this.texture = Main.getAm().getTexture(texture);

        weapon = Main.getAm().getTexture(weaponName);

        for(int i = 0; i < 4; i++){
            armor[i] = items[i];
        }
    }

}
