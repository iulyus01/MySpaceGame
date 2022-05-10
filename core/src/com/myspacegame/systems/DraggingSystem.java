package com.myspacegame.systems;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.myspacegame.Info;
import com.myspacegame.components.DraggingComponent;
import com.myspacegame.components.TransformComponent;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.factories.BodyFactory;

import java.util.Arrays;

public class DraggingSystem extends IteratingSystem {

    private final ComponentMapper<DraggingComponent> draggingMapper;
    private final ComponentMapper<PieceComponent> pieceMapper;

    private final BodyFactory bodyFactory;

    public DraggingSystem(World world) {
        super(Family.all(DraggingComponent.class, PieceComponent.class).get());
        this.bodyFactory = BodyFactory.getInstance(world);

        draggingMapper = ComponentMapper.getFor(DraggingComponent.class);
        pieceMapper = ComponentMapper.getFor(PieceComponent.class);

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PieceComponent pieceComponent = pieceMapper.get(entity);
        DraggingComponent draggingComponent = draggingMapper.get(entity);

        Fixture fixture = pieceComponent.fixture;
        Body body = fixture.getBody();

        if(draggingComponent.isBeforeFirstUpdate) {
            doFirstUpdate(pieceComponent, draggingComponent, body);
        } else {
            doDragUpdate(draggingComponent, body);
        }

        if(draggingComponent.toRotateRight) {
            draggingComponent.toRotateRight = false;
            rotatePieceRight(entity, pieceComponent);
        }

    }

    private void doFirstUpdate(PieceComponent pieceComponent, DraggingComponent draggingComponent, Body body) {
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



        draggingComponent.isBeforeFirstUpdate = false;
    }

    private void doDragUpdate(DraggingComponent draggingComponent, Body body) {
        float newBodyX = Info.mouseWorldX - draggingComponent.draggingPointOfPieceFromCenter.x;
        float newBodyY = Info.mouseWorldY - draggingComponent.draggingPointOfPieceFromCenter.y;
        body.setTransform(newBodyX, newBodyY, body.getAngle());
    }

    private void rotatePieceRight(Entity entity, PieceComponent pieceComponent) {
        TransformComponent transformComponent = entity.getComponent(TransformComponent.class);
        Body body = pieceComponent.fixture.getBody();

        pieceComponent.piece.rotation = (pieceComponent.piece.rotation + 1) % 4;
        pieceComponent.piece.shape.setRotation(pieceComponent.piece.rotation * 90);
        transformComponent.angleOrientationRad = pieceComponent.piece.rotation * Info.rad90Deg;

        pieceComponent.fixture.getBody().destroyFixture(pieceComponent.fixture);
        bodyFactory.createPieceFixture(body, pieceComponent.piece, entity);

//        Info.rotateEdges(pieceComponent.piece.edges, Info.rad90Deg);
    }

}
