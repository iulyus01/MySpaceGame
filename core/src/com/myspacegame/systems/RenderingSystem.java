package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.CropComponent;
import com.myspacegame.components.TextureRotatingComponent;
import com.myspacegame.components.TextureComponent;
import com.myspacegame.components.TransformComponent;

import java.util.*;

public class RenderingSystem extends SortedIteratingSystem {

    private final SpriteBatch batch;
    private final Array<Entity> entities;
    private final Comparator<Entity> comparator;
    private final OrthographicCamera camera;

    private final ComponentMapper<TextureComponent> textureMapper;
    private final ComponentMapper<TextureRotatingComponent> rotatingTextureMapper;
    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<CropComponent> cropMapper;
    private final ShapeRenderer shapes;

    public RenderingSystem(MainClass game, SpriteBatch batch, ShapeRenderer shapes) {
        super(Family.all(TextureComponent.class, TransformComponent.class).get(), new ZComparator());
        this.batch = batch;
        this.shapes = shapes;

        textureMapper = ComponentMapper.getFor(TextureComponent.class);
        rotatingTextureMapper = ComponentMapper.getFor(TextureRotatingComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        cropMapper = ComponentMapper.getFor(CropComponent.class);

        entities = new Array<>();
        comparator = new ZComparator();


        camera = new OrthographicCamera(Info.W * Info.PIXELS_TO_METRES, Info.H * Info.PIXELS_TO_METRES);
        camera.position.set(0, 0, 0);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        entities.sort(comparator);

//        drawBackgroundLines();

        batch.setProjectionMatrix(camera.combined);
        batch.enableBlending();
        batch.begin();

        for(Entity entity : entities) {
            TextureComponent texture = textureMapper.get(entity);
            TextureRotatingComponent rotatingTexture = rotatingTextureMapper.get(entity);
            TransformComponent transform = transformMapper.get(entity);

            if(texture.textureRegion == null || transform.isHidden) continue;

            float width = texture.textureRegion.getRegionWidth();
            float height = texture.textureRegion.getRegionHeight();

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


            if(texture.overlayTexture != null) {
                batch.draw(texture.overlayTexture,
                        transform.position.x - originX, transform.position.y - originY,
                        originX, originY,
                        width, height,
                        transform.scale.x, transform.scale.y,
                        (transform.angleRad + transform.angleOrientationRad) * MathUtils.radDeg
                );
            }
            if(rotatingTexture != null) {
                width = rotatingTexture.textureRegion.getRegionWidth();
                height = rotatingTexture.textureRegion.getRegionHeight();

                originX = rotatingTexture.origin.x;
                originY = rotatingTexture.origin.y;

                batch.draw(rotatingTexture.textureRegion,
                        transform.position.x - originX, transform.position.y - originY,
                        originX, originY,
                        width, height,
                        transform.scale.x, transform.scale.y,
                        rotatingTexture.angleRad * MathUtils.radDeg
                );
            }
        }

//        batch.end();
        entities.clear();

    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        entities.add(entity);
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

    public OrthographicCamera getCamera() {
        return camera;
    }
}