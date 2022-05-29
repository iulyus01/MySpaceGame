package com.myspacegame.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.myspacegame.Info;
import com.myspacegame.components.*;
import com.myspacegame.components.pieces.CorePieceComponent;
import com.myspacegame.entities.Area;
import com.myspacegame.entities.ShipData;
import com.myspacegame.factories.WorldFactory;
import com.myspacegame.utils.ShapeDrawing;

public class TeleporterSystem extends IteratingSystem {

    private final PooledEngine engine;
    private final WorldFactory worldFactory;

    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<BodyComponent> bodyMapper;
    private final ComponentMapper<TeleporterComponent> teleporterMapper;
    private final ComponentMapper<TeleporterRockComponent> teleporterRockMapper;

    private final ShipData playerShip;

    private Entity closestTeleporterEntity;
    private Entity teleportAnimationEntity;

    private float playerTpDist2;
    private boolean isAnimationRunning = false;

    public TeleporterSystem(PooledEngine engine, WorldFactory worldFactory) {
        super(Family.all(TransformComponent.class).one(TeleporterComponent.class, TeleporterRockComponent.class).get());
        this.engine = engine;
        this.worldFactory = worldFactory;

        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        bodyMapper = ComponentMapper.getFor(BodyComponent.class);
        teleporterMapper = ComponentMapper.getFor(TeleporterComponent.class);
        teleporterRockMapper = ComponentMapper.getFor(TeleporterRockComponent.class);

        Entity playerEntity = engine.getEntitiesFor(Family.all(PlayerComponent.class, CorePieceComponent.class).get()).first();
        this.playerShip = playerEntity.getComponent(ShipComponent.class).shipData;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if(closestTeleporterEntity == null) {
            playerTpDist2 = Float.MAX_VALUE;
            return;
        }
        TeleporterComponent teleporterComponent = teleporterMapper.get(closestTeleporterEntity);
        playerTpDist2 -= teleporterComponent.activationDistanceMin;

        float distRate = playerTpDist2 / ((teleporterComponent.activationDistanceMax - teleporterComponent.activationDistanceMin) * (teleporterComponent.activationDistanceMax - teleporterComponent.activationDistanceMin));

        if(distRate > 1) {
            teleporterComponent.rotationSpeed = teleporterComponent.rotationSpeedMin;
        } else if(distRate < 0) {
            teleporterComponent.rotationSpeed = teleporterComponent.rotationSpeedMax;
        } else {
            teleporterComponent.rotationSpeed = (1 - distRate) * (teleporterComponent.rotationSpeedMax - teleporterComponent.rotationSpeedMin) + teleporterComponent.rotationSpeedMin;
        }

        if(distRate < 0) {
            if(!isAnimationRunning) startTeleportAnimation(teleporterComponent);
        } else {
            if(isAnimationRunning) endTeleportAnimation();
        }

        playerTpDist2 = Float.MAX_VALUE;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transformComponent = transformMapper.get(entity);

        updatePositions(entity, transformComponent, deltaTime);

        if(teleporterMapper.has(entity)) {
            checkClosestTeleporter(entity, transformComponent);
        }
    }

    private void updatePositions(Entity entity, TransformComponent transformComponent, float delta) {
        if(teleporterMapper.has(entity)) {
            TeleporterComponent teleporterComponent = teleporterMapper.get(entity);

            BodyComponent bodyComponent = bodyMapper.get(entity);
            bodyComponent.body.setTransform(bodyComponent.body.getPosition(), bodyComponent.body.getAngle() + teleporterComponent.rotationSpeed);

            transformComponent.position.x = bodyComponent.body.getPosition().x;
            transformComponent.position.y = bodyComponent.body.getPosition().y;
            transformComponent.angleRad = bodyComponent.body.getAngle();
        } else {
            TeleporterRockComponent teleporterRockComponent = teleporterRockMapper.get(entity);

            teleporterRockComponent.delay += delta;
            if(teleporterRockComponent.delay >= teleporterRockComponent.delayMax) {
                teleporterRockComponent.delay = 0;
            }
            float percent = teleporterRockComponent.delay / teleporterRockComponent.delayMax * 2;
            if(percent > 1) percent = 2 - percent;
            teleporterRockComponent.radius = percent * teleporterRockComponent.offsetRadius;

            float radians = teleporterRockComponent.angleRad + teleporterRockComponent.teleporterTransform.angleRad;
            float radius = teleporterRockComponent.baseRadius + teleporterRockComponent.radius;
            transformComponent.position.x = teleporterRockComponent.teleporterX + MathUtils.cos(radians) * radius;
            transformComponent.position.y = teleporterRockComponent.teleporterY + MathUtils.sin(radians) * radius;
            transformComponent.angleRad = teleporterRockComponent.teleporterTransform.angleRad;
        }
    }

    private void checkClosestTeleporter(Entity entity, TransformComponent teleportTransform) {
        float temp = playerShip.core.pieceComponent.fixtureCenter.dst2(teleportTransform.position.x, teleportTransform.position.y);
        if(temp < playerTpDist2) {
            closestTeleporterEntity = entity;
            playerTpDist2 = temp;
        }
    }

    private void startTeleportAnimation(TeleporterComponent teleporterComponent) {
        Entity animationEntity = engine.createEntity();

        ShapeDrawingComponent shapeDrawingComponent = engine.createComponent(ShapeDrawingComponent.class);
        shapeDrawingComponent.delay = 0;
        shapeDrawingComponent.shape = new ShapeDrawing() {
            @Override
            public void draw(ShapeDrawingComponent shapeDrawingComponent, TransformComponent transformComponent, ShapeRenderer shapeRenderer) {
                float ratio = shapeDrawingComponent.delay / shapeDrawingComponent.delayMax;
                if(ratio > 1) ratio = 1;

                shapeRenderer.setColor(Info.colorPurple);
                int segments = 40;
                float radius = Info.blockSize * 5;
                float arcSize = 360f / segments * MathUtils.degRad;
                float x1 = MathUtils.cos(0) * radius;
                float y1 = MathUtils.sin(0) * radius;
                float x2 = x1;
                float y2 = y1;
                for(int i = 0; i <= ratio * segments; i++) {
                    shapeRenderer.rectLine(
                            transformComponent.position.x + x1,
                            transformComponent.position.y + y1,
                            transformComponent.position.x + x2,
                            transformComponent.position.y + y2,
                            Info.blockSize / 2f
                    );
                    x2 = x1;
                    y2 = y1;
                    x1 = MathUtils.cos(arcSize * i) * radius;
                    y1 = MathUtils.sin(arcSize * i) * radius;
                }
                shapeRenderer.rectLine(
                        transformComponent.position.x + x1,
                        transformComponent.position.y + y1,
                        transformComponent.position.x + x2,
                        transformComponent.position.y + y2,
                        Info.blockSize / 2f
                );
            }

            @Override
            public void update(ShapeDrawingComponent shapeDrawingComponent, TransformComponent transformComponent, float delta) {
                transformComponent.position.x = playerShip.core.pieceComponent.fixtureCenter.x;
                transformComponent.position.y = playerShip.core.pieceComponent.fixtureCenter.y;

                shapeDrawingComponent.delay += delta;
                if(shapeDrawingComponent.delay >= shapeDrawingComponent.delayMax && !teleporterComponent.teleported) {
                    end();
                    teleporterComponent.teleported = true;
                }
            }

            @Override
            public void end() {
                switchArena(teleporterComponent.destArea, teleporterComponent);
            }
        };

        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
        transformComponent.isHidden = false;
        transformComponent.position.x = 0;
        transformComponent.position.y = 0;
        transformComponent.position.z = 0;

        animationEntity.add(shapeDrawingComponent);
        animationEntity.add(transformComponent);

        engine.addEntity(animationEntity);

        isAnimationRunning = true;
        teleportAnimationEntity = animationEntity;
    }

    private void endTeleportAnimation() {
        engine.removeEntity(teleportAnimationEntity);
        isAnimationRunning = false;
    }

    private void switchArena(Area destArea, TeleporterComponent teleporterComponent) {
        worldFactory.destroyLevel();
        worldFactory.createLevel(destArea, false);
        int i = 0;
        for(; i < teleporterComponent.destArea.to.size; i++) {
            var tp = teleporterComponent.destArea.to.get(i);
            if(tp.first == teleporterComponent.currentArea) break;
        }
        worldFactory.setPlayerToTp(teleporterComponent.destArea.to.get(i).second);
        endTeleportAnimation();
    }

}
