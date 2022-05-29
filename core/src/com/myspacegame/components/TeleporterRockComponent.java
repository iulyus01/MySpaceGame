package com.myspacegame.components;

import com.badlogic.ashley.core.Component;

public class TeleporterRockComponent implements Component {
    public TeleporterComponent teleporterComponent;
    public TransformComponent teleporterTransform;
    public float teleporterX;
    public float teleporterY;
    public float angleRad;
    public float baseRadius;
    public float radius;
    public float offsetRadius;
    public float delay = 0;
    public float delayMax = 2;
}
