package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class BulletComponent implements Component, Pool.Poolable {
    public float maxLifeDelta = .8f;
    public float lifeDelta = 0;

    @Override
    public void reset() {
        lifeDelta = 0;
    }
}
