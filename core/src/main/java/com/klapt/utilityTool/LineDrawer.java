package com.klapt.utilityTool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.klapt.main.GameScreen;

import java.util.ArrayList;
import java.util.List;

public class LineDrawer {
    private static ShapeRenderer shapeRenderer;
    public final static List<LineDrawer> container = new ArrayList<>();

    public static void create() { shapeRenderer = new ShapeRenderer(); }

    public static void dispose() { shapeRenderer.dispose(); }

    float startX, startY, endX, endY;
    float thickness;
    float alpha = 1;
    Color color;

    public LineDrawer(float startX, float startY, float endX, float endY, float thickness, Color color) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.thickness = thickness;
        this.color = color;
    }


    public static void render(float deltaTime, Matrix4 projectionMatrix){
        if(container.isEmpty()) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = container.size() - 1; i >= 0; i--) {
            LineDrawer l = container.get(i);
            float x = l.startX, y = l.startY;

            x = MathUtils.lerp(x, l.endX, 0.05f);
            y = MathUtils.lerp(y, l.endY, 0.05f);

            l.alpha -= deltaTime / 0.95f;
            if (l.alpha < 0) l.alpha = 0;

            l.color.a = l.alpha;
            shapeRenderer.setColor(l.color);
            shapeRenderer.rectLine(x, y, l.endX, l.endY, l.thickness);

            l.startX = x;
            l.startY = y;

            if (l.alpha <= 0) {
                container.remove(i);
            }

        }

        shapeRenderer.end();
    }
}
