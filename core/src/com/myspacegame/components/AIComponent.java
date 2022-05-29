package com.myspacegame.components;

import com.badlogic.ashley.core.Component;

public class AIComponent implements Component {
    public float movementTargetX;
    public float movementTargetY;
    public float movementTargetDelay = 0;
    public float movementTargetDelayMax = 3f;
    public float movingDelay = 0;
    public float movingDelayMax = .5f;
    public float movementScale = 1;
    public boolean forward = false;

    public float shotTargetX;
    public float shotTargetY;
    public float shootingDelay = 0;
    public float shootingDelayMax = .0f;
    public float notShootingDelay = 0;
    public float notShootingDelayMax = 10.5f;
    public boolean shooting = false;
}