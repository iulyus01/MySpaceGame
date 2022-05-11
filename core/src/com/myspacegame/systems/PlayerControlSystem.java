package com.myspacegame.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Input;
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
import com.myspacegame.components.pieces.TractorBeamPieceComponent;
import com.myspacegame.components.pieces.WeaponPieceComponent;
import com.myspacegame.entities.ThrusterPiece;
import com.myspacegame.entities.TractorBeamPiece;
import com.myspacegame.entities.WeaponPiece;
import com.myspacegame.factories.EntitiesFactory;
import com.myspacegame.factories.SystemManager;
import com.myspacegame.factories.WorldFactory;
import com.myspacegame.utils.Functions;

public class PlayerControlSystem extends IteratingSystem {

    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<DraggingComponent> draggingMapper;
    private final ComponentMapper<ThrusterPieceComponent> thrusterPieceMapper;
    private final ComponentMapper<WeaponPieceComponent> weaponPieceMapper;
    private final ComponentMapper<TractorBeamPieceComponent> tractorBeamPieceMapper;
    private final KeyboardController controller;
    private final PooledEngine engine;
    private final OrthographicCamera camera;
    private final EntitiesFactory entitiesFactory;

    private final Body playerBody;
    private float angleDiffRad;

    private final Array<Info.Quintuple<Float, Float, Float, Float, Integer>> thrusterForces;

    public PlayerControlSystem(KeyboardController keyboardController, MainClass game, PooledEngine engine, OrthographicCamera camera) {
        super(Family.all(PlayerComponent.class).one(ThrusterPieceComponent.class, WeaponPieceComponent.class, TractorBeamPieceComponent.class).get());
        this.engine = engine;
        this.camera = camera;
        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        draggingMapper = ComponentMapper.getFor(DraggingComponent.class);
        thrusterPieceMapper = ComponentMapper.getFor(ThrusterPieceComponent.class);
        weaponPieceMapper = ComponentMapper.getFor(WeaponPieceComponent.class);
        tractorBeamPieceMapper = ComponentMapper.getFor(TractorBeamPieceComponent.class);
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
                computeShipAutoRotation();
                applyForces();

                float velX = playerBody.getLinearVelocity().x;
                float velY = playerBody.getLinearVelocity().y;
                velX = Math.signum(velX) * Math.min(Math.abs(velX), Info.maxHorVerVelocity);
                velY = Math.signum(velY) * Math.min(Math.abs(velY), Info.maxHorVerVelocity);
                playerBody.setLinearVelocity(velX, velY);

                camera.position.set(playerBody.getWorldCenter().x, playerBody.getWorldCenter().y, 0);
                camera.update();

                break;
            case BUILDING:
                playerBody.setLinearVelocity(0, 0);
                playerBody.setAngularVelocity(0);

                manuallyRotateShipTo0();

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

        if(thrusterPieceMapper.has(entity)) {
            handleThruster(entity, transformComponent, pieceComponent);
        } else if(weaponPieceMapper.has(entity)) {
            handleWeapon(entity, pieceComponent, deltaTime);
        } else if(tractorBeamPieceMapper.has(entity)) {
            handleTractorBeam(pieceComponent);
        }
    }

    private void computeShipAutoRotation() {
        float diffX = Info.mouseWorldX - playerBody.getWorldCenter().x;
        float diffY = Info.mouseWorldY - playerBody.getWorldCenter().y;
        if(Math.abs(diffX) < Info.blockSize * 3 && Math.abs(diffY) < Info.blockSize * 3) {
            angleDiffRad = 0;
            return;
        }

        int playerBodyAngleRotations = (int) ((playerBody.getAngle() + Math.signum(playerBody.getAngle()) * Info.rad180Deg) / Info.rad360Deg);
        float playerBodyAngle = playerBody.getAngle() - playerBodyAngleRotations * Info.rad360Deg;
        float cursorAngle = MathUtils.atan2(diffY, diffX);
        angleDiffRad = cursorAngle - playerBodyAngle;
        if(Math.abs(Info.rad360Deg + angleDiffRad) < Math.abs(angleDiffRad)) angleDiffRad += Info.rad360Deg;
        else if((Math.abs(angleDiffRad - Info.rad360Deg) < Math.abs(angleDiffRad))) angleDiffRad -= Info.rad360Deg;
    }

    private void handleThruster(Entity entity, TransformComponent transformComponent, PieceComponent pieceComponent) {
        ThrusterPieceComponent thrusterComponent = thrusterPieceMapper.get(entity);
        ThrusterPiece piece = thrusterComponent.piece;

        float computedLinearImpulse = Info.defaultThrusterForce;

        // ---------- HANDLING MOVEMENT ----------
        boolean activated = controller.keysDown.get(Info.movementKeys[piece.rotation], 0) == 1;


        // ---------- HANDLING AUTO ROTATION ----------
        if(!activated) {
            float x = Math.abs(angleDiffRad) / Info.rad180Deg;
            float y = Functions.rotatingThrustersFunction(x);

            computedLinearImpulse *= y;
            if(angleDiffRad > 0) {
                // rotate to left
                if(piece.rotation == 0 && playerBody.getLocalCenter().y > piece.pos.y * Info.blockSize) {
                    activated = true;
                } else if(piece.rotation == 1 && playerBody.getLocalCenter().x < piece.pos.x * Info.blockSize) {
                    activated = true;
                } else if(piece.rotation == 2 && playerBody.getLocalCenter().y < piece.pos.y * Info.blockSize) {
                    activated = true;
                } else if(piece.rotation == 3 && playerBody.getLocalCenter().x > piece.pos.x * Info.blockSize) {
                    activated = true;
                }
            } else {
                // rotate to right
                if(piece.rotation == 0 && playerBody.getLocalCenter().y < piece.pos.y * Info.blockSize) {
                    activated = true;
                } else if(piece.rotation == 1 && playerBody.getLocalCenter().x > piece.pos.x * Info.blockSize) {
                    activated = true;
                } else if(piece.rotation == 2 && playerBody.getLocalCenter().y > piece.pos.y * Info.blockSize) {
                    activated = true;
                } else if(piece.rotation == 3 && playerBody.getLocalCenter().x < piece.pos.x * Info.blockSize) {
                    activated = true;
                }
            }
            if(piece.rotation % 2 == 0) computedLinearImpulse /= 2;
        }


        if(activated) {
            float angleX = (float) Math.cos(transformComponent.angleRad + thrusterComponent.piece.rotation * Info.rad90Deg);
            float angleY = (float) Math.sin(transformComponent.angleRad + thrusterComponent.piece.rotation * Info.rad90Deg);
            Vector2 fixtureCenter = pieceComponent.fixtureCenter;

            var q = thrusterForces.get(piece.rotation);
            int nr = q.fifth;
            float impulseX = q.first + angleX * computedLinearImpulse;
            float impulseY = q.second + angleY * computedLinearImpulse;
            float pointX = (q.third * nr + fixtureCenter.x) / (nr + 1);
            float pointY = (q.forth * nr + fixtureCenter.y) / (nr + 1);

            q.set(impulseX, impulseY, pointX, pointY, nr + 1);
        }

    }

    private void handleWeapon(Entity entity, PieceComponent pieceComponent, float deltaTime) {
        WeaponPieceComponent weaponComponent = weaponPieceMapper.get(entity);
        WeaponPiece piece = weaponComponent.piece;

        // weapon angle, oriented to mouse
        if(!piece.fixedAngle) {
            Vector2 fixtureCenter = pieceComponent.fixtureCenter;
            piece.angleRad = (float) Math.atan2(Info.mouseWorldY - fixtureCenter.y, Info.mouseWorldX - fixtureCenter.x);
        }

        // reloading
        if(piece.isReloading) {
            piece.reloadingTime += deltaTime;
            if(piece.reloadingTime >= piece.reloadingTimeMax) {
                piece.isReloading = false;
                piece.reloadingTime = 0;
            }
        }

        // fire
        boolean mouseLeftPressed = controller.mouseLeft;
        if(mouseLeftPressed) {
            if(!piece.isReloading) {
                Vector2 pos = pieceComponent.fixtureCenter;

                Entity bulletEntity = entitiesFactory.createBulletEntity(piece.actorId, pos.x, pos.y, piece.angleRad);
                engine.addEntity(bulletEntity);
                piece.isReloading = true;

            }
        }
    }

    public void handleTractorBeam(PieceComponent pieceComponent) {
        TractorBeamPiece piece = (TractorBeamPiece) pieceComponent.piece;
        piece.activated = controller.keysDown.get(piece.activateKey, 0) == 1;

    }

    private void handleModeChange() {
        if(controller.bPressed) {
            controller.bPressed = false;
            if(Info.activeMode == Info.PlayerMode.MOVING) {
                Info.activeMode = Info.PlayerMode.BUILDING;
                SystemManager.getInstance().toggleBuilding(true);
            } else if(Info.activeMode == Info.PlayerMode.BUILDING) {
                Info.activeMode = Info.PlayerMode.MOVING;
                SystemManager.getInstance().toggleBuilding(false);
            }
        }
    }

    private void manuallyRotateShipTo0() {
        int angle = (int) (playerBody.getAngle() * MathUtils.radDeg) % 360;
        if(angle > 180) angle = angle - 360;
        else if(angle < -180) angle = angle + 360;

        if(Math.abs(playerBody.getAngle()) > 1 * MathUtils.degRad)
            playerBody.setTransform(playerBody.getPosition(), angle * MathUtils.degRad * .9f);
        else playerBody.setTransform(playerBody.getPosition(), 0);
    }

    private void handleBuildingCamera(float delta) {
        float distPerFrame = Info.blockSize * delta * 20;
        float distX = 0;
        float distY = 0;

        if(controller.keysDown.get(Input.Keys.D, 0) == 1) distX += distPerFrame;
        if(controller.keysDown.get(Input.Keys.W, 0) == 1) distY += distPerFrame;
        if(controller.keysDown.get(Input.Keys.A, 0) == 1) distX -= distPerFrame;
        if(controller.keysDown.get(Input.Keys.S, 0) == 1) distY -= distPerFrame;

        camera.position.set(camera.position.x + distX, camera.position.y + distY, 0);
        camera.update();
    }

    private void handleBuildingKeys() {
    }

    private void applyForces() {
        for(int i = 0; i < thrusterForces.size; i++) {
            if(thrusterForces.get(i).fifth == 0) continue;
            playerBody.applyForce(
//            playerBody.applyLinearImpulse(
                    thrusterForces.get(i).first,
                    thrusterForces.get(i).second,
                    thrusterForces.get(i).third,
                    thrusterForces.get(i).forth,
                    true
            );

//            ShapeRenderingDebug.drawDebugCircle(thrusterForces.get(i).third, thrusterForces.get(i).forth, .5f);

            thrusterForces.get(i).set(0f, 0f, 0f, 0f, 0);
            // TODO set speed limit, use normalization (.nor())
        }
    }


}