package com.myspacegame.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

public class PieceTractoringSystem extends IteratingSystem {

    public PieceTractoringSystem() {
        super(Family.all().get());
    }

    @Override
    protected void processEntity(Entity entity, float v) {

    }
}
