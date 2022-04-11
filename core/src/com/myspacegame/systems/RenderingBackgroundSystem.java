package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.myspacegame.Info;
import com.myspacegame.components.CropComponent;
import com.myspacegame.components.TextureComponent;
import com.myspacegame.components.TransformComponent;

public class RenderingBackgroundSystem extends SortedIteratingSystem {

    private final SpriteBatch batch;

    private final ComponentMapper<TextureComponent> textureMapper;
    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<CropComponent> cropMapper;
    private final ShapeRenderer shapes;

    public RenderingBackgroundSystem(SpriteBatch batch, ShapeRenderer shapes) {
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
//        drawBackgroundLines();
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        TextureComponent texture = textureMapper.get(entity);
        TransformComponent transform = transformMapper.get(entity);

        if(texture.textureRegion == null || transform.isHidden) return;

        if(cropMapper.has(entity)) {
            drawCrop(entity, transform);
        } else {
            drawImage(texture, transform);
        }

    }

    private void drawCrop(Entity entity, TransformComponent transform) {
        CropComponent crop = entity.getComponent(CropComponent.class);
        float width = crop.region.getRegionWidth();
        float height = crop.region.getRegionHeight();
        float originX = width / 2f;
        float originY = height / 2f;
        batch.draw(crop.region,
                transform.position.x - originX, transform.position.y - originY,
                originX, originY,
                width, height,
                transform.scale.x, transform.scale.y,
//                        1, 1,
                0
        );
    }

    private void drawImage(TextureComponent texture, TransformComponent transform) {
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

    private void drawBackgroundLines() {
        shapes.begin(ShapeRenderer.ShapeType.Line);
        int space = 2;
        int left = -100;
        int right = 100;
        shapes.setColor(Info.colorRed.r, Info.colorRed.g, Info.colorRed.b, .01f);
        for(int i = left; i < right; i++) {
            shapes.line(left * space, i * space, right * space, i * space);
            shapes.line(i * space, left * space, i * space, right * space);
        }
        shapes.end();
    }

}