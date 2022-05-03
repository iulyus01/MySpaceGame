package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class DraggingComponent implements Component {

    public float playerShipAngleRad;
    public final Vector2 draggingPointOfPieceFromCenter = new Vector2();
    public boolean isBeforeFirstUpdate = false;
    public boolean toRotateRight = false;

}