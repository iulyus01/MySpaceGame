package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.myspacegame.Info;
import com.myspacegame.components.TransformComponent;
import com.myspacegame.components.pieces.PieceComponent;

public class PieceSystem extends IteratingSystem {

    private final Engine engine;

    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<TransformComponent> transformMapper;

    public PieceSystem(Engine engine) {
        super(Family.all(PieceComponent.class, TransformComponent.class).get());
        this.engine = engine;

        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PieceComponent pieceComponent = pieceMapper.get(entity);
        TransformComponent transformComponent = transformMapper.get(entity);

        transformComponent.angleRad = pieceComponent.fixture.getBody().getAngle();
        transformComponent.position.x = pieceComponent.fixtureCenter.x;
        transformComponent.position.y = pieceComponent.fixtureCenter.y;

        if(pieceComponent.isDead) {
            pieceComponent.fixture.getBody().destroyFixture(pieceComponent.fixture);
//            world.destroyBody(bodyComponent.body);
            engine.removeEntity(entity);
            // TODO remove fixture
        }
    }
}
