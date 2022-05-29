package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.MathUtils;
import com.myspacegame.Info;
import com.myspacegame.entities.Area;

public class TeleporterComponent implements Component {

    public Area currentArea;
    public Area destArea;
    public float rotationSpeed = MathUtils.degRad / 10;
    public float rotationSpeedMax = MathUtils.degRad * 2;
    public float rotationSpeedMin = MathUtils.degRad / 10;
    public float activationDistanceMax = Info.blockSize * 100;
    public float activationDistanceMin = Info.blockSize * 10;
    public boolean teleported = false;
    public boolean isAnimationRunning = false;

}
