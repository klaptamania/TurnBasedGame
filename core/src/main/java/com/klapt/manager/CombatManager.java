package com.klapt.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.klapt.entity.Entity;
import com.klapt.entity.EntityBehavior;
import com.klapt.gameplayStuff.CharacterChoosing;
import com.klapt.rendering.EntityRenderer;
import com.klapt.factory.EntityFactory;
import com.klapt.main.GameScreen;
import com.klapt.utilityTool.QuickGacha;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CombatManager {

    List<Entity> ally = new ArrayList<>();
    List<Entity> enemy = new ArrayList<>();

    Stage stage;

    boolean turn = true;
    int iA = 0, iE = 0;
    float running = 0;
    float width, height;

    public CombatManager(float width, float height){
        this.width = width;
        this.height = height;


        stage = new Stage(new FitViewport(width, height));
        stage.setDebugAll(true);

        Gdx.input.setInputProcessor(stage);
    }

    public void addEntity(boolean isAlly, String filePath){
        List<Entity> currentList = isAlly ? ally : enemy;

        currentList.add(EntityFactory.getEntityFromJson(filePath, currentList.size()));

        int count = currentList.size();
        float divided = width / (count + 1);

        for(int i = 0; i < count; i++){
            currentList.get(i).setX((i + 1) * divided - (GameScreen.imageTile / 2f));
            currentList.get(i).setY(isAlly ? 250 : 500);

            System.out.printf("%s have %d HP!\n", currentList.get(i).getName(), currentList.get(i).getHp());
        }
    }

    public void combat(float deltaTime){
        List<Entity> currentList = turn ? ally : enemy;
        List<Entity> currentDList = turn ? enemy : ally;
        int index = turn ? iA : iE;

        Entity currentEntity = currentList.get(index);

        MoveSetTable moveSetTable = stage.getRoot().findActor( currentEntity.getName()+ "SelectMove");
        if(moveSetTable== null && currentEntity.getMove() == null){
            stage.addActor(new MoveSetTable(currentEntity));
        }

        if(currentEntity.getMove() == null || currentEntity.getMove().contentEquals("")){
            stage.act(deltaTime);
            return;
        }

        switch(currentEntity.getMove()){
            case "Attack":
                SelectEntityTable selectEntityTable = stage.getRoot().findActor( "EntitySelection");

                if(selectEntityTable == null){
                    if (moveSetTable != null) {
                        moveSetTable.remove();
                    }
                    selectEntityTable = new SelectEntityTable(currentDList);
                    stage.addActor(selectEntityTable);
                }
                String name = selectEntityTable.getEntityName();

                for(Entity e : currentDList){
                    if(e.getName().contentEquals(name)){
                        EntityBehavior.attack(currentEntity, e, turn);
                    }
                }

                break;
            case "Skill":
                break;
        }

        stage.act(deltaTime);
    }

    private int getRandomUnattackedIndex(List<Entity> listed){
        int index = -1;
        List<Integer> unAttackList = new ArrayList<>();

        //Take all un attack entity
        for(int i = 0; i < listed.size(); i++){
            if(listed.get(i).isAlive() && !listed.get(i).isAlreadyAttack()) unAttackList.add(i);
        }

        //Return -1 if there's all attacked
        if(unAttackList.isEmpty()) return index;

        //Return the index of random none attack entity
        index = MathUtils.random(0, unAttackList.size() - 1);

        return unAttackList.get(index);
    }

    public static int getRandomAliveIndex(List<Entity> listed) {
        int index = -1;

        List<Integer> aliveIndices = new ArrayList<>();
        //Take all alive entity
        for(int i = 0; i < listed.size(); i++){
            if(listed.get(i).isAlive() ) aliveIndices.add(i);
        }

        //Return -1 if there's no alive entity
        if(aliveIndices.isEmpty()) return index;

        //Return the index of random alive entity
        index = MathUtils.random(0, aliveIndices.size() - 1);

        return aliveIndices.get(index);
    }

    public void draw(SpriteBatch batch, Texture bg, float deltaTime){
        int count = ally.size();
        float divided = width / (count + 1);
        batch.draw(bg, 0, 0, width, height);

        //Allies
        for(int i = 0; i < count; i++){
            float x = (i + 1) * divided - (GameScreen.imageTile / 2f);
            EntityRenderer.draw(batch, ally.get(i), x, 250, deltaTime);
        }


        //Enemy
        count = enemy.size();
        divided = width / (count + 1);

        for(int i = 0; i < count; i++){
            float x = (i + 1) * divided - (GameScreen.imageTile / 2f);
            EntityRenderer.draw(batch, enemy.get(i), x, 500, deltaTime);
        }

    }

    public void drawStage(){
        stage.draw();
    }
}
