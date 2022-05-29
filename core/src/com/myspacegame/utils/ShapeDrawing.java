package com.myspacegame.utils;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.myspacegame.components.ShapeDrawingComponent;
import com.myspacegame.components.TransformComponent;

public interface ShapeDrawing {
    void draw(ShapeDrawingComponent shapeDrawingComponent, TransformComponent transformComponent, ShapeRenderer shapeRenderer);
    void update(ShapeDrawingComponent shapeDrawingComponent, TransformComponent transformComponent, float delta);
    void end();
}
