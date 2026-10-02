package com.klapt.manager;

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

public class MoveSetTable extends Table {
    Button button;
    String name = "";

    public MoveSetTable(Entity user){
        this.bottom();
        this.setFillParent(true);
        this.setName(user.getName() + "SelectMove");
        button = new ImageButton(new TextureRegionDrawable(new TextureRegion(Main.getAm().getTexture("Person"))));
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y){
                user.setMove("Attack");
            }
        });

        this.add(button);
    }
}
