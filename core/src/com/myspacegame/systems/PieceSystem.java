package com.myspacegame.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.*;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.components.pieces.TractorBeamPieceComponent;
import com.myspacegame.entities.Anchor;
import com.myspacegame.entities.CorePiece;
import com.myspacegame.entities.Piece;
import com.myspacegame.entities.TractorBeamPiece;
import com.myspacegame.factories.BodyFactory;
import com.myspacegame.factories.ShapeRenderingDebug;
import com.myspacegame.factories.WorldFactory;

public class PieceSystem extends IteratingSystem {

    private final PooledEngine engine;
    private final WorldFactory worldFactory;

    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<ShipComponent> shipMapper;
    private final ComponentMapper<TractorBeamPieceComponent> tractorBeamPieceMapper;

    private final BodyFactory bodyFactory;
    private final Array<Piece> toDetach;
    private final ObjectSet<TractorBeamPieceComponent> tractorBeamPieces;

    public PieceSystem(MainClass game, PooledEngine engine) {
        super(Family.all(PieceComponent.class, TransformComponent.class).get());
        this.engine = engine;
        this.worldFactory = WorldFactory.getInstance(game, engine);
        World world = this.worldFactory.getWorld();
        this.bodyFactory = BodyFactory.getInstance(world);

        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        shipMapper = ComponentMapper.getFor(ShipComponent.class);
        tractorBeamPieceMapper = ComponentMapper.getFor(TractorBeamPieceComponent.class);

        toDetach = new Array<>(false, 16, Piece.class);
        tractorBeamPieces = new ObjectSet<>(8);

    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PieceComponent pieceComponent = pieceMapper.get(entity);
        TransformComponent transformComponent = transformMapper.get(entity);

        transformComponent.angleRad = pieceComponent.fixture.getBody().getAngle();
        transformComponent.position.x = pieceComponent.fixtureCenter.x;
        transformComponent.position.y = pieceComponent.fixtureCenter.y;

        if(pieceComponent.isDead) {
            if(pieceComponent.piece instanceof CorePiece && entity.getComponent(PlayerComponent.class) != null) {
                this.worldFactory.gameOver(false);
                Info.playerIsDead = true;
            // TODO this is a little broken rn
//                detachShip(pieceComponent, entity);
//                return;
            }
            detachPiece(pieceComponent.piece, entity, false);
            // isDead will be reset when entity is removed (component pooling)
            removeAnchorEntities(pieceComponent.piece);
            return;
        }
        if(pieceComponent.isManuallyDetached) {
            detachPiece(pieceComponent.piece, entity, true);
            pieceComponent.isManuallyDetached = false;
        }
        if(pieceComponent.toRemoveAnchors) {
            removeAnchors(pieceComponent.piece, entity, true);
            pieceComponent.toRemoveAnchors = false;
        }

        if(tractorBeamPieceMapper.has(entity)) {
            tractorBeamPiece(tractorBeamPieceMapper.get(entity), deltaTime);
        }

        if(!shipMapper.has(entity)) {
            computeLostPiece(pieceComponent);
        }
    }

    private void detachPiece(Piece piece, Entity entity, boolean recreateFixture) {
        boolean isShip = shipMapper.has(entity);
        if(isShip) {
            Array<Piece> piecesArray = shipMapper.get(entity).shipData.piecesArray;
            for(Piece p : piecesArray) p.checked = 0;
        }

        toDetach.clear();
        for(Anchor anchor : piece.anchors) {
            if(anchor.piece == null) continue;
            toDetach.add(anchor.piece);
        }

        removeAnchors(piece, entity, recreateFixture);

        Array<Piece> toDetachQueue = new Array<>(16);
        for(Piece toDetachPieceStart : toDetach) {
            int k = 0;
            toDetachQueue.clear();
            toDetachQueue.add(toDetachPieceStart);
            while(k < toDetachQueue.size) {
                if(toDetachQueue.get(k) instanceof CorePiece) {
                    for(int i = 0; i < toDetachQueue.size; i++) {
                        toDetachQueue.get(i).checked = 2;
                    }
                    toDetachQueue.clear();
                    break;
                }
                toDetachQueue.get(k).checked = 1;

                for(Anchor anchor : toDetachQueue.get(k).anchors) {
                    if(anchor.piece == null) continue;
                    if(anchor.piece.checked == 1) continue;
                    if(anchor.piece.checked == 2) {
                        for(int i = 0; i < toDetachQueue.size; i++) {
                            toDetachQueue.get(i).checked = 2;
                        }
                        toDetachQueue.clear();
                        break;
                    }
                    toDetachQueue.add(anchor.piece);
                }
                k++;
            }

            for(Piece detachingPiece : toDetachQueue) {
                detachingPiece.pieceComponent.toRemoveAnchors = true;
            }
        }
    }

    private void detachShip(PieceComponent pieceComponent, Entity entity) {
        ShipComponent shipComponent = shipMapper.get(entity);
        for(Piece piece : shipComponent.shipData.piecesArray) {
            recreateFixture(bodyFactory, piece.pieceComponent);
            piece.pieceComponent.toRemoveAnchors = true;
        }
        destroyPiece(pieceComponent);
    }

    private void removeAnchors(Piece piece, Entity entity, boolean recreateFixture) {
        for(int i = 0; i < piece.anchors.size; i++) {
            Anchor anchor = piece.anchors.get(i);
            if(anchor.piece == null) continue;
            Piece nextPiece = anchor.piece;
            for(Anchor a : nextPiece.anchors) {
                if(a.piece == null || a.piece != piece) continue;
                a.piece = null;
            }
            piece.anchors.get(i).piece = null;
        }

        piece.actorId = Info.StaticActorIds.NONE.getValue();
        if(shipMapper.has(entity)) {
            shipMapper.get(entity).shipData.piecesArray.removeValue(piece, true);
        }

        if(recreateFixture) recreateFixture(bodyFactory, piece.pieceComponent);
        else destroyPiece(piece.pieceComponent);
    }

    private void recreateFixture(BodyFactory bodyFactory, PieceComponent pieceComponent) {
        pieceComponent.piece.pos.x = 0;
        pieceComponent.piece.pos.y = 0;

        Entity entity = (Entity) pieceComponent.fixture.getUserData();
        Body body = bodyFactory.createPieceBody(pieceComponent.fixtureCenter.x, pieceComponent.fixtureCenter.y, pieceComponent.fixture.getBody().getAngle(), true);
        Fixture fixture = bodyFactory.createPieceFixture(body, pieceComponent.piece, entity);

        pieceComponent.fixture.getBody().destroyFixture(pieceComponent.fixture);
        pieceComponent.fixture = fixture;
        pieceComponent.fixture.getFilterData().maskBits = Info.MASK_NOTHING;
        pieceComponent.piece.actorId = Info.StaticActorIds.NONE.getValue();

        entity.remove(NPCComponent.class);
        entity.remove(PlayerComponent.class);
        entity.remove(ShipComponent.class);
    }

    private void destroyPiece(PieceComponent pieceComponent) {
        Entity entity = (Entity) pieceComponent.fixture.getUserData();
        pieceComponent.fixture.getBody().destroyFixture(pieceComponent.fixture);
        engine.removeEntity(entity);
    }

    private void removeAnchorEntities(Piece piece) {
        for(Anchor anchor : piece.anchors) {
            anchor.anchorComponent.toRemove = true;
        }
    }

    private void tractorBeamPiece(TractorBeamPieceComponent tractorBeamPieceComponent, float delta) {
        if(tractorBeamPieceComponent.active) {
            if(tractorBeamPieceComponent.state == 0) {
                tractorBeamPieceComponent.state = 1;
            }
            if(tractorBeamPieceComponent.state == 1) {
                tractorBeamPieceComponent.prepareToPullDelay += delta;
                tractorBeamPieces.add(tractorBeamPieceComponent);
            }
            if(tractorBeamPieceComponent.prepareToPullDelay >= tractorBeamPieceComponent.prepareToPullDelayMax) {
                tractorBeamPieceComponent.state = 2;
            }
        } else {
            if(tractorBeamPieceComponent.state == 2) {
                tractorBeamPieceComponent.state = 3;
            }
            if(tractorBeamPieceComponent.state == 3) {
                tractorBeamPieceComponent.ceasePullDelay += delta;
            }
            if(tractorBeamPieceComponent.ceasePullDelay >= tractorBeamPieceComponent.ceasePullDelayMax) {
                tractorBeamPieces.remove(tractorBeamPieceComponent);
                tractorBeamPieceComponent.reset();
            }
        }

    }

    private void computeLostPiece(PieceComponent pieceComponent) {
        if(tractorBeamPieces.size == 0) return;
        float distMin = Float.MAX_VALUE;
        TractorBeamPieceComponent tractorBeamPieceComponentDest = null;

        for(TractorBeamPieceComponent tractorBeamPieceComponent : tractorBeamPieces) {
            TractorBeamPiece piece = tractorBeamPieceComponent.piece;
            float dist = pieceComponent.fixtureCenter.dst2(piece.pieceComponent.fixtureCenter.x, piece.pieceComponent.fixtureCenter.y);
            if(dist < distMin && dist < piece.radius2) {
                distMin = dist;
                tractorBeamPieceComponentDest = tractorBeamPieceComponent;
            }
        }
        if(tractorBeamPieceComponentDest == null) return;

        Piece destPiece = tractorBeamPieceComponentDest.piece;
        ShapeRenderingDebug.drawDebugLine(pieceComponent.fixtureCenter.x, pieceComponent.fixtureCenter.y, destPiece.pieceComponent.fixtureCenter.x, destPiece.pieceComponent.fixtureCenter.y);

        if(tractorBeamPieceComponentDest.state == 1) {
            Info.tempVector2 = pieceComponent.fixture.getBody().getLinearVelocity();
            pieceComponent.fixture.getBody().setLinearVelocity(Info.tempVector2.x * .8f, Info.tempVector2.y * .8f);
        } else if(tractorBeamPieceComponentDest.state == 2) {
            float angle = MathUtils.atan2( destPiece.pieceComponent.fixtureCenter.y - pieceComponent.fixtureCenter.y, destPiece.pieceComponent.fixtureCenter.x - pieceComponent.fixtureCenter.x);
            pieceComponent.fixture.getBody().applyForceToCenter(MathUtils.cos(angle) * tractorBeamPieceComponentDest.piece.force, MathUtils.sin(angle) * tractorBeamPieceComponentDest.piece.force, true);
        } else if(tractorBeamPieceComponentDest.state == 3) {
            Info.tempVector2 = pieceComponent.fixture.getBody().getLinearVelocity();
            if(Math.abs(Info.tempVector2.x) + Math.abs(Info.tempVector2.y) < 1) {
                pieceComponent.fixture.getBody().setLinearVelocity(0, 0);
            } else {
                pieceComponent.fixture.getBody().setLinearVelocity(Info.tempVector2.x * .8f, Info.tempVector2.y * .8f);
            }
        }
    }
}
