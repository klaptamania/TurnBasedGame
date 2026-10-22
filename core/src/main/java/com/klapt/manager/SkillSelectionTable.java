package com.klapt.manager;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.klapt.entity.Entity;
import com.klapt.main.Main;
import com.klapt.skill.Skill;

import java.util.List;

public class SkillSelectionTable extends Table {

    public SkillSelectionTable(Entity user, CombatManager manager){
        this.setFillParent(true);
        this.setName("SkillSelection");
        this.bottom().pad(15);

        // get skills
        List<Skill> skillLst = user.getAllSkill();

        if(skillLst.isEmpty()) return;

        int counter = 0;

        for(Skill s : skillLst){
            Stack stack = new Stack();
            String textureName = user.getAllSkill().get(counter).getName() + "Icon";
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
                        user.setMove("SkillActivation " + finalCounter);
                        manager.executePlayerSkillActivation();
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
