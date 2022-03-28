package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class BulletComponent implements Component, Pool.Poolable {

    public int createdByActorId = -1;
    public float maxLifeDelta = .8f;
    public float lifeDelta = 0;
    public float damage = 1;
    public boolean isDead = false;

    @Override
    public void reset() {
        lifeDelta = 0;
        isDead = false;
    }
}
