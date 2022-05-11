package com.myspacegame.systems;

import com.badlogic.ashley.core.*;
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

public class PieceSystem extends IteratingSystem {

    private final PooledEngine engine;

    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<ShipComponent> shipMapper;

    private final BodyFactory bodyFactory;
    private final Array<Piece> toDetach;
    private final com.badlogic.gdx.utils.ObjectSet<TractorBeamPiece> tractorBeamPieces;

    public PieceSystem(MainClass game, PooledEngine engine) {
        super(Family.all(PieceComponent.class, TransformComponent.class).get());
        this.engine = engine;
        World world = WorldFactory.getInstance(game, engine).getWorld();
        this.bodyFactory = BodyFactory.getInstance(world);

        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        shipMapper = ComponentMapper.getFor(ShipComponent.class);

        toDetach = new Array<>(false, 16, Piece.class);
        tractorBeamPieces = new com.badlogic.gdx.utils.ObjectSet<>(8);

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
            removeAnchors(pieceComponent.piece, entity);
            pieceComponent.toRemoveAnchors = false;
        }

        if(pieceComponent.piece instanceof TractorBeamPiece) {
            tractorBeamPiece((TractorBeamPiece) pieceComponent.piece);
        }

        if(!shipMapper.has(entity)) {
            computeLostPiece(pieceComponent);
        }
    }

    private void detachPiece(Piece piece, Entity entity, boolean recreateFixture) {
        boolean isShip = shipMapper.has(entity);
        if(isShip) {
            Array<Piece> piecesArray = shipMapper.get(entity).piecesArray;
            for(Piece p : piecesArray) p.checked = 0;
        }

        toDetach.clear();
        for(Anchor anchor : piece.anchors) {
            if(anchor.piece == null) continue;
            toDetach.add(anchor.piece);
        }

        removeAnchors(piece, entity);
        if(recreateFixture) recreateFixture(bodyFactory, piece.pieceComponent, entity);
        else destroyPiece(piece.pieceComponent, entity);
//        if(true) return;

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

    private void removeAnchors(Piece piece, Entity entity) {
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
        if(shipMapper.has(entity)) shipMapper.get(entity).piecesArray.removeValue(piece, true);
        recreateFixture(bodyFactory, piece.pieceComponent, entity);
    }

    private void recreateFixture(BodyFactory bodyFactory, PieceComponent pieceComponent, Entity entity) {
        pieceComponent.piece.pos.x = 0;
        pieceComponent.piece.pos.y = 0;

        Body body = bodyFactory.createPieceBody(pieceComponent.fixtureCenter.x, pieceComponent.fixtureCenter.y, pieceComponent.fixture.getBody().getAngle(), true);
        Fixture fixture = bodyFactory.createPieceFixture(body, pieceComponent.piece, entity);

        pieceComponent.fixture.getBody().destroyFixture(pieceComponent.fixture);
        pieceComponent.fixture = fixture;
        pieceComponent.fixture.getFilterData().maskBits = Info.MASK_NOTHING;
        pieceComponent.piece.actorId = Info.StaticActorIds.NONE.getValue();

        // TODO can probably delete this
//        if(shipMapper.has(entity)) shipMapper.get(entity).piecesArray.removeValue(pieceComponent.piece, true);

        entity.remove(NPCComponent.class);
        entity.remove(PlayerComponent.class);
        entity.remove(ShipComponent.class);
    }

    private void destroyPiece(PieceComponent pieceComponent, Entity entity) {
        pieceComponent.fixture.getBody().destroyFixture(pieceComponent.fixture);
        // TODO can probably delete this
//        if(shipMapper.has(entity)) shipMapper.get(entity).piecesArray.removeValue(pieceComponent.piece, true);
        engine.removeEntity(entity);
    }

    private void removeAnchorEntities(Piece piece) {
        for(Anchor anchor : piece.anchors) {
            anchor.anchorComponent.toRemove = true;
        }
    }

    private void tractorBeamPiece(TractorBeamPiece piece) {
        if(!piece.activated) {
            tractorBeamPieces.remove(piece);
            return;
        }
        tractorBeamPieces.add(piece);
    }

    private void computeLostPiece(PieceComponent pieceComponent) {
        if(tractorBeamPieces.size == 0) return;
        float distMin = Float.MAX_VALUE;
        TractorBeamPiece destPiece = null;

        for(TractorBeamPiece piece : tractorBeamPieces) {
            float dist = pieceComponent.fixtureCenter.dst2(piece.pieceComponent.fixtureCenter.x, piece.pieceComponent.fixtureCenter.y);
            if(dist < distMin && dist < piece.radius2) {
                distMin = dist;
                destPiece = piece;
            }
        }
        if(destPiece == null) return;

        ShapeRenderingDebug.drawDebugLine(pieceComponent.fixtureCenter.x, pieceComponent.fixtureCenter.y, destPiece.pieceComponent.fixtureCenter.x, destPiece.pieceComponent.fixtureCenter.y);
        float angle = MathUtils.atan2( destPiece.pieceComponent.fixtureCenter.y - pieceComponent.fixtureCenter.y, destPiece.pieceComponent.fixtureCenter.x - pieceComponent.fixtureCenter.x);

        Info.tempVector2 = pieceComponent.fixture.getBody().getLinearVelocity();
        System.out.println("velocity: " + Info.tempVector2.x + " " + Info.tempVector2.y);
        if(Math.abs(Info.tempVector2.x) + Math.abs(Info.tempVector2.y) < 1) {
//            pieceComponent.fixture.getBody().setLinearVelocity(0, 0);
            pieceComponent.fixture.getBody().applyForceToCenter(MathUtils.cos(angle) * 1, MathUtils.sin(angle) * 1, true);
        } else {
            pieceComponent.fixture.getBody().setLinearVelocity(Info.tempVector2.x * .95f, Info.tempVector2.y * .95f);
        }
    }
}
