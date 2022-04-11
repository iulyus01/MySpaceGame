package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.myspacegame.components.CropComponent;
import com.myspacegame.components.TextureComponent;
import com.myspacegame.components.TransformComponent;

public class RenderingSystem extends SortedIteratingSystem {

    private final SpriteBatch batch;

    private final ComponentMapper<TextureComponent> textureMapper;
    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<CropComponent> cropMapper;
    private final ShapeRenderer shapes;

    public RenderingSystem(SpriteBatch batch, ShapeRenderer shapes) {
        super(Family.all(TextureComponent.class, TransformComponent.class).get(), new ZComparator());
        this.batch = batch;
        this.shapes = shapes;

        textureMapper = ComponentMapper.getFor(TextureComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        cropMapper = ComponentMapper.getFor(CropComponent.class);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        TextureComponent texture = textureMapper.get(entity);
        TransformComponent transform = transformMapper.get(entity);

        if(texture.textureRegion == null || transform.isHidden) return;

        float width = transform.width;
        float height = transform.height;

        float originX = width / 2f;
        float originY = height / 2f;


        if(cropMapper.has(entity)) {
            CropComponent crop = entity.getComponent(CropComponent.class);
            width = crop.region.getRegionWidth();
            height = crop.region.getRegionHeight();
            originX = width / 2f;
            originY = height / 2f;
            batch.draw(crop.region,
                    transform.position.x - originX, transform.position.y - originY,
                    originX, originY,
                    width, height,
                    transform.scale.x, transform.scale.y,
//                        1, 1,
                    0
            );
        } else {
            batch.draw(texture.textureRegion,
                    transform.position.x - originX, transform.position.y - originY,
                    originX, originY,
                    width, height,
                    transform.scale.x, transform.scale.y,
                    (transform.angleRad + transform.angleOrientationRad) * MathUtils.radDeg
            );
        }

    }

}