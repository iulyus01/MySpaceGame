package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class TextureRotatingComponent implements Component {
    public TextureRegion textureRegion = null;
    public Vector2 origin = new Vector2();
    public float angleRad = 0.0f;
}