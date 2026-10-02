package com.klapt.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.klapt.gameplayStuff.GameplayHolder;
import com.klapt.manager.CombatManager;
import com.klapt.rendering.EffectRenderer;
import com.klapt.rendering.TextDamageRenderer;
import com.klapt.skill.Effect;
import com.klapt.utilityTool.AnimationHolder;
import com.klapt.utilityTool.LineDrawer;

public class GameScreen implements Screen {
    protected OrthographicCamera cam;
    protected Viewport vp;
    protected SpriteBatch batch;

    protected CombatManager cm;

    public static final int imageTile = 78;

    public static final Json json = new Json();

    private boolean active = false, running = false;

    private final GameplayHolder gHolder = new GameplayHolder();

    public GameScreen(Main main){
        batch = main.getBatch();

        cam = new OrthographicCamera();
        vp = new FitViewport(360, 640, cam);
        cm = new CombatManager(vp.getWorldWidth(), vp.getWorldHeight());
        cm.addEntity(true, "Json/Entity/Sample/Wizard.json");
        cm.addEntity(true, "Json/Entity/Sample/Samurai.json");
        cm.addEntity(false, "Json/Entity/Sample/Samurai.json");
        cm.addEntity(false, "Json/Entity/Sample/Wizard.json");
        cm.addEntity(false, "Json/Entity/Sample/Dummy.json");

        TextDamageRenderer.loadFont(new Color(255,255,255,1));
        AnimationHolder.loadAllAnimations();
    }

    @Override
    public void show() {
        cam.position.set(vp.getWorldWidth() / 2f, vp.getWorldHeight() / 2f, 0);
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw(delta);
    }

    @Override
    public void resize(int width, int height) {
        vp.update(width, height, true);
        gHolder.resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        gHolder.dispose();
    }

    //=========================DRAW=================================//

    private void draw(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        cam.update();
        batch.setProjectionMatrix(cam.combined);

        batch.begin();

//        if(active) gHolder.draw(batch, delta);
        if(active) drawCombat(delta);

        batch.end();

        if(active) cm.drawStage();

        LineDrawer.render(delta, cam.combined);
    }

    //==========================RENDER================================//

    private void update(float deltaTime) {

        if(Gdx.input.isKeyJustPressed((Input.Keys.SPACE)) && !running){
            active = !active;
        }

        if(active){
            running = true;
            combatProcessing(deltaTime);
        }
    }

    //=========================Extra Function==========================//

    private void drawCombat(float delta) {
        cm.draw(batch, Main.getAm().getTexture("BGTemper"), delta);
        TextDamageRenderer.textDrawer(batch);

        effectsAnimationDrawer();
    }

    private void effectsAnimationDrawer() {
        if(EffectRenderer.effectsAnim.isEmpty()) return;

        for(Effect e : EffectRenderer.effectsAnim){
            e.draw(batch);
        }
    }



    private void combatProcessing(float deltaTime) {
        cm.combat(deltaTime);

        TextDamageRenderer.textProcessor(deltaTime);
        TextDamageRenderer.garbageTextProcessor();

        EffectRenderer.effectsAnimationProcessor(deltaTime);
        EffectRenderer.garbageEffectProcessor();
    }

}
