package com.klapt.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.klapt.utilityTool.AssetManager;
import com.klapt.utilityTool.LineDrawer;

public class Main extends Game {
    private SpriteBatch batch;
    private static AssetManager am;

    @Override
    public void create() {
        batch = new SpriteBatch();
        am = new AssetManager();
        setScreen(new GameScreen(this));
        LineDrawer.create();
    }
    @Override
    public void render() { super.render(); }
    @Override
    public void dispose() {
        batch.dispose();
        am.dispose();
        LineDrawer.dispose();
    }

    public SpriteBatch getBatch() {return batch;}
    public static AssetManager getAm() {return am;}
}
