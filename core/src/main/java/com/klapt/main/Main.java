package com.klapt.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.klapt.utilityTool.AssetManager;
import com.klapt.utilityTool.LineDrawer;
import lombok.Getter;

@Getter
public class Main extends Game {
    private SpriteBatch batch;
    @Getter
    private static AssetManager am;

    @Override
    public void create() {
        batch = new SpriteBatch();
        am = AssetManager.getInstance();
        setScreen(new GameScreen(this));
        LineDrawer.create();
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        am.dispose();
        LineDrawer.dispose();
    }

}
