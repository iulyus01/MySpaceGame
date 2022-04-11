package com.myspacegame.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
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
import com.myspacegame.factories.ShapeRenderingDebug;
import com.myspacegame.factories.SystemManager;
import com.myspacegame.factories.WorldFactory;

public class PlayerControlSystem extends IteratingSystem {

    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<ThrusterPieceComponent> pieceThrusterMapper;
    private final ComponentMapper<WeaponPieceComponent> pieceWeaponMapper;
    private final KeyboardController controller;
    private final MainClass game;
    private final PooledEngine engine;
    private final OrthographicCamera camera;
    private final EntitiesFactory entitiesFactory;

    private final Body playerBody;

    private final Array<Info.Quintuple<Float, Float, Float, Float, Integer>> thrusterForces;

    public PlayerControlSystem(KeyboardController keyboardController, MainClass game, PooledEngine engine, OrthographicCamera camera) {
        super(Family.all(PlayerComponent.class).one(ThrusterPieceComponent.class, WeaponPieceComponent.class).get());
        this.game = game;
        this.engine = engine;
        this.camera = camera;
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        pieceThrusterMapper = ComponentMapper.getFor(ThrusterPieceComponent.class);
        pieceWeaponMapper = ComponentMapper.getFor(WeaponPieceComponent.class);
        controller = keyboardController;

        World world = WorldFactory.getInstance(game, engine).getWorld();
        entitiesFactory = EntitiesFactory.getInstance(game, engine, world);
        Entity entity = engine.getEntitiesFor(Family.all(PlayerComponent.class, PieceComponent.class).get()).first();
        playerBody = pieceMapper.get(entity).fixture.getBody();

        thrusterForces = new Array<>(true, 4, Info.Quintuple.class);
        thrusterForces.add(new Info.Quintuple<>(0f, 0f, 0f, 0f, 0));
        thrusterForces.add(new Info.Quintuple<>(0f, 0f, 0f, 0f, 0));
        thrusterForces.add(new Info.Quintuple<>(0f, 0f, 0f, 0f, 0));
        thrusterForces.add(new Info.Quintuple<>(0f, 0f, 0f, 0f, 0));
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        handleModeChange();

        switch(Info.activeMode) {
            case MOVING:
                applyForces();

                float velX = playerBody.getLinearVelocity().x;
                float velY = playerBody.getLinearVelocity().y;
                velX = Math.signum(velX) * Math.min(Math.abs(velX), Info.maxHorVerVelocity);
                velY = Math.signum(velY) * Math.min(Math.abs(velY), Info.maxHorVerVelocity);
                playerBody.setLinearVelocity(velX, velY);

//                playerBody.setLinearVelocity(playerBody.getLinearVelocity().x, playerBody.getLinearVelocity().y);
//                Info.tempVector2.x = playerBody.getLinearVelocity().x;
//                Info.tempVector2.y = playerBody.getLinearVelocity().y;
//                Info.tempVector2.nor();
//                playerBody.setLinearVelocity(Info.tempVector2.x * Info.maxHorVerVelocity, Info.tempVector2.y * Info.maxHorVerVelocity);

                camera.position.set(playerBody.getWorldCenter().x, playerBody.getWorldCenter().y, 0);
                camera.update();

                break;
            case BUILDING:
                playerBody.setLinearVelocity(0, 0);
                playerBody.setAngularVelocity(0);

                float angle = playerBody.getAngle() % (Info.rad90Deg * 4);
                if(Math.abs(angle) > Info.rad90Deg * 2) angle = -(angle - Info.rad90Deg * 2);

                if(Math.abs(playerBody.getAngle()) > 1 * MathUtils.degRad) playerBody.setTransform(playerBody.getPosition(), angle * .9f);
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

        float linearImpulse = Info.defaultThrusterImpulse;

        boolean wPressed = (controller.wDown && piece.activateKey == Info.Key.W);
        boolean sPressed = (controller.sDown && piece.activateKey == Info.Key.S);
        boolean aPressed = (controller.aDown && piece.activateKey == Info.Key.A);
        boolean dPressed = (controller.dDown && piece.activateKey == Info.Key.D);
        if(wPressed || sPressed || aPressed || dPressed) {
            float angleX = (float) Math.cos(transformComponent.angleRad + thrusterComponent.piece.angleDirection * Info.rad90Deg);
            float angleY = (float) Math.sin(transformComponent.angleRad + thrusterComponent.piece.angleDirection * Info.rad90Deg);
            Vector2 fixtureCenter = pieceComponent.fixtureCenter;

            var q = thrusterForces.get(piece.angleDirection);
            int nr = q.fifth;
            float impulseX = q.first + angleX * linearImpulse;
            float impulseY = q.second + angleY * linearImpulse;
            float pointX = (q.third * nr + fixtureCenter.x) / (nr + 1);
            float pointY = (q.forth * nr + fixtureCenter.y) / (nr + 1);



//            if(piece.angleDirection == 0) {
//                pointX = -.84f;
//                pointY = 0;
//            }

            q.set(impulseX, impulseY, pointX, pointY, nr + 1);
        }
    }

    private void handleWeapons(Entity entity, PieceComponent pieceComponent, float deltaTime) {
        WeaponPieceComponent weaponComponent = pieceWeaponMapper.get(entity);

        WeaponPiece piece = weaponComponent.piece;

        // weapon angle, oriented to mouse
        if(!piece.fixedAngle) {
            Vector2 fixtureCenter = pieceComponent.fixtureCenter;
            piece.angleRad = (float) Math.atan2(Info.mouseWorldY - fixtureCenter.y, Info.mouseWorldX - fixtureCenter.x);
        }

        // reloading
        if(weaponComponent.isReloading) {
            weaponComponent.reloadingTime += deltaTime;
            if(weaponComponent.reloadingTime >= weaponComponent.reloadingTimeMax) {
                weaponComponent.isReloading = false;
                weaponComponent.reloadingTime = 0;
            }
        }

        // fire
        boolean mouseLeftPressed = controller.mouseLeft;
        if(mouseLeftPressed) {
            if(!weaponComponent.isReloading) {
                Vector2 pos = pieceComponent.fixtureCenter;

                Entity bulletEntity = entitiesFactory.createBullet(piece.actorId, pos.x, pos.y, piece.angleRad);
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
//                engine.addSystem(new BuildingSystem(controller, game, engine));
//                engine.addSystem(new RenderingBuildingSystem(game, engine, camera));
                SystemManager.getInstance().toggleBuilding(true);
            } else if(Info.activeMode == Info.PlayerMode.BUILDING) {
                Info.activeMode = Info.PlayerMode.MOVING;
//                engine.removeSystem(engine.getSystem(BuildingSystem.class));
                SystemManager.getInstance().toggleBuilding(false);
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

    private void applyForces() {
        for(int i = 0; i < thrusterForces.size; i++) {
            if(thrusterForces.get(i).fifth == 0) continue;
//            playerBody.applyForce(
            playerBody.applyLinearImpulse(
                    thrusterForces.get(i).first,
                    thrusterForces.get(i).second,
                    thrusterForces.get(i).third,
                    thrusterForces.get(i).forth,
                    true
            );

            int finalI = i;
//            ShapeRenderingDebug.addToDrawThenRemove(() -> ShapeRenderingDebug.drawDebugCircle(thrusterForces.get(finalI).third, thrusterForces.get(finalI).forth, .5f));
//            ShapeRenderingDebug.addToDrawThenRemove(() -> ShapeRenderingDebug.drawDebugLine(0, 0, thrusterForces.get(finalI).third, thrusterForces.get(finalI).forth));
            ShapeRenderingDebug.drawDebugCircle(thrusterForces.get(finalI).third, thrusterForces.get(finalI).forth, .5f);

            thrusterForces.get(i).set(0f, 0f, 0f, 0f, 0);
            // TODO set speed limit, use normalization (.nor())
        }
    }


}