package com.klapt.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.klapt.entity.Entity;
import com.klapt.entity.EntityTexture;
import com.klapt.equipment.Item;
import com.klapt.main.GameScreen;
import com.klapt.main.Main;

import java.util.List;

public class SelectEntityTable extends Table {
    Button[] button;
    List<Entity> list;
    String entityName = "";

    public SelectEntityTable(List<Entity> selectionList){
        this.setFillParent(true);
        this.setName("EntitySelection");
        this.list = selectionList;
        this.bottom().pad(15);

        button = new Button[list.size()];
        Button.ButtonStyle transparentStyle = new Button.ButtonStyle();
        for(int i = 0; i < list.size(); i++){
            Entity e = list.get(i);

            EntityTexture texture = e.getTexture();

            Image baseCharacter = new Image(texture.getTexture());
            Stack characterStack = new Stack();
            characterStack.add(baseCharacter);

            for(Item j : texture.getArmor()){
                if(j != null){
                    Image current = new Image(j.getTexture());

                    if(j.getName().equals("Wizard Hat")){
                        current.setScaling(Scaling.none);
                        current.setAlign(Align.bottom);
                    }

                    characterStack.add(current);
                }
            }

            characterStack.add(new Image(texture.getWeapon()));

            button[i] = new Button(transparentStyle);
            button[i].add(characterStack);
            button[i].setClip(true);
            button[i].addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y){
                    entityName = e.getName();
                }
            });
            this.add(button[i]).size(64, 64).pad(15);
        }

    }

    public String getEntityName() { return entityName; }
}
