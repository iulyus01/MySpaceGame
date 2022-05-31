package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.myspacegame.Info;
import com.myspacegame.utils.ShapeDrawing;

public class ShapeDrawingComponent implements Component {

    public ShapeDrawing shape;
    public float delayMax = Info.speedrun ? 3 : 5;
    public float delay = 0;

}
