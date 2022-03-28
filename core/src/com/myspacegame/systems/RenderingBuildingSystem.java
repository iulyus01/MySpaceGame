package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Transform;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.DraggingComponent;
import com.myspacegame.components.PlayerComponent;
import com.myspacegame.components.TransformComponent;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.entities.Anchor;
import com.myspacegame.entities.Piece;
import com.myspacegame.entities.ThrusterPiece;
import com.myspacegame.utils.PieceEdge;

public class RenderingBuildingSystem extends IteratingSystem {

    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<TransformComponent> transformMapper;
    private final SpriteBatch batch;

    private final TextureRegion anchorTexture;
    private final float width;
    private final float height;
    private final float originX;
    private final float originY;

    public RenderingBuildingSystem(MainClass game, SpriteBatch batch) {
        super(Family.all(PieceComponent.class, TransformComponent.class).one(PlayerComponent.class, DraggingComponent.class).get());
        this.batch = batch;

        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);

        anchorTexture = new TextureRegion(game.assetManager.get("images/anchor.png", Texture.class));

        width = anchorTexture.getRegionWidth();
        height = anchorTexture.getRegionHeight();
        originX = width / 2f;
        originY = height / 2f;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        if(Info.activeMode != Info.PlayerMode.BUILDING) {
            return;
        }

        PieceComponent pieceComponent = pieceMapper.get(entity);
        TransformComponent transformComponent = transformMapper.get(entity);

        // don't show the anchors if mouse is too far (20 pieces far)
        if(Math.abs(pieceComponent.fixtureCenter.x - Info.mouseWorldX) > Info.blockSize * 20 || Math.abs(pieceComponent.fixtureCenter.y - Info.mouseWorldY) > Info.blockSize * 20) return;


        Piece piece = pieceComponent.piece;
        Anchor anchor;
        for(int i = 0; i < pieceComponent.piece.anchors.size; i++) {
            anchor = pieceComponent.piece.anchors.get(i);

            if(piece.anchors.get(i).piece != null) continue;
            Transform trf = pieceComponent.fixture.getBody().getTransform();

            // get anchor pos relative to piece
            PieceEdge edge = piece.edges.get(anchor.edgeIndex);
            anchor.pos.x = edge.x1 + edge.anchorRatios.get(anchor.edgeAnchorIndex) * (edge.x2 - edge.x1);
            anchor.pos.y = edge.y1 + edge.anchorRatios.get(anchor.edgeAnchorIndex) * (edge.y2 - edge.y1);

            // rotate anchor by piece rotation
            if(piece instanceof ThrusterPiece) {
                anchor.pos.rotateRad(piece.rotation * Info.rad90Deg);
            }

            // change anchor pos relative to ship
            anchor.pos.x += piece.pos.x * Info.blockSize;
            anchor.pos.y += piece.pos.y * Info.blockSize;
            trf.mul(anchor.pos);

            float scaleX = transformComponent.scale.x / piece.W * Info.blockSize;
            float scaleY = transformComponent.scale.y / piece.H * Info.blockSize;

            batch.draw(anchorTexture,
                    anchor.pos.x - originX, anchor.pos.y - originY,
                    originX, originY,
                    width, height,
                    scaleX, scaleY,
                    pieceComponent.fixture.getBody().getAngle() * MathUtils.radDeg
            );

        }

    }


}