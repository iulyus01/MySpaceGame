package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class BulletComponent implements Component, Pool.Poolable {

    public int createdByActorId = -1;
    public float lifeDeltaMax = .8f;
    public float lifeDeadDeltaMax = lifeDeltaMax + .2f;
    public float lifeDelta = 0;
    public float damage = 3;
    public boolean isDead = false;
    public boolean isReadyToDie = false;

    @Override
    public void reset() {
        createdByActorId = -1;
        lifeDelta = 0;
        isDead = false;
        isReadyToDie = false;
    }
}
