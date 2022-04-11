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
import com.myspacegame.factories.EntitiesFactory;
import com.myspacegame.factories.WorldFactory;

import java.util.Arrays;

public class PieceSystem extends IteratingSystem {

    private final PooledEngine engine;

    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<ShipComponent> shipMapper;

    private final EntitiesFactory entitiesFactory;

    private final BodyFactory bodyFactory;
    private final Array<Piece> connectedPiecesToDetach;

    public PieceSystem(MainClass game, PooledEngine engine) {
        super(Family.all(PieceComponent.class, TransformComponent.class).get());
        this.engine = engine;
        World world = WorldFactory.getInstance(game, engine).getWorld();
        this.bodyFactory = BodyFactory.getInstance(world);
        this.entitiesFactory = EntitiesFactory.getInstance(game, engine, world);

        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        shipMapper = ComponentMapper.getFor(ShipComponent.class);

        connectedPiecesToDetach = new Array<>(false, 16, Piece.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PieceComponent pieceComponent = pieceMapper.get(entity);
        TransformComponent transformComponent = transformMapper.get(entity);

        transformComponent.angleRad = pieceComponent.fixture.getBody().getAngle();
        transformComponent.position.x = pieceComponent.fixtureCenter.x;
        transformComponent.position.y = pieceComponent.fixtureCenter.y;

        if(pieceComponent.isDead) {
            destroyPiece(pieceComponent, entity);
            // isDead will be reset when entity is removed
        }
        if(pieceComponent.isManuallyDetached) {
            detachPieceMultiple(pieceComponent.piece, entity);
            pieceComponent.isManuallyDetached = false;
        }
        if(pieceComponent.toRemoveAnchors) {
            detachPieceSingle(pieceComponent.piece, entity);
            pieceComponent.toRemoveAnchors = false;
            pieceComponent.toRecreateFixture = true;
        }
        if(pieceComponent.toRecreateFixture) {
            detachPieceRecreateFixture(bodyFactory, pieceComponent, entity);
            pieceComponent.toRecreateFixture = false;
        }
    }

    private void destroyPiece(PieceComponent pieceComponent, Entity entity) {
        detachPieceMultiple(pieceComponent.piece, entity);

        pieceComponent.fixture.getBody().destroyFixture(pieceComponent.fixture);

        if(shipMapper.has(entity)) shipMapper.get(entity).piecesArray.removeValue(pieceComponent.piece, true);

        engine.removeEntity(entity);
    }

    private void detachPieceSingle(Piece piece, Entity entity) {
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

        if(shipMapper.has(entity)) shipMapper.get(entity).piecesArray.removeValue(piece, true);

    }

    private void detachPieceMultiple(Piece piece, Entity entity) {
        connectedPiecesToDetach.clear();
        for(Anchor anchor : piece.anchors) {
            if(anchor.piece == null) continue;
            connectedPiecesToDetach.add(anchor.piece);
        }
        piece.actorId = -1;
        detachPieceSingle(piece, entity);
        piece.pieceComponent.toRecreateFixture = true;

        for(int i = 0; i < connectedPiecesToDetach.size; i++) {
            boolean found = detachPieceSearchForCore(connectedPiecesToDetach.get(i), 1, 2, 3);

            if(found) {
                connectedPiecesToDetach.removeIndex(i);
                i--;
            }
        }

        boolean isShip = shipMapper.has(entity);
        for(int i = 0; i < connectedPiecesToDetach.size; i++) {
            detachPieceSearchForCoreCallbackFunc(
                    connectedPiecesToDetach.get(i),
                    new int[]{1, 2, 3},
                    4,
                    pieceComponent -> pieceComponent.toRemoveAnchors = true
            );
        }

        if(isShip) {
            Array<Piece> piecesArray = shipMapper.get(entity).piecesArray;
            for(Piece p : piecesArray) p.checked = 0;
        }
    }

    private static boolean detachPieceSearchForCore(Piece piece, int checkValue, int checkedFoundValue, int checkedNotFoundValue) {
        if(piece instanceof CorePiece) {
            piece.checked = checkedFoundValue;
            return true;
        }
        piece.checked = checkValue;
        for(Anchor anchor : piece.anchors) {
            if(anchor.piece == null) continue;
            if(anchor.piece.checked == checkedFoundValue) return true;
            if(anchor.piece.checked == checkedNotFoundValue) continue;
            if(anchor.piece.checked == checkValue) continue;
            boolean value = detachPieceSearchForCore(anchor.piece, checkValue, checkedFoundValue, checkedNotFoundValue);
            if(value) {
                piece.checked = checkedFoundValue;
                return true;
            }
        }
        piece.checked = checkedNotFoundValue;
        return false;
    }

    private interface DetachPieceSearchForCoreCallback {
        void func(PieceComponent pieceComponent);
    }

    private void detachPieceSearchForCoreCallbackFunc(Piece piece, int[] checkValue, int newCheckValue, DetachPieceSearchForCoreCallback callback) {
        callback.func(piece.pieceComponent);
        piece.checked = newCheckValue;
        for(Anchor anchor : piece.anchors) {
            if(anchor.piece == null || anchor.piece.checked == newCheckValue) continue;
            if(Arrays.stream(checkValue).noneMatch(value -> value == anchor.piece.checked)) continue;
            detachPieceSearchForCoreCallbackFunc(anchor.piece, checkValue, newCheckValue, callback);
        }
    }

    private void detachPieceRecreateFixture(BodyFactory bodyFactory, PieceComponent pieceComponent, Entity entity) {
        pieceComponent.piece.pos.x = 0;
        pieceComponent.piece.pos.y = 0;

        Body body = bodyFactory.createPieceBody(pieceComponent.fixtureCenter.x, pieceComponent.fixtureCenter.y, pieceComponent.fixture.getBody().getAngle());
        Fixture fixture = bodyFactory.createPieceFixture(body, pieceComponent.piece, entity);

        pieceComponent.fixture.getBody().destroyFixture(pieceComponent.fixture);
        pieceComponent.fixture = fixture;
        pieceComponent.fixture.getFilterData().maskBits = Info.MASK_NOTHING;
        pieceComponent.piece.actorId = -1;

        if(shipMapper.has(entity)) shipMapper.get(entity).piecesArray.removeValue(pieceComponent.piece, true);

        entity.remove(NPCComponent.class);
        entity.remove(PlayerComponent.class);
        entity.remove(ShipComponent.class);
    }

}
