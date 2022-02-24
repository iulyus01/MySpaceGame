package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.myspacegame.MainClass;
import com.myspacegame.components.*;
import com.myspacegame.factories.WorldFactory;

public class BulletSystem extends IteratingSystem {

    private final ComponentMapper<BulletComponent> bulletMapper;
    private final ComponentMapper<TransformComponent> transformMapper;
    private final World world;
    private final PooledEngine engine;

    public BulletSystem(MainClass game, PooledEngine engine) {
        super(Family.all(BulletComponent.class, BodyComponent.class, TransformComponent.class).get());
        this.engine = engine;
        bulletMapper = ComponentMapper.getFor(BulletComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);

        this.world = WorldFactory.getInstance(game, engine).getWorld();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BulletComponent bulletComponent = bulletMapper.get(entity);
        TransformComponent transformComponent = transformMapper.get(entity);
        Body body = entity.getComponent(BodyComponent.class).body;
        if(body == null) return;

        bulletComponent.lifeDelta += deltaTime;

        transformComponent.position.x = body.getWorldCenter().x;
        transformComponent.position.y = body.getWorldCenter().y;

        // bullet life
        if(bulletComponent.lifeDelta >= bulletComponent.maxLifeDelta) {
            body.destroyFixture(body.getFixtureList().get(0));
            world.destroyBody(body);
            engine.removeEntity(entity);
        }
    }

}