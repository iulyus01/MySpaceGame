package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.myspacegame.components.TransformComponent;

import java.util.Comparator;


public class ZComparator implements Comparator<Entity> {
    private final ComponentMapper<TransformComponent> transformMapper;

    public ZComparator() {
        transformMapper = ComponentMapper.getFor(TransformComponent.class);

    }

    @Override
    public int compare(Entity entityA, Entity entityB) {
        float az = transformMapper.get(entityA).position.z;
        float bz = transformMapper.get(entityB).position.z;
        int res = 0;
        if(az > bz) {
            res = 1;
        } else if(az < bz) {
            res = -1;
        }
        return res;
    }
}