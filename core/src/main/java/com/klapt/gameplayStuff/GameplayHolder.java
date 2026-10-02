package com.klapt.gameplayStuff;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.klapt.entity.Entity;

public class GameplayHolder {
    Stage stage;
    Entity trainee;

    public GameplayHolder(){
        stage = new Stage(new FitViewport(360, 640));

        stage.addActor(new CharacterChoosing());

        Gdx.input.setInputProcessor(stage);
    }

    //===============RENDER=====================//

    public void update(float deltaTime){
        CharacterChoosing selectionTable = stage.getRoot().findActor("SelectionTable");

        if (selectionTable != null) {
            Entity selectedEntity = selectionTable.getEntity();
            if (selectedEntity != null) {
                trainee = selectedEntity;
                selectionTable.remove();
            }
        }

        stage.act(deltaTime);
    }

    //===================DRAW====================//

    public void draw(SpriteBatch batch, float deltaTime){
        if(trainee != null){
            batch.draw(trainee.getTexture().getTexture(), 300, 300);
        }

    }

    public void drawStage(){
        stage.draw();
    }

    //===============OTHER FUNCTION==================//

    public void resize(int width, int height){
        stage.getViewport().update(width, height, true);
    }
    public void dispose(){
        stage.dispose();
    }
}
