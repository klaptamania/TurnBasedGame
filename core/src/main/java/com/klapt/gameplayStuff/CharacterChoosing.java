package com.klapt.gameplayStuff;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.klapt.entity.Entity;
import com.klapt.main.GameScreen;
import com.klapt.main.Main;


public class CharacterChoosing extends Table {
    Button button;
    String name = "";

    public CharacterChoosing(){
        this.setFillParent(true);
        this.setName("SelectionTable");
        button = new ImageButton(new TextureRegionDrawable(new TextureRegion(Main.getAm().getTexture("Person"))));
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y){
                name = "Person";
            }
        });

        this.add(button);
    }

    public Entity getEntity(){
        Entity entity = null;
        switch (name){
            case "Person":
                entity = GameScreen.json.fromJson(Entity.class, Gdx.files.internal("Json/Entity/Sample/Human.json"));
                entity.load(0);
                break;
            case "Samurai":
                entity = GameScreen.json.fromJson(Entity.class, Gdx.files.internal("Json/Entity/Sample/Samurai.json"));
                entity.load(0);
                break;
        }

        return entity;
    }
}
