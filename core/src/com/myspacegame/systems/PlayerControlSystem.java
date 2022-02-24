package com.myspacegame.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.myspacegame.Info;
import com.myspacegame.KeyboardController;
import com.myspacegame.MainClass;
import com.myspacegame.components.*;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.components.pieces.ThrusterPieceComponent;
import com.myspacegame.components.pieces.WeaponPieceComponent;
import com.myspacegame.entities.ThrusterPiece;
import com.myspacegame.entities.WeaponPiece;
import com.myspacegame.factories.EntitiesFactory;
import com.myspacegame.factories.WorldFactory;

public class PlayerControlSystem extends IteratingSystem {

    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<ThrusterPieceComponent> pieceThrusterMapper;
    private final ComponentMapper<WeaponPieceComponent> pieceWeaponMapper;
    private final ComponentMapper<TextureRotatingComponent> rotatingMapper;
    private final KeyboardController controller;
    private final MainClass game;
    private final PooledEngine engine;
    private final OrthographicCamera camera;
    private final EntitiesFactory entitiesFactory;

    private final Body playerBody;

    public PlayerControlSystem(KeyboardController keyboardController, MainClass game, PooledEngine engine, OrthographicCamera camera) {
        super(Family.all(PlayerComponent.class).one(ThrusterPieceComponent.class, WeaponPieceComponent.class).get());
        this.game = game;
        this.engine = engine;
        this.camera = camera;
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        pieceThrusterMapper = ComponentMapper.getFor(ThrusterPieceComponent.class);
        pieceWeaponMapper = ComponentMapper.getFor(WeaponPieceComponent.class);
        rotatingMapper = ComponentMapper.getFor(TextureRotatingComponent.class);
        controller = keyboardController;

        World world = WorldFactory.getInstance(game, engine).getWorld();
        entitiesFactory = EntitiesFactory.getInstance(game, engine, world);
        Entity entity = engine.getEntitiesFor(Family.all(PlayerComponent.class, PieceComponent.class).get()).first();
        playerBody = pieceMapper.get(entity).fixture.getBody();
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        handleModeChange();

        switch(Info.activeMode) {
            case MOVING:
                camera.position.set(playerBody.getWorldCenter().x, playerBody.getWorldCenter().y, 0);
                camera.update();
                break;
            case BUILDING:
                playerBody.setLinearVelocity(0, 0);
                playerBody.setAngularVelocity(0);


                float angle = playerBody.getAngle() % (Info.rad90Deg * 4);
                if(Math.abs(angle) > Info.rad90Deg * 2) angle = -(angle - Info.rad90Deg * 2);

                if(Math.abs(playerBody.getAngle()) > 1 * MathUtils.degRad)
                    playerBody.setTransform(playerBody.getPosition(), angle * .9f);
                else playerBody.setTransform(playerBody.getPosition(), 0);
                handleBuildingCamera(deltaTime);
                handleBuildingKeys();
                break;
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if(Info.activeMode == Info.PlayerMode.BUILDING) return;

        PieceComponent pieceComponent = pieceMapper.get(entity);
        TransformComponent transformComponent = transformMapper.get(entity);

        Body body = pieceComponent.fixture.getBody();

        if(entity.getComponent(ThrusterPieceComponent.class) != null) {
            handleThrusters(entity, transformComponent, pieceComponent, body);
        } else if(entity.getComponent(WeaponPieceComponent.class) != null) {
            handleWeapons(entity, pieceComponent, deltaTime);
        }


//        bodyComponent.body.setLinearVelocity(MathUtils.lerp(bodyComponent.body.getLinearVelocity().x, 0, 0.1f), bodyComponent.body.getLinearVelocity().y);
    }

    private void handleThrusters(Entity entity, TransformComponent transformComponent, PieceComponent pieceComponent, Body body) {
        ThrusterPieceComponent thrusterComponent = pieceThrusterMapper.get(entity);
        ThrusterPiece piece = thrusterComponent.piece;

        float linearImpulse = Info.defaultThrusterForce;

        boolean wPressed = (controller.wDown && piece.activateKey == Info.Key.W);
        boolean sPressed = (controller.sDown && piece.activateKey == Info.Key.S);
        boolean aPressed = (controller.aDown && piece.activateKey == Info.Key.A);
        boolean dPressed = (controller.dDown && piece.activateKey == Info.Key.D);
        if(wPressed || sPressed || aPressed || dPressed) {
            float angleX = (float) Math.cos(transformComponent.angleRad + thrusterComponent.piece.angleRad * Info.rad90Deg);
            float angleY = (float) Math.sin(transformComponent.angleRad + thrusterComponent.piece.angleRad * Info.rad90Deg);
            Vector2 fixtureCenter = pieceComponent.fixtureCenter;

            body.applyForce(
//            body.applyLinearImpulse(
                    angleX * linearImpulse,
                    angleY * linearImpulse,
                    fixtureCenter.x,
                    fixtureCenter.y,
                    true
            );
            System.out.println();
        }
    }

    private void handleWeapons(Entity entity, PieceComponent pieceComponent, float deltaTime) {
        WeaponPieceComponent weaponComponent = pieceWeaponMapper.get(entity);
        TextureRotatingComponent rotatingComponent = rotatingMapper.get(entity);

        WeaponPiece piece = weaponComponent.piece;

        // weapon angle, oriented to mouse
        if(!piece.fixedAngle) {
            Vector2 fixtureCenter = pieceComponent.fixtureCenter;
            piece.angleRad = (float) Math.atan2(Info.mouseWorldY - fixtureCenter.y, Info.mouseWorldX - fixtureCenter.x);
            rotatingComponent.angleRad = piece.angleRad;
        }

        // reloading
        if(weaponComponent.isReloading) {
            weaponComponent.reloadingTime += deltaTime;
            if(weaponComponent.reloadingTime >= weaponComponent.reloadingTimeMax) {
                weaponComponent.isReloading = false;
                weaponComponent.reloadingTime = 0;
            }
        }

        boolean mouseLeftPressed = controller.mouseLeft;
        if(mouseLeftPressed) {
            if(!weaponComponent.isReloading) {
                Vector2 pos = pieceComponent.fixtureCenter;

                Entity bulletEntity = entitiesFactory.createBullet(pos.x, pos.y, rotatingComponent.angleRad);
                engine.addEntity(bulletEntity);
                weaponComponent.isReloading = true;

            }
        }
    }

    private void handleModeChange() {
        if(controller.bPressed) {
            controller.bPressed = false;
            if(Info.activeMode == Info.PlayerMode.MOVING) {
                Info.activeMode = Info.PlayerMode.BUILDING;
                engine.addSystem(new BuildingSystem(controller, game, engine));
//                engine.addSystem(new BuildingRenderingSystem(game, engine, camera));
            } else if(Info.activeMode == Info.PlayerMode.BUILDING) {
                Info.activeMode = Info.PlayerMode.MOVING;
                engine.removeSystem(engine.getSystem(BuildingSystem.class));
            }
        }
    }

    private void handleBuildingCamera(float delta) {
        float distPerFrame = Info.blockSize * delta * 20;
        float distX = 0;
        float distY = 0;

        if(controller.dDown) distX += distPerFrame;
        if(controller.wDown) distY += distPerFrame;
        if(controller.aDown) distX -= distPerFrame;
        if(controller.sDown) distY -= distPerFrame;

        camera.position.set(camera.position.x + distX, camera.position.y + distY, 0);
        camera.update();
    }

    private void handleBuildingKeys() {
        if(controller.mouseRight) {

        }
    }


}