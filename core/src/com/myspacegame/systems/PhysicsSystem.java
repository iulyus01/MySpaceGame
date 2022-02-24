package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.*;
import com.myspacegame.Info;
import com.myspacegame.components.BodyComponent;
import com.myspacegame.components.PlayerComponent;
import com.myspacegame.components.pieces.PieceComponent;

public class PhysicsSystem extends IteratingSystem {

    private static final float MAX_STEP_TIME = 1 / 45f;
    private static float accumulator = 0f;

    private final ComponentMapper<PieceComponent> pieceMapper;

    private final World world;

    public PhysicsSystem(World world) {
        super(Family.one(PieceComponent.class, BodyComponent.class).get());
        this.world = world;

        pieceMapper = ComponentMapper.getFor(PieceComponent.class);

    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;


        if(accumulator >= MAX_STEP_TIME) {
            world.step(MAX_STEP_TIME, 6, 2);
            accumulator -= MAX_STEP_TIME;
        }


    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PieceComponent pieceComponent = pieceMapper.get(entity);
        if(pieceComponent != null) {
            Info.computePieceFixtureBottomLeft(pieceComponent.fixtureBottomLeft, pieceComponent.fixture);
            Info.computePieceFixtureCenter(pieceComponent);
        }

    }
}
