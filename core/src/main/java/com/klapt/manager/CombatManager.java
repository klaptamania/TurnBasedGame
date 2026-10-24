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
import com.klapt.factory.EntityFactory;
import com.klapt.main.GameScreen;
import com.klapt.movement.Move;
import com.klapt.rendering.EntityRenderer;
import com.klapt.skill.Skill;
import com.klapt.utilityTool.QuickGacha;

import java.util.ArrayList;
import java.util.List;


public class CombatManager {

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
        stage.addListener(new ClickListener(Input.Buttons.RIGHT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (stage.getRoot().findActor("SkillSelection") != null) {
                    showPlayerUI();
                }
            }
        });
    }

    public static int getRandomAliveIndex(List<Entity> listed) {
        List<Integer> aliveIndices = new ArrayList<>();
        for (int i = 0; i < listed.size(); i++) {
            if (listed.get(i).isAlive()) aliveIndices.add(i);
        }
        if (aliveIndices.isEmpty()) return -1;
        return aliveIndices.get(MathUtils.random(0, aliveIndices.size() - 1));
    }

    public void addEntity(boolean isAlly, String filePath) {
        Entity currentEntity = EntityFactory.getEntityFromJson(filePath, 1, isAlly);

        float divided = width / (2);

        if (isAlly) {
            divided -= 50f;
        } else divided += 50f;

        currentEntity.setX(divided - (GameScreen.imageTile / 2f));
        currentEntity.setY(isAlly ? 250 : 400);

        if (isAlly) {
            player = currentEntity;
            player.setName("Player");
        } else opponent = currentEntity;

        System.out.printf("%s have %d HP!\n", currentEntity.getName(), currentEntity.getHp());

    }

    private Move getSkillFromMove(Entity entity) {
        Move skill = null;

        String skillString = entity.getMove();
        if (skillString != null && skillString.startsWith("SkillActivation ")) {
            int skillIndex = Integer.parseInt(skillString.substring("SkillActivation ".length()).trim());
            skill = entity.getAllSkill().get(skillIndex);
        }else if (skillString != null && skillString.startsWith("MoveActivation ")) {
            int skillIndex = Integer.parseInt(skillString.substring("MoveActivation ".length()).trim());
            skill = entity.getMoveSet().get(skillIndex);
        }

        return skill;
    }


    private boolean skillNameCheck(Entity entity, String checker) {
        String str = entity.getMove();

        if (str == null || !str.startsWith("SkillActivation ")) return false;

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

    private void actionTimeProcessor(float deltaTime) {
        System.out.printf("[ATTENTION first] Player: %f | Opponent: %f\n", player.getActionTime(), opponent.getActionTime());
        //Get skill of two
        Move pSkill = null, oSkill = null;
        float pActionTime = player.getActionTime(), oActionTime = opponent.getActionTime();

        if (pActionTime <= 0 && player.getCurrentState() == Entity.State.CASTING) {
            pSkill = getSkillFromMove(player);
        }
        if (oActionTime <= 0 && opponent.getCurrentState() == Entity.State.CASTING) {
            oSkill = getSkillFromMove(opponent);
        }

        //Get cast time
        float pSCastTime = (pSkill != null) ? pSkill.getSkillCastSpeed() + pActionTime : 0;
        float oSCastTime = (oSkill != null) ? oSkill.getSkillCastSpeed() + oActionTime : 0;
        System.out.printf("[ATTENTION cast Time] Player: %f | Opponent: %f\n", pSCastTime, oSCastTime);


        //Comparing
        if (player.isThisState(Entity.State.CASTING) && opponent.isThisState(Entity.State.CASTING)) {// if both are casting skill at the same time
            if (pSCastTime < oSCastTime) { //if player faster than opponent
                firstTake = player;
                secondTake = opponent;
            } else if (pSCastTime > oSCastTime) { //if player slower than opponent
                firstTake = opponent;
                secondTake = player;
            } else {                            //if both
                if(MathUtils.randomBoolean()){
                    firstTake = player;
                    secondTake = opponent;
                }else{
                    firstTake = opponent;
                    secondTake = player;
                }

                player.setActionTime((pSkill != null) ? pSkill.getSkillRecoverTime() : 1.0f);
                opponent.setActionTime((oSkill != null) ? oSkill.getSkillRecoverTime() : 1.0f);
                opponent.setCurrentState(Entity.State.RECOVERY);

                isTurnPrepared = true;
                return;
            }
        }else{
            // Check if both are recovery
            if(player.isThisState(Entity.State.RECOVERY) && opponent.isThisState(Entity.State.RECOVERY)){
                float min = Math.min(player.getActionTime(), opponent.getActionTime());
                player.setActionTime(player.getActionTime() - min);
                opponent.setActionTime(opponent.getActionTime() - min);

                if(player.getActionTime() == 0 && opponent.getActionTime() != 0){
                    currentState = CombatState.START_TURN;
                    player.setCurrentState(Entity.State.IDLE);
                }else if(player.getActionTime() != 0 && opponent.getActionTime() == 0){
                    currentState = CombatState.ENEMY_THINKING;
                    opponent.setCurrentState(Entity.State.IDLE);
                }else{
                    currentState = CombatState.START_TURN;
                    opponent.setCurrentState(Entity.State.IDLE);
                    player.setCurrentState(Entity.State.IDLE);
                }
                System.out.printf("[ATTENTION recovery] Player: %f | Opponent: %f\n", player.getActionTime(), opponent.getActionTime());
                return;
            }
            pActionTime = player.getActionTime();
            oActionTime = opponent.getActionTime();

            // Check if opponent recovered while player is still casting
            if(player.isThisState(Entity.State.CASTING) && opponent.isThisState(Entity.State.RECOVERY)){
                if(pActionTime == 0){
                    pActionTime = getSkillFromMove(player).getSkillCastSpeed();
                }

                if(oActionTime - pActionTime <= 0){
                    currentState = CombatState.ENEMY_THINKING;
                    player.setActionTime(player.getActionTime() - Math.abs(oActionTime));
                    oActionTime = 0;
                    opponent.setActionTime(oActionTime);
                    opponent.setCurrentState(Entity.State.IDLE);
                    isTurnPrepared = false;
                    System.out.printf("[ATTENTION recovered before!] Player: %f | Opponent: %f\n", player.getActionTime(), opponent.getActionTime());
                    return;
                }
            }
            // Check if player recovered while opponent is still casting
            if(player.isThisState(Entity.State.RECOVERY) && opponent.isThisState(Entity.State.CASTING)){
                if(oActionTime == 0){
                    oActionTime = getSkillFromMove(opponent).getSkillCastSpeed();
                }

                if(pActionTime - oActionTime <= 0){
                    currentState = CombatState.START_TURN;
                    opponent.setActionTime(opponent.getActionTime() - Math.abs(pActionTime));
                    pActionTime = 0;
                    player.setActionTime(pActionTime);
                    player.setCurrentState(Entity.State.IDLE);
                    isTurnPrepared = false;
                    System.out.printf("[ATTENTION recovered before!] Player: %f | Opponent: %f\n", player.getActionTime(), opponent.getActionTime());
                    return;
                }
            }

            // Check if player recover faster
            if(player.isThisState(Entity.State.RECOVERY)){
                if(pActionTime - oActionTime <= 0){
                    currentState = CombatState.START_TURN;
                    player.setActionTime(0);
                    opponent.setActionTime(oActionTime - pActionTime);
                    player.setCurrentState(Entity.State.IDLE);
                    isTurnPrepared = false;
                    System.out.printf("[ATTENTION quick Recover] Player: %f | Opponent: %f\n", player.getActionTime(), opponent.getActionTime());
                    return;
                }
            }
            // Check if opponent recover faster
            if(opponent.isThisState(Entity.State.RECOVERY)){
                if(oActionTime - pActionTime <= 0){
                    currentState = CombatState.ENEMY_THINKING;
                    opponent.setActionTime(0);
                    player.setActionTime(pActionTime - oActionTime);
                    opponent.setCurrentState(Entity.State.IDLE);
                    isTurnPrepared = false;
                    System.out.printf("[ATTENTION quick Recover] Player: %f | Opponent: %f\n", player.getActionTime(), opponent.getActionTime());
                    return;
                }
            }

            if(player.isThisState(Entity.State.CASTING)){
                firstTake = player;
                secondTake = opponent;
            }
            if(opponent.isThisState(Entity.State.CASTING)){
                firstTake = opponent;
                secondTake = player;
            }
        }

        Move fSkill = getSkillFromMove(firstTake);
        Move sSkill = getSkillFromMove(secondTake);

        if (sSkill != null && fSkill != null) { //reduce action time due to player's cast speed
            secondTake.setActionTime((sSkill.getSkillCastSpeed() + secondTake.getActionTime()) - (fSkill.getSkillCastSpeed() + firstTake.getActionTime()));
        } else {
            secondTake.setActionTime(0f);
        }

        firstTake.setActionTime((fSkill != null) ? fSkill.getSkillRecoverTime() : 1.0f);
        firstTake.setCurrentState(Entity.State.RECOVERY);

        isTurnPrepared = true;
        System.out.printf("[ATTENTION end] Player: %f %b | Opponent: %f %b\n", player.getActionTime(), player.isThisState(Entity.State.RECOVERY), opponent.getActionTime(), opponent.isThisState(Entity.State.RECOVERY));

    }

    private void executing(Entity first, Entity second, float deltaTime) {
        delayedProcessing += deltaTime;
        if (!progress1) {
            progress1 = true;
            moveProcessing(first);
            if(first.isThisState(Entity.State.CASTING)) first.setCurrentState(Entity.State.RECOVERY);
        }

        if (!progress2 && delayedProcessing >= 0.4f) {
            progress2 = true;
            if(second.isThisState(Entity.State.CASTING)){
                Move s = getSkillFromMove(second);
                float fullTime = s.getSkillCastSpeed() - second.getActionTime();
                if(fullTime >= s.getSkillCastNeed()){
                    moveProcessing(second);
                    second.setActionTime(s.getSkillRecoverTime());
                }
                second.setCurrentState(Entity.State.RECOVERY);
                System.out.printf("[ATTENTION extra!] Player: %f %b | Opponent: %f %b\n", player.getActionTime(), player.isThisState(Entity.State.RECOVERY), opponent.getActionTime(), opponent.isThisState(Entity.State.RECOVERY));
            }
        }
//
        if (delayedProcessing >= 1f) {
            progress1 = false;
            progress2 = false;

            if(first.getActionTime() == 0) {
                first.setMove("");
                first.setCurrentState(Entity.State.IDLE);
            }
            if(second.getActionTime() == 0) {
                second.setMove("");
                second.setCurrentState(Entity.State.IDLE);
            }

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

        MoveManager.onCDSkill(player);
        MoveManager.onCDSkill(opponent);

        EntityBehavior.regenSP(player);
        EntityBehavior.regenSP(opponent);

        player.setAlreadyAttack(false);
        opponent.setAlreadyAttack(false);

        player.setCastSkill(false);
        opponent.setCastSkill(false);

        player.setBeingHit(false);
        opponent.setBeingHit(false);
    }

    private void moveProcessing(Entity entity) {
        String str = entity.getMove();
        int firstSpace = str.indexOf(" ");
        String order = "", skillIndex = "";
        if (firstSpace != -1) {
            String part1 = str.substring(0, firstSpace);          // "Skill"
            String part2 = str.substring(firstSpace + 1).trim(); // "Mr Beast"

            order = part1;
            skillIndex = part2;
        } else {
            order = str;
        }
        switch (order) {
            case "Attack":
                EntityBehavior.attack(entity, entity == player ? opponent : player, entity == player);
                break;
            case "MoveActivation":
                if(skillIndex.isEmpty()) return;
                MoveManager.moveActivation(entity,entity == player ? opponent : player, Integer.parseInt(skillIndex), entity == player);
                break;
            case "SkillActivation":
                if (skillIndex.isEmpty()) return;
                MoveManager.skillActivation(entity, entity == player ? opponent : player, Integer.parseInt(skillIndex), entity == player);
                break;
            case "Waiting":
                entity.addStatusCondition("STUN", 1);
                break;
        }
    }

    private void opponentThinking() {
        opponent.setAlreadyAttack(false);

        List<Move> moveList = List.of(((Enemy) opponent).getMoves());
        List<Move> activeList = new ArrayList<>();
        for (Move move : moveList) {
            if(!move.isUsed()) activeList.add(move);
        }

        QuickGacha<Move> moves = new QuickGacha<>(activeList);

        Move move = moves.roll();
        String[] arr = move.getName().split(" ");

        switch (arr[0]) {
            case "M":
                opponent.setMove("MoveActivation " + arr[1]);
                System.out.println("MoveActivation " + getSkillFromMove(opponent).getName());
                break;
            case "S":
                opponent.setMove("SkillActivation " + arr[1]);
                System.out.println("SkillActivation " + getSkillFromMove(opponent).getName());
                break;
            default:
                opponent.setMove("Waiting");
                break;
        }
    }

    public void setPlayerMoveSkillAct() {
        clearUI();

        List<Skill> skillLst = player.getAllSkill();

        if (skillLst.isEmpty()) {
            showPlayerUI();
            return;
        }

        stage.addActor(new SkillSelectionTable(player, this));
    }

    public void executeNormalMove() {
        clearUI();

        List<Move> moveSetLst = player.getMoveSet();
        if(moveSetLst.isEmpty()){
            showPlayerUI();
            return;
        }
        stage.addActor(new MoveSelectionTable(moveSetLst, player, this));
    }

    public void executePlayerSkillActivation() {
        clearUI();
        currentState = CombatState.ENEMY_THINKING;
    }
    public void executePlayerMoveActivation() {
        clearUI();
        currentState = CombatState.ENEMY_THINKING;
    }

    public void showPlayerUI() {
        clearUI();

        stage.addActor(new MoveSetTable(player, this));
    }

    private void clearUI() {
        stage.getRoot().clearChildren();
    }

    public void draw(SpriteBatch batch, Texture bg, float deltaTime) {
        batch.draw(bg, 0, 0, width, height);

        EntityRenderer.draw(batch, player, player.getX(), player.getY(), deltaTime);

        EntityRenderer.draw(batch, opponent, opponent.getX(), opponent.getY(), deltaTime);
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void drawStage() {
        stage.draw();
    }

    public void update(float deltaTime) {
        stage.act(deltaTime);

        switch (currentState) {
            case START_TURN:
                if (player != null && player.isAlive() && opponent.isAlive()) {
                    if (player.containStatus("STUN")) {
                        currentState = CombatState.ENEMY_THINKING;
                        break;
                    }

                    if (player.isThisState(Entity.State.IDLE)) {
                        System.out.println("Player turn");
                        showPlayerUI();
                        player.setCurrentState(Entity.State.CASTING);
                        currentState = CombatState.WAIT_FOR_INPUT;
                    } else currentState = CombatState.ENEMY_THINKING;

                }
                break;
            case WAIT_FOR_INPUT:
                break;
            case ENEMY_THINKING:
                if (opponent != null && opponent.isAlive()) {
                    if (opponent.isThisState(Entity.State.IDLE)) {
                        opponentThinking();
                        System.out.println("Opponent turn");
                        opponent.setCurrentState(Entity.State.CASTING);
                    }

                    currentState = CombatState.EXECUTE_ACTION;
                }

                break;
            case EXECUTE_ACTION:
                if(!isTurnPrepared) actionTimeProcessor(deltaTime);
                if(isTurnPrepared) executing(firstTake, secondTake,deltaTime);

                break;
            case CHECK_WIN_LOSE:
                if (player.getHp() > 0 && opponent.getHp() > 0) {
                    currentState = CombatState.START_TURN;
                    overTime();
                } else {
                    if (player.getHp() <= 0 && player.isAlive()) {
                        player.callOfDeath();
                        System.out.println(player.getName() + " fainted.");
                    }
                    if (opponent.getHp() <= 0 && opponent.isAlive()) {
                        opponent.callOfDeath();
                        System.out.println(opponent.getName() + " fainted.");
                    }
                }
                break;
        }
    }

    public enum CombatState {
        START_TURN,
        WAIT_FOR_INPUT,
        ENEMY_THINKING,
        EXECUTE_ACTION,
        CHECK_WIN_LOSE
    }
}
