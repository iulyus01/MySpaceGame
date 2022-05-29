package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.*;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.entities.*;
import com.myspacegame.factories.EntitiesFactory;
import com.myspacegame.factories.WorldFactory;
import com.myspacegame.utils.Functions;

public class AIControlSystem extends IteratingSystem {

    private final ComponentMapper<ShipComponent> shipMapper;
    private final ComponentMapper<NPCComponent> npcMapper;
    private final ComponentMapper<AIComponent> aiMapper;
    private final PooledEngine engine;
    private final EntitiesFactory entitiesFactory;

    private final Array<Info.Quintuple<Float, Float, Float, Float, Integer>> thrusterForces;

    private final Array<Info.Pair<ShipData, AIComponent>> ships;

    private ShipData playerShip;
    private Body playerBody;


    public AIControlSystem(MainClass game, PooledEngine engine) {
        super(Family.all(NPCComponent.class, ShipCoreComponent.class, ShipComponent.class).get());
        this.engine = engine;
        World world = WorldFactory.getInstance(game, engine).getWorld();

        shipMapper = ComponentMapper.getFor(ShipComponent.class);
        npcMapper = ComponentMapper.getFor(NPCComponent.class);
        aiMapper = ComponentMapper.getFor(AIComponent.class);

        entitiesFactory = EntitiesFactory.getInstance(game, engine, world);

        thrusterForces = new Array<>(true, 4, Info.Quintuple.class);
        thrusterForces.add(new Info.Quintuple<>(0f, 0f, 0f, 0f, 0));
        thrusterForces.add(new Info.Quintuple<>(0f, 0f, 0f, 0f, 0));
        thrusterForces.add(new Info.Quintuple<>(0f, 0f, 0f, 0f, 0));
        thrusterForces.add(new Info.Quintuple<>(0f, 0f, 0f, 0f, 0));

        ships = new Array<>(false, 16, Info.Pair.class);

        Entity entity = engine.getEntitiesFor(Family.all(ShipCoreComponent.class, PlayerComponent.class, PieceComponent.class).get()).first();
        playerShip = shipMapper.get(entity).shipData;
        playerBody = playerShip.core.pieceComponent.fixture.getBody();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ShipComponent shipComponent = shipMapper.get(entity);
        AIComponent aiComponent = aiMapper.get(entity);

        updateMovement(aiComponent, shipComponent, deltaTime);

        updateShooting(aiComponent, shipComponent, deltaTime);


        ships.add(new Info.Pair<>(shipComponent.shipData, aiComponent));
    }


    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        for(Info.Pair<ShipData, AIComponent> pair : ships) {
            ShipData shipData = pair.first;
            AIComponent aiComponent = pair.second;

            Body shipBody = shipData.core.pieceComponent.fixture.getBody();
            float angleDiffRad = computeShipAutoRotation(aiComponent, shipBody);

            for(Piece piece : shipData.piecesArray) {
                if(piece instanceof ThrusterPiece) {
                    handleThruster(aiComponent, piece.pieceComponent, shipBody, angleDiffRad);
                } else if(piece instanceof WeaponPiece) {
                    handleWeapon(aiComponent, piece.pieceComponent, deltaTime);
                }
            }

            applyForces(shipBody, aiComponent);
            float velX = shipBody.getLinearVelocity().x;
            float velY = shipBody.getLinearVelocity().y;
            velX = Math.signum(velX) * Math.min(Math.abs(velX), Info.maxHorVerVelocity);
            velY = Math.signum(velY) * Math.min(Math.abs(velY), Info.maxHorVerVelocity);
            shipBody.setLinearVelocity(velX, velY);
        }
        ships.clear();

    }

    private void updateMovement(AIComponent aiComponent, ShipComponent shipComponent, float deltaTime) {
        aiComponent.movementTargetDelay += deltaTime;
        if(aiComponent.movementTargetDelay >= aiComponent.movementTargetDelayMax) {
            if(shipComponent.shipData.core.pieceComponent.fixture.getBody().getPosition().dst2(playerBody.getPosition()) < 1600) {
                aiComponent.movementScale = .6f;
                aiComponent.movementTargetX = MathUtils.random(playerBody.getPosition().x - 10, playerBody.getPosition().x + 10);
                aiComponent.movementTargetY = MathUtils.random(playerBody.getPosition().y - 10, playerBody.getPosition().y + 10);
            } else {
                aiComponent.movementScale = .3f;
                aiComponent.movementTargetX = MathUtils.random(-Info.worldWidthLimit + 10, Info.worldWidthLimit - 10);
                aiComponent.movementTargetY = MathUtils.random(-Info.worldHeightLimit + 10, Info.worldHeightLimit - 10);
            }
        }

        aiComponent.movingDelay += deltaTime;
        if(aiComponent.movingDelay > aiComponent.movingDelayMax) {
            aiComponent.forward = !aiComponent.forward;
            aiComponent.movingDelay = 0;
        }
    }

    private void updateShooting(AIComponent aiComponent, ShipComponent shipComponent, float deltaTime) {
        if(shipComponent.shipData.core.pieceComponent.fixture.getBody().getPosition().dst2(playerBody.getPosition()) < 900) {
            if(playerShip.piecesArray.size > 0) {
                int index = MathUtils.random(0, playerShip.piecesArray.size - 1);
                aiComponent.shotTargetX = playerShip.piecesArray.get(index).pieceComponent.fixtureCenter.x;
                aiComponent.shotTargetY = playerShip.piecesArray.get(index).pieceComponent.fixtureCenter.y;
            } else {

                aiComponent.shotTargetX = playerBody.getPosition().x;
                aiComponent.shotTargetY = playerBody.getPosition().y;
            }
            if(aiComponent.shooting) {
                aiComponent.shootingDelay += deltaTime;
                if(aiComponent.shootingDelay > aiComponent.shootingDelayMax) {
                    aiComponent.shooting = false;
                    aiComponent.shootingDelay = 0;
                }
            } else {
                aiComponent.notShootingDelay += deltaTime;
                if(aiComponent.notShootingDelay > aiComponent.notShootingDelayMax) {
                    aiComponent.shooting = true;
                    aiComponent.notShootingDelay = 0;
                }
            }
        } else {
            aiComponent.shooting = false;
        }
    }

    private float computeShipAutoRotation(AIComponent aiComponent, Body shipBody) {
        float angleDiffRad;

        float diffX = aiComponent.movementTargetX - shipBody.getWorldCenter().x;
        float diffY = aiComponent.movementTargetY - shipBody.getWorldCenter().y;
        if(Math.abs(diffX) < Info.blockSize * 3 && Math.abs(diffY) < Info.blockSize * 3) {
            return 0;
        }

        int playerBodyAngleRotations = (int) ((shipBody.getAngle() + Math.signum(shipBody.getAngle()) * Info.rad180Deg) / Info.rad360Deg);
        float playerBodyAngle = shipBody.getAngle() - playerBodyAngleRotations * Info.rad360Deg;
        float cursorAngle = MathUtils.atan2(diffY, diffX);
        angleDiffRad = cursorAngle - playerBodyAngle;
        if(Math.abs(Info.rad360Deg + angleDiffRad) < Math.abs(angleDiffRad)) angleDiffRad += Info.rad360Deg;
        else if((Math.abs(angleDiffRad - Info.rad360Deg) < Math.abs(angleDiffRad))) angleDiffRad -= Info.rad360Deg;

        return angleDiffRad;
    }

    private void handleThruster(AIComponent aiComponent, PieceComponent pieceComponent, Body shipBody, float angleDiffRad) {
        ThrusterPiece piece = (ThrusterPiece) pieceComponent.piece;

        float computedLinearImpulse = Info.defaultThrusterForce;

        // ---------- HANDLING MOVEMENT ----------
        boolean activated = piece.rotation == 0 && aiComponent.forward;


        // ---------- HANDLING AUTO ROTATION ----------
        if(!activated) {
            float x = Math.abs(angleDiffRad) / Info.rad180Deg;
            float y = Functions.rotatingThrustersFunction(x);

            computedLinearImpulse *= y;
            if(angleDiffRad > 0) {
                // rotate to left
                if(piece.rotation == 0 && shipBody.getLocalCenter().y > piece.pos.y * Info.blockSize) {
                    activated = true;
                } else if(piece.rotation == 1 && shipBody.getLocalCenter().x < piece.pos.x * Info.blockSize) {
                    activated = true;
                } else if(piece.rotation == 2 && shipBody.getLocalCenter().y < piece.pos.y * Info.blockSize) {
                    activated = true;
                } else if(piece.rotation == 3 && shipBody.getLocalCenter().x > piece.pos.x * Info.blockSize) {
                    activated = true;
                }
            } else {
                // rotate to right
                if(piece.rotation == 0 && shipBody.getLocalCenter().y < piece.pos.y * Info.blockSize) {
                    activated = true;
                } else if(piece.rotation == 1 && shipBody.getLocalCenter().x > piece.pos.x * Info.blockSize) {
                    activated = true;
                } else if(piece.rotation == 2 && shipBody.getLocalCenter().y > piece.pos.y * Info.blockSize) {
                    activated = true;
                } else if(piece.rotation == 3 && shipBody.getLocalCenter().x < piece.pos.x * Info.blockSize) {
                    activated = true;
                }
            }
            if(piece.rotation % 2 == 0) computedLinearImpulse /= 2;
        }


        if(activated) {
            float angleX = (float) Math.cos(shipBody.getAngle() + piece.rotation * Info.rad90Deg);
            float angleY = (float) Math.sin(shipBody.getAngle() + piece.rotation * Info.rad90Deg);
            Vector2 fixtureCenter = pieceComponent.fixtureCenter;

            var q = thrusterForces.get(piece.rotation);
            int nr = q.fifth;
            float impulseX = q.first + angleX * computedLinearImpulse * aiComponent.movementScale;
            float impulseY = q.second + angleY * computedLinearImpulse * aiComponent.movementScale;
            float pointX = (q.third * nr + fixtureCenter.x) / (nr + 1);
            float pointY = (q.forth * nr + fixtureCenter.y) / (nr + 1);

            q.set(impulseX, impulseY, pointX, pointY, nr + 1);
        }

    }

    private void handleWeapon(AIComponent aiComponent, PieceComponent pieceComponent, float deltaTime) {
        WeaponPiece piece = (WeaponPiece) pieceComponent.piece;

        // weapon angle, oriented to mouse
        if(!piece.fixedAngle) {
            Vector2 fixtureCenter = pieceComponent.fixtureCenter;
            piece.angleRad = (float) Math.atan2(aiComponent.shotTargetY - fixtureCenter.y, aiComponent.shotTargetX - fixtureCenter.x);
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
        if(aiComponent.shooting) {
            if(!piece.isReloading) {
                Vector2 pos = pieceComponent.fixtureCenter;

                Entity bulletEntity = entitiesFactory.createBulletEntity(piece.actorId, pos.x, pos.y, piece.angleRad);
                engine.addEntity(bulletEntity);
                piece.isReloading = true;

            }
        }
    }

    private void applyForces(Body shipBody, AIComponent aiComponent) {
//        int keysPressed = aiComponent.forward ? 1 : 0;
//        if(keysPressed == 0) {
//            boolean firstIsSmaller = Math.abs(thrusterForces.get(0).first) + Math.abs(thrusterForces.get(0).second) < Math.abs(thrusterForces.get(2).first) + Math.abs(thrusterForces.get(2).second);
//            if(Math.signum(thrusterForces.get(0).first) != Math.signum(thrusterForces.get(2).first)) {
//                if(firstIsSmaller) thrusterForces.get(2).first = -thrusterForces.get(0).first;
//                else thrusterForces.get(0).first = -thrusterForces.get(2).first;
//            } else {
//                if(firstIsSmaller) thrusterForces.get(2).first = thrusterForces.get(0).first;
//                else thrusterForces.get(0).first = thrusterForces.get(2).first;
//            }
//
//            firstIsSmaller = Math.abs(thrusterForces.get(1).first) + Math.abs(thrusterForces.get(1).second) < Math.abs(thrusterForces.get(3).first) + Math.abs(thrusterForces.get(3).second);
//            if(Math.signum(thrusterForces.get(1).first) != Math.signum(thrusterForces.get(3).first)) {
//                if(firstIsSmaller) thrusterForces.get(3).first = -thrusterForces.get(1).first;
//                else thrusterForces.get(1).first = -thrusterForces.get(3).first;
//            } else {
//                if(firstIsSmaller) thrusterForces.get(3).first = thrusterForces.get(1).first;
//                else thrusterForces.get(1).first = thrusterForces.get(3).first;
//            }
//
//        }
        for(int i = 0; i < thrusterForces.size; i++) {
            if(thrusterForces.get(i).fifth == 0) continue;
            shipBody.applyForce(
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
