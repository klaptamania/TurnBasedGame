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
    Button[] button;

    public MoveSetTable(Entity user, CombatManager manager){
        this.bottom();
        this.setFillParent(true);
        this.setName(user.getName() + "SelectMove");
        this.bottom().pad(15);

        button = new Button[4];

        button[0] = new ImageButton(new TextureRegionDrawable(new TextureRegion(Main.getAm().getTexture("Person"))));
        button[0].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y){
                user.setMove("Attack");
                manager.executePlayerAttack();
            }
        });
        button[1] = new ImageButton(new TextureRegionDrawable(new TextureRegion(Main.getAm().getTexture("Person"))));
        button[1].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y){
                user.setMove("SkillActivation");
                manager.setPlayerMoveSkillAct();
            }
        });


        this.add(button[0]).size(64, 64).pad(15);
        this.add(button[1]).size(64, 64).pad(15);
    }
}
