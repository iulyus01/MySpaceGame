package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.myspacegame.components.TextureRotatingComponent;
import com.myspacegame.components.TransformComponent;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.entities.Piece;
import com.myspacegame.entities.WeaponPiece;

public class RenderingRotatingSystem extends SortedIteratingSystem {

    private final SpriteBatch batch;

    private final ComponentMapper<TextureRotatingComponent> rotatingTextureMapper;
    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<PieceComponent> pieceMapper;

    public RenderingRotatingSystem(SpriteBatch batch) {
        super(Family.all(TextureRotatingComponent.class, TransformComponent.class).get(), new ZComparator());
        this.batch = batch;

        rotatingTextureMapper = ComponentMapper.getFor(TextureRotatingComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        // TODO make this a separate entity.. i think
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        TextureRotatingComponent rotatingTexture = rotatingTextureMapper.get(entity);
        TransformComponent transform = transformMapper.get(entity);

        if(rotatingTexture.textureRegion == null || transform.isHidden) return;

        float width = rotatingTexture.textureRegion.getRegionWidth();
        float height = rotatingTexture.textureRegion.getRegionHeight();

        float originX = rotatingTexture.origin.x;
        float originY = rotatingTexture.origin.y;

        float rotation = getRotation(entity);

        batch.draw(rotatingTexture.textureRegion,
                transform.position.x - originX, transform.position.y - originY,
                originX, originY,
                width, height,
                transform.scale.x, transform.scale.y,
                rotation
        );
    }

    private float getRotation(Entity entity) {
        if(!pieceMapper.has(entity)) return 0;

        Piece piece = pieceMapper.get(entity).piece;
        if(piece instanceof WeaponPiece) {
            return ((WeaponPiece) piece).angleRad * MathUtils.radDeg;
        }
        return 0;
    }

}