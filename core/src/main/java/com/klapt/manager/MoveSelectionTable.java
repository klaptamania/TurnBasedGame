package com.klapt.manager;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.klapt.main.Main;
import com.klapt.movement.Move;
import com.klapt.entity.Entity;

import java.util.List;

public class MoveSelectionTable extends Table {
    public MoveSelectionTable(List<Move> moveList, Entity user, CombatManager manager){
        this.setFillParent(true);
        this.setName("MoveSelectionTable");
        this.bottom().pad(15);

        if(moveList.isEmpty()) return;

        int counter = 0;

        for(Move s : moveList){
            Stack stack = new Stack();
            String textureName = moveList.get(counter).getName() + "Icon";
            Button button = new ImageButton(new TextureRegionDrawable(
                Main.getAm().textureContain(textureName) ?
                    new TextureRegion(Main.getAm().getTexture(textureName)) :
                    new TextureRegion(Main.getAm().getTexture("Temper"))
            ));


            int finalCounter = counter;
            stack.add(button);

            if(!s.isUsed()){
                button.addListener(new ClickListener(){
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        user.setMove("MoveActivation " + finalCounter);
                        manager.executePlayerMoveActivation();
                    }
                });
            }else{
                button.addListener(new ClickListener(){
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        manager.showPlayerUI();
                    }
                });
                Label cdLabel = new Label(Integer.toString(s.getCdLeft()), Main.getAm().getSkin());
                cdLabel.setTouchable(Touchable.disabled);
                stack.add(cdLabel);
            }

            this.add(stack).size(64, 64).pad(15);
            counter++;
        }
    }
}
