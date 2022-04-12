package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
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
import com.myspacegame.factories.BodyFactory;
import com.myspacegame.factories.WorldFactory;

public class PieceSystem extends IteratingSystem {

    private final PooledEngine engine;

    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<ShipComponent> shipMapper;

    private final BodyFactory bodyFactory;
    private final Array<Piece> toDetach;

    public PieceSystem(MainClass game, PooledEngine engine) {
        super(Family.all(PieceComponent.class, TransformComponent.class).get());
        this.engine = engine;
        World world = WorldFactory.getInstance(game, engine).getWorld();
        this.bodyFactory = BodyFactory.getInstance(world);

        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        shipMapper = ComponentMapper.getFor(ShipComponent.class);

        toDetach = new Array<>(false, 16, Piece.class);
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
        }
        if(pieceComponent.isManuallyDetached) {
            detachPiece(pieceComponent.piece, entity, true);
            pieceComponent.isManuallyDetached = false;
        }
        if(pieceComponent.toRemoveAnchors) {
            removeAnchors(pieceComponent.piece, entity);
            pieceComponent.toRemoveAnchors = false;
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

        Array<Piece> toDetachArray = new Array<>(16);
        for(Piece toDetachPieceStart : toDetach) {
            int k = 0;
            toDetachArray.add(toDetachPieceStart);
            while(k < toDetachArray.size) {
                if(toDetachArray.get(k) instanceof CorePiece) {
                    for(int i = 0; i < toDetachArray.size; i++) {
                        toDetachArray.get(i).checked = 2;
                    }
                    toDetachArray.clear();
                    break;
                }
                toDetachArray.get(k).checked = 1;

                for(Anchor anchor : toDetachArray.get(k).anchors) {
                    if(anchor.piece == null) continue;
                    if(anchor.piece.checked == 1) continue;
                    if(anchor.piece.checked == 2) {
                        toDetachArray.clear();
                        break;
                    }
                    toDetachArray.add(anchor.piece);
                }
                k++;
            }

            for(Piece detachingPiece : toDetachArray) {
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

        piece.actorId = -1;
        if(shipMapper.has(entity)) shipMapper.get(entity).piecesArray.removeValue(piece, true);
        recreateFixture(bodyFactory, piece.pieceComponent, entity);
    }

    private void recreateFixture(BodyFactory bodyFactory, PieceComponent pieceComponent, Entity entity) {
        pieceComponent.piece.pos.x = 0;
        pieceComponent.piece.pos.y = 0;

        Body body = bodyFactory.createPieceBody(pieceComponent.fixtureCenter.x, pieceComponent.fixtureCenter.y, pieceComponent.fixture.getBody().getAngle());
        Fixture fixture = bodyFactory.createPieceFixture(body, pieceComponent.piece, entity);

        pieceComponent.fixture.getBody().destroyFixture(pieceComponent.fixture);
        pieceComponent.fixture = fixture;
        pieceComponent.fixture.getFilterData().maskBits = Info.MASK_NOTHING;
        pieceComponent.piece.actorId = -1;

        // TODO can probably delete this
        if(shipMapper.has(entity)) shipMapper.get(entity).piecesArray.removeValue(pieceComponent.piece, true);

        entity.remove(NPCComponent.class);
        entity.remove(PlayerComponent.class);
        entity.remove(ShipComponent.class);
    }

    private void destroyPiece(PieceComponent pieceComponent, Entity entity) {
        pieceComponent.fixture.getBody().destroyFixture(pieceComponent.fixture);
        // TODO can probably delete this
        if(shipMapper.has(entity)) shipMapper.get(entity).piecesArray.removeValue(pieceComponent.piece, true);
        engine.removeEntity(entity);
    }

}
