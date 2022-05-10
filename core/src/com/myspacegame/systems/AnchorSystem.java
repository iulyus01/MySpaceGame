package com.myspacegame.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Transform;
import com.myspacegame.Info;
import com.myspacegame.components.*;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.entities.Anchor;
import com.myspacegame.entities.Piece;
import com.myspacegame.utils.PieceEdge;

public class AnchorSystem extends IteratingSystem {

    private final ComponentMapper<AnchorComponent> anchorMapper;
    private final ComponentMapper<TransformComponent> transformMapper;

    public AnchorSystem() {
        super(Family.one(AnchorComponent.class).get());

        anchorMapper = ComponentMapper.getFor(AnchorComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        Family family = Family.one(AnchorComponent.class).get();
        for(Entity entity : engine.getEntitiesFor(family)) {
            TransformComponent transformComponent = transformMapper.get(entity);
            transformComponent.isHidden = true;
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        AnchorComponent anchorComponent = anchorMapper.get(entity);
        PieceComponent pieceComponent = anchorComponent.piece.pieceComponent;
        TransformComponent transformComponent = transformMapper.get(entity);

        if(anchorComponent.toRemove) {
            getEngine().removeEntity(entity);
            return;
        }

        if(anchorComponent.piece.actorId > Info.StaticActorIds.PLAYER.getValue()) {
            anchorComponent.active = false;
        } else {
            anchorComponent.active = anchorComponent.anchor.piece == null;
        }

        // don't show the anchors if mouse is too far (20 pieces far)
        if(Math.abs(pieceComponent.fixtureCenter.x - Info.mouseWorldX) > Info.blockSize * 20 || Math.abs(pieceComponent.fixtureCenter.y - Info.mouseWorldY) > Info.blockSize * 20) {
            transformComponent.isHidden = true;
            return;
        }

        transformComponent.isHidden = !anchorComponent.active;

        Piece piece = pieceComponent.piece;
        Anchor anchor = anchorComponent.anchor;

        if(anchor.piece != null) return;
        Transform trf = pieceComponent.fixture.getBody().getTransform();

        // get anchor pos relative to piece
        PieceEdge edge = piece.edges.get(anchor.edgeIndex);
        try {
            // TODO this is temporary for some random crash
            anchor.pos.x = edge.x1 + edge.anchorRatios.get(anchor.edgeAnchorIndex) * (edge.x2 - edge.x1);
            anchor.pos.y = edge.y1 + edge.anchorRatios.get(anchor.edgeAnchorIndex) * (edge.y2 - edge.y1);
        } catch(IndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("piece: " + Info.getPieceName(piece) + " actorId: " + piece.actorId + " " + piece.pos.x + " " + piece.pos.y);
            System.out.println(edge);
            System.out.println(edge.anchorRatios);
            System.out.println(anchor.edgeAnchorIndex);
            Gdx.app.exit();
        }

        // rotate anchor by piece rotation
//        if(piece instanceof ThrusterPiece) {
        anchor.pos.rotateRad(piece.rotation * Info.rad90Deg);
//        }

        // change anchor pos relative to ship
        anchor.pos.x += piece.pos.x * Info.blockSize;
        anchor.pos.y += piece.pos.y * Info.blockSize;
        trf.mul(anchor.pos);


        transformComponent.position.x = anchor.pos.x;
        transformComponent.position.y = anchor.pos.y;

    }


}