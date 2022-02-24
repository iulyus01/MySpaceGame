package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class TransformComponent implements Component {
    public final Vector3 position = new Vector3();
    public final Vector2 scale = new Vector2(1.0f, 1.0f);
    public float width = 10;
    public float height = 10;
    public float angleRad = 45;
    public float angleOrientationRad = 0;
    public boolean isHidden = false;
}
