package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.myspacegame.components.*;

public class RockSystem extends IteratingSystem {

    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<BodyComponent> bodyMapper;

    public RockSystem() {
        super(Family.all(RockComponent.class, TransformComponent.class).get());

        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        bodyMapper = ComponentMapper.getFor(BodyComponent.class);

    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transformComponent = transformMapper.get(entity);
        BodyComponent bodyComponent = bodyMapper.get(entity);

        transformComponent.position.x = bodyComponent.body.getPosition().x;
        transformComponent.position.y = bodyComponent.body.getPosition().y;
        transformComponent.angleRad = bodyComponent.body.getAngle();
    }

}
