package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class CropComponent implements Component {
    public TextureRegion region;
    public int srcX;
    public int srcY;
    public int srcWidth;
    public int srcHeight;
}