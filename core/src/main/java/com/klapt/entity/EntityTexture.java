package com.klapt.entity;

import com.badlogic.gdx.graphics.Texture;
import com.klapt.equipment.Item;
import com.klapt.main.Main;
import lombok.Getter;

@Getter
public class EntityTexture {
    Texture texture;
    Texture weapon;
    Item[] armor = new Item[5];

    public EntityTexture(String texture, String weaponName, Item[] items) {
        this.texture = Main.getAm().getTexture(texture);

        weapon = Main.getAm().getTexture(weaponName);

        System.arraycopy(items, 0, armor, 0, 5);
    }

}
