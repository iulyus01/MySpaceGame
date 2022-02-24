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

        batch.end();
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


        for(int i = 0; i < pieceComponent.piece.anchors.size; i++) {
            Anchor a = pieceComponent.piece.anchors.get(i);
            Piece piece = pieceComponent.piece;
            if(pieceComponent.piece.anchors.get(i).piece != null) continue;
            Transform trf = pieceComponent.fixture.getBody().getTransform();

            a.pos.x = piece.shape.getVertices()[a.startVertex] + a.posRate * (piece.shape.getVertices()[a.endVertex] - piece.shape.getVertices()[a.startVertex]);
            a.pos.y = piece.shape.getVertices()[a.startVertex + 1] + a.posRate * (piece.shape.getVertices()[a.endVertex + 1] - piece.shape.getVertices()[a.startVertex + 1]);

            a.pos.x += piece.pos.x * Info.blockSize;
            a.pos.y += piece.pos.y * Info.blockSize;
            trf.mul(a.pos);

            float scaleX = transformComponent.scale.x / piece.W * Info.blockSize;
            float scaleY = transformComponent.scale.y / piece.H * Info.blockSize;

            batch.draw(anchorTexture,
                    a.pos.x - originX, a.pos.y - originY,
                    originX, originY,
                    width, height,
                    scaleX, scaleY,
                    pieceComponent.fixture.getBody().getAngle() * MathUtils.radDeg
            );

        }

    }


}