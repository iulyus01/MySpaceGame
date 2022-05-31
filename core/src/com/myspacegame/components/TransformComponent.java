package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

public class TransformComponent implements Component, Pool.Poolable {
    public final Vector3 position = new Vector3();
    public final Vector2 scale = new Vector2(1.0f, 1.0f);
    // it will stretch to cover this width and height
    public float width = 10;
    public float height = 10;
    public float angleRad = 0;
    public float angleOrientationRad = 0;
    public boolean isHidden = false;

    @Override
    public void reset() {
        position.x = -1000;
        position.y = -1000;
        angleRad = 0;
        angleOrientationRad = 0;
        isHidden = false;
    }
}
