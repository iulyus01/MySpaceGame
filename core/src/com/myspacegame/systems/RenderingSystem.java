package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.myspacegame.components.TextureComponent;
import com.myspacegame.components.TransformComponent;

public class RenderingSystem extends SortedIteratingSystem {

    private final SpriteBatch batch;

    private final ComponentMapper<TextureComponent> textureMapper;
    private final ComponentMapper<TransformComponent> transformMapper;

    private float previousZ = -2;

    public RenderingSystem(SpriteBatch batch) {
        super(Family.all(TextureComponent.class, TransformComponent.class).get(), new ZComparator());
        this.batch = batch;

        textureMapper = ComponentMapper.getFor(TextureComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        previousZ = -2;
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        TextureComponent texture = textureMapper.get(entity);
        TransformComponent transform = transformMapper.get(entity);

        if(transform.position.z < previousZ) {
            System.out.println("################ NoOoOoOoOoOoOo ################");
            System.out.println("");
            previousZ = transform.position.z;
        }

        if(texture.textureRegion == null || transform.isHidden) return;

        float width = texture.textureRegion.getRegionWidth();
        float height = texture.textureRegion.getRegionHeight();

        float originX = width / 2f;
        float originY = height / 2f;

        batch.draw(texture.textureRegion,
                transform.position.x - originX, transform.position.y - originY,
                originX, originY,
                width, height,
                transform.scale.x, transform.scale.y,
                (transform.angleRad + transform.angleOrientationRad) * MathUtils.radDeg
        );

    }

}