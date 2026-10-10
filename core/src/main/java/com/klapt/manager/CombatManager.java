package com.klapt.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.klapt.entity.Entity;
import com.klapt.entity.EntityBehavior;
import com.klapt.rendering.EntityRenderer;
import com.klapt.factory.EntityFactory;
import com.klapt.main.GameScreen;
import com.klapt.skill.Skill;

import java.util.ArrayList;
import java.util.List;


public class CombatManager {

    public enum CombatState {
        START_TURN,
        WAIT_FOR_INPUT,
        ENEMY_THINKING,
        EXECUTE_ACTION,
        CHECK_WIN_LOSE
    }


    Entity player;
    Entity opponent;

    Entity firstTake, secondTake;
    boolean isTurnPrepared = false;

    Stage stage;

    float delayedProcessing = 0;
    boolean progress1 = false, progress2 = false;

    CombatState currentState = CombatState.START_TURN;

    float width, height;

    public CombatManager(float width, float height){
        this.width = width;
        this.height = height;


        stage = new Stage(new FitViewport(width, height));
        stage.setDebugAll(true);

        Gdx.input.setInputProcessor(stage);
    }

    public void addEntity(boolean isAlly, String filePath){
        Entity currentEntity = EntityFactory.getEntityFromJson(filePath, 1);

        currentEntity = EntityFactory.getEntityFromJson(filePath, 1);

        float divided = width / (2);
        currentEntity.setX(divided - (GameScreen.imageTile / 2f));
        currentEntity.setY(isAlly ? 250 : 500);

        if(isAlly){
            player = currentEntity;
        }else opponent = currentEntity;

        System.out.printf("%s have %d HP!\n", currentEntity.getName(), currentEntity.getHp());

    }

    public void update(float deltaTime){
        stage.act(deltaTime);

        switch(currentState){
            case START_TURN :
                if(player != null && player.isAlive() && opponent.isAlive()){
                    showPlayerUI();

                    currentState = CombatState.WAIT_FOR_INPUT;
                }
                break;
            case WAIT_FOR_INPUT :
                break;
            case ENEMY_THINKING :
                if(opponent != null && opponent.isAlive()){
                    opponentThinking();
                    currentState = CombatState.EXECUTE_ACTION;
                }

                break;
            case EXECUTE_ACTION :
                checkingTurn(deltaTime);

                break;
            case CHECK_WIN_LOSE :
                if(player.getHp() > 0 && opponent.getHp() > 0){
                    currentState = CombatState.START_TURN;
                    overTime();
                }

                break;
        }
    }
    private void startCombatState(){

        boolean opponentHasReflection = reflectionCheck(opponent);
        boolean playerHasReflection = reflectionCheck(player);

        if (opponentHasReflection && !playerHasReflection) {
            firstTake = opponent;
            secondTake = player;
        } else {
            firstTake = player;
            secondTake = opponent;
        }

        isTurnPrepared = true;
    }

    private void checkingTurn(float deltaTime){
        if(!isTurnPrepared){
            startCombatState();
        }
        executing(firstTake, secondTake, deltaTime);
    }

    private boolean reflectionCheck(Entity entity){
        String str = entity.getMove();

        if(str == null || !str.startsWith("SkillActivation ")) return false;

        try {
            int skillIndex = Integer.parseInt(str.substring("SkillActivation ".length()).trim());
            List<Skill> skills = entity.getAllSkill();

            if (skills != null && skillIndex >= 0 && skillIndex < skills.size()) {
                return "Reflection".equalsIgnoreCase(skills.get(skillIndex).getName());
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return false;
    }


    private void executing(Entity first, Entity second, float deltaTime) {
        delayedProcessing += deltaTime;
        if(!progress1){
            progress1 = true;
            moveProcessing(first);
        }

        if(!progress2 && delayedProcessing >= 0.4f){
            progress2 = true;
            moveProcessing(second);
        }

        if(delayedProcessing >= 1f){
            progress1 = false;
            progress2 = false;
            delayedProcessing -= 1f;
            isTurnPrepared = false;
            currentState = CombatState.CHECK_WIN_LOSE;
        }
    }

    private void overTime() {
        EntityBehavior.statusProcessing(player);
        EntityBehavior.statusProcessing(opponent);

        EntityBehavior.clearingStatus(player);
        EntityBehavior.clearingStatus(opponent);

        EntityBehavior.stunProgress(player);
        EntityBehavior.stunProgress(opponent);

        SkillManager.onCDSkill(player);
        SkillManager.onCDSkill(opponent);

        EntityBehavior.regenSP(player);
        EntityBehavior.regenSP(opponent);

        player.setAlreadyAttack(false);
        opponent.setAlreadyAttack(false);

        player.setCastSkill(false);
        opponent.setCastSkill(false);
    }

    private void moveProcessing(Entity entity){
        String str = entity.getMove();
        int firstSpace = str.indexOf(" ");
        String order = "", skillIndex = "";
        if (firstSpace != -1) {
            String part1 = str.substring(0, firstSpace);          // "Skill"
            String part2 = str.substring(firstSpace + 1).trim(); // "Mr Beast"

            order = part1;
            skillIndex = part2;
        }else{
            order = str;
        }
        switch(order){
            case "Attack":
                EntityBehavior.attack(entity, entity == player ? opponent : player, entity == player);
                break;
            case "SkillActivation":
                if(skillIndex.isEmpty()) return;
                SkillManager.skillActivation(entity, entity == player ? opponent : player, Integer.parseInt(skillIndex), entity == player );
                break;
        }

        entity.setMove("");
    }

    private void opponentThinking() {
        opponent.setAlreadyAttack(false);
        opponent.setMove("SkillActivation 0");
        System.out.println(opponent.getAllSkill().getFirst().getCdLeft());
    }

    public void setPlayerMoveSkillAct() {
        clearUI();

        List<Skill> skillLst = player.getAllSkill();

        if(skillLst.isEmpty()){
            showPlayerUI();
            return;
        }

        stage.addActor(new SkillSelectionTable(player, this));
    }

    public void executePlayerAttack() {
        clearUI();
        currentState = CombatState.ENEMY_THINKING;
    }

    public void executePlayerSkillActivation(){
        clearUI();
        currentState = CombatState.ENEMY_THINKING;
    }

    public void showPlayerUI() {
        clearUI();

        stage.addActor(new MoveSetTable(player, this));
    }

    private void clearUI() { stage.getRoot().clearChildren(); }

    public void draw(SpriteBatch batch, Texture bg, float deltaTime){
        batch.draw(bg, 0, 0, width, height);

        EntityRenderer.draw(batch, player, player.getX(), player.getY(), deltaTime);

        EntityRenderer.draw(batch, opponent, opponent.getX(), opponent.getY(), deltaTime);
    }

    public static int getRandomAliveIndex(List<Entity> listed) {
        List<Integer> aliveIndices = new ArrayList<>();
        for (int i = 0; i < listed.size(); i++) {
            if (listed.get(i).isAlive()) aliveIndices.add(i);
        }
        if (aliveIndices.isEmpty()) return -1;
        return aliveIndices.get(MathUtils.random(0, aliveIndices.size() - 1));
    }

    public void drawStage(){
        stage.draw();
    }
}
