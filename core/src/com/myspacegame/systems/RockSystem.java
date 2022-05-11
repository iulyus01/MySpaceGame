package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.*;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.entities.Anchor;
import com.myspacegame.entities.CorePiece;
import com.myspacegame.entities.Piece;
import com.myspacegame.entities.TractorBeamPiece;
import com.myspacegame.factories.BodyFactory;
import com.myspacegame.factories.ShapeRenderingDebug;
import com.myspacegame.factories.WorldFactory;

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
