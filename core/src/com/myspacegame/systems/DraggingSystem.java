package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.myspacegame.Info;
import com.myspacegame.components.DraggingComponent;
import com.myspacegame.components.pieces.PieceComponent;

public class DraggingSystem extends IteratingSystem {

    private final ComponentMapper<DraggingComponent> draggingMapper;
    private final ComponentMapper<PieceComponent> pieceMapper;

    public DraggingSystem() {
        super(Family.all(DraggingComponent.class, PieceComponent.class).get());

        draggingMapper = ComponentMapper.getFor(DraggingComponent.class);
        pieceMapper = ComponentMapper.getFor(PieceComponent.class);

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PieceComponent pieceComponent = pieceMapper.get(entity);
        DraggingComponent draggingComponent = draggingMapper.get(entity);

        Fixture fixture = pieceComponent.fixture;
        Body body = fixture.getBody();

        if(draggingComponent.first) {
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);


            draggingComponent.draggingPointOfPieceFromCenter.x = Info.mouseWorldX - pieceComponent.fixtureCenter.x;
            draggingComponent.draggingPointOfPieceFromCenter.y = Info.mouseWorldY - pieceComponent.fixtureCenter.y;

            float draggingPointAngleFromBottomLeftRad = MathUtils.atan2(
                    draggingComponent.draggingPointOfPieceFromCenter.y,
                    draggingComponent.draggingPointOfPieceFromCenter.x
            ) - body.getAngle();
            float draggingPointDistFromBottomLeft = (float) Math.sqrt(draggingComponent.draggingPointOfPieceFromCenter.x * draggingComponent.draggingPointOfPieceFromCenter.x + draggingComponent.draggingPointOfPieceFromCenter.y * draggingComponent.draggingPointOfPieceFromCenter.y);
            float angleToComputePiecePosOffset = draggingComponent.playerShipAngleRad + draggingPointAngleFromBottomLeftRad + Info.rad90Deg * 2;
            body.setTransform(body.getPosition(), draggingComponent.playerShipAngleRad);
            body.setTransform(
                    pieceComponent.fixtureCenter.x + draggingComponent.draggingPointOfPieceFromCenter.x + MathUtils.cos(angleToComputePiecePosOffset) * draggingPointDistFromBottomLeft,
                    pieceComponent.fixtureCenter.y + draggingComponent.draggingPointOfPieceFromCenter.y + MathUtils.sin(angleToComputePiecePosOffset) * draggingPointDistFromBottomLeft,
                    body.getAngle()
            );

            draggingComponent.draggingPointOfPieceFromCenter.x = Info.mouseWorldX - pieceComponent.fixtureCenter.x;
            draggingComponent.draggingPointOfPieceFromCenter.y = Info.mouseWorldY - pieceComponent.fixtureCenter.y;

            draggingComponent.first = false;
        } else {
            float newBodyX = Info.mouseWorldX - draggingComponent.draggingPointOfPieceFromCenter.x;
            float newBodyY = Info.mouseWorldY - draggingComponent.draggingPointOfPieceFromCenter.y;
            body.setTransform(newBodyX, newBodyY, body.getAngle());

        }

    }

}
