package com.klapt.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.klapt.entity.Enemy;
import com.klapt.entity.Entity;
import com.klapt.entity.EntityBehavior;
import com.klapt.movement.Move;
import com.klapt.rendering.EntityRenderer;
import com.klapt.factory.EntityFactory;
import com.klapt.main.GameScreen;
import com.klapt.skill.Skill;
import com.klapt.utilityTool.QuickGacha;

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

    public CombatManager(float width, float height) {
        this.width = width;
        this.height = height;


        stage = new Stage(new FitViewport(width, height));
        stage.setDebugAll(true);

        Gdx.input.setInputProcessor(stage);
        stage.addListener(new ClickListener(Input.Buttons.RIGHT){
           @Override
           public void clicked(InputEvent event, float x, float y){
                if(stage.getRoot().findActor("SkillSelection") != null){
                    showPlayerUI();
                }
           }
        });
    }


    public void addEntity(boolean isAlly, String filePath){
        Entity currentEntity = EntityFactory.getEntityFromJson(filePath, 1, isAlly);

        float divided = width / (2);

        if(isAlly){
            divided -= 50f;
        }else divided += 50f;

        currentEntity.setX(divided - (GameScreen.imageTile / 2f));
        currentEntity.setY(isAlly ? 200 : 400);

        if(isAlly){
            player = currentEntity;
        }else opponent = currentEntity;

        System.out.printf("%s have %d HP!\n", currentEntity.getName(), currentEntity.getHp());

    }

    private Skill getSkillFromMove(Entity entity){
        Skill skill = null;

        String skillString = entity.getMove();
        if(skillString != null && skillString.startsWith("SkillActivation ")){
            int skillIndex = Integer.parseInt(skillString.substring("SkillActivation ".length()).trim());
            skill = entity.getAllSkill().get(skillIndex);
        }

        return skill;
    }

    private void startCombatState(){
        //Checking if actionTime > 0

        //Compare two actionTime
        Skill pSkill = getSkillFromMove(player), oSkill = getSkillFromMove(opponent);

        Skill fSkill, sSkill;

        if(pSkill.getSkillCastSpeed() > oSkill.getSkillCastSpeed()){
            firstTake = player;
            secondTake = opponent;
            fSkill = pSkill;
            sSkill = oSkill;
        }else if(pSkill.getSkillCastSpeed() < oSkill.getSkillCastSpeed()){
            firstTake = opponent;
            secondTake = player;
            fSkill = oSkill;
            sSkill = pSkill;
        }else{
            player.setActionTime(pSkill.getSkillRecoverTime());
            opponent.setActionTime(oSkill.getSkillRecoverTime());
            player.setSkillCasting(true);
            opponent.setSkillCasting(true);

            firstTake = player;
            secondTake = opponent;

            isTurnPrepared = true;
            return;
        }

        //Reorder
        secondTake.setActionTime(secondTake.getActionTime() - sSkill.getSkillCastSpeed());
        firstTake.setActionTime(fSkill.getSkillRecoverTime());
        firstTake.setSkillCasting(true);

        isTurnPrepared = true;
    }

    private void checkingTurn(float deltaTime){
        if(!isTurnPrepared){
            startCombatState();
        }
        executing(firstTake, secondTake, deltaTime);
    }

    private boolean skillNameCheck(Entity entity, String checker){
        String str = entity.getMove();

        if(str == null || !str.startsWith("SkillActivation ")) return false;

        try {
            int skillIndex = Integer.parseInt(str.substring("SkillActivation ".length()).trim());
            List<Skill> skills = entity.getAllSkill();

            if (skills != null && skillIndex >= 0 && skillIndex < skills.size()) {
                return checker.equalsIgnoreCase(skills.get(skillIndex).getName());
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
            delayedProcessing = 0f;
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

        player.setBeingHit(false);
        opponent.setBeingHit(false);
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
            case "Waiting":
                player.addStatusCondition("STUN", 1);
                break;
        }

        entity.setMove("");
    }

    private void opponentThinking() {
        opponent.setAlreadyAttack(false);

        List<Move> moveList = List.of(((Enemy) opponent).getMoves());
        List<Move> activeList = new ArrayList<>();
        activeList.add(moveList.get(0));
        activeList.add(moveList.get(1));
        for(Move move : moveList){
            if(!move.getName().contentEquals("Attack") && !move.getName().contentEquals("Waiting")){
                int index = Integer.parseInt(move.getName());

                if(!opponent.getAllSkill().get(index).isUsed()){
                    activeList.add(move);
                }
            }
        }

        QuickGacha<Move> moves = new QuickGacha<>(activeList);

        Move move = moves.roll();

        switch(move.getName()){
            case "Attack":
                opponent.setMove("Attack");
                break;
            case "Waiting":
                opponent.setMove("Waiting");
                break;
            default:
                opponent.setMove("SkillActivation " + move.getName());
                break;
        }
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

    public void executeNormalMove() {
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
    public void resize(int width, int height){
        stage.getViewport().update(width, height, true);
    }

    public void drawStage(){
        stage.draw();
    }

    public void update(float deltaTime){
        stage.act(deltaTime);

        switch(currentState){
            case START_TURN :
                if(player != null && player.isAlive() && opponent.isAlive()){
                    if(player.containStatus("STUN")){
                        currentState = CombatState.ENEMY_THINKING;
                        break;
                    }

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
                }else{
                    if(player.getHp() <= 0 && player.isAlive()){
                        player.callOfDeath();
                        System.out.println(player.getName() + " fainted.");
                    }
                    if(opponent.getHp() <= 0 && opponent.isAlive()){
                        opponent.callOfDeath();
                        System.out.println(opponent.getName() + " fainted.");
                    }
                }

                break;
        }
    }
}
