package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.CropComponent;
import com.myspacegame.components.TextureComponent;
import com.myspacegame.components.TransformComponent;
import com.myspacegame.components.background.BackgroundBigDotComponent;
import com.myspacegame.components.background.BackgroundComponent;
import com.myspacegame.components.background.BackgroundParticleComponent;
import com.myspacegame.components.background.BackgroundSmallDotComponent;
import com.myspacegame.factories.ShapeRenderingDebug;

import java.util.ArrayList;
import java.util.List;

public class BackgroundSystem extends IteratingSystem {

    private final ComponentMapper<BackgroundBigDotComponent> bigDotMapper;
    private final ComponentMapper<BackgroundSmallDotComponent> smallDotMapper;
    private final ComponentMapper<BackgroundParticleComponent> particleMapper;
    private final ComponentMapper<CropComponent> cropMapper;
    private final Engine engine;
    private final OrthographicCamera camera;

    public BackgroundSystem(Engine engine, OrthographicCamera camera) {
        super(Family.all(BackgroundComponent.class, TransformComponent.class).get());
        this.engine = engine;
        this.camera = camera;

        bigDotMapper = ComponentMapper.getFor(BackgroundBigDotComponent.class);
        smallDotMapper = ComponentMapper.getFor(BackgroundSmallDotComponent.class);
        particleMapper = ComponentMapper.getFor(BackgroundParticleComponent.class);
        cropMapper = ComponentMapper.getFor(CropComponent.class);

    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);


    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        TextureRegion texture = entity.getComponent(TextureComponent.class).textureRegion;
        if(transform == null) return;
        if(bigDotMapper.has(entity)) {
            // big dot
            processBigDot(entity, transform, texture);
        } else if(smallDotMapper.has(entity)) {
            // small dot
            processSmallDot(entity, transform, texture);
        }

    }

    private void processBigDot(Entity entity, TransformComponent transform, TextureRegion texture) {
        CropComponent crop = entity.getComponent(CropComponent.class);
        BackgroundBigDotComponent bigDotComponent = entity.getComponent(BackgroundBigDotComponent.class);

        transform.position.x = Info.cameraWorldX;
        transform.position.y = Info.cameraWorldY;

        // this should be > 1 if scaling is bigger than PPM
        float scaleDiff = transform.scale.x / Info.PIXELS_TO_METRES;
        // crop y should be reversed, dunno why, textureRegion's fault
//            crop.srcX = (int) ((Info.cameraWorldX - Info.W / 2 * Info.PIXELS_TO_METRES - (bigDotComponent.x - texture.getRegionWidth() / 2 * Info.PIXELS_TO_METRES)) * Info.PPM);
//            crop.srcY = (int) (((bigDotComponent.y + texture.getRegionHeight() / 2 * Info.PIXELS_TO_METRES) - (Info.cameraWorldY + Info.H / 2 * Info.PIXELS_TO_METRES)) * Info.PPM);
        crop.srcX = (int) ((Info.cameraWorldX - Info.W / 2 * transform.scale.x - (bigDotComponent.x - texture.getRegionWidth() / 2 * transform.scale.x)) / transform.scale.x) - 40;
        crop.srcY = (int) (((bigDotComponent.y + texture.getRegionHeight() / 2 * transform.scale.y) - (Info.cameraWorldY + Info.H / 2 * transform.scale.y)) / transform.scale.y) - 40;
        crop.srcWidth = Info.W + 80;
        crop.srcHeight = Info.H + 80;
//            crop.srcWidth = (int) (Info.W * transform.scale.x);
//            crop.srcHeight = (int) (Info.H * transform.scale.y);

        crop.srcX += (crop.srcWidth - crop.srcWidth / scaleDiff) / 2;
        crop.srcY += (crop.srcHeight - crop.srcHeight / scaleDiff) / 2;
        crop.srcWidth /= scaleDiff;
        crop.srcHeight /= scaleDiff;

        ShapeRenderingDebug.addToDrawThenRemove(() -> ShapeRenderingDebug.drawDebugLine(Info.cameraWorldX, Info.cameraWorldY, bigDotComponent.x, bigDotComponent.y));

        crop.region.setRegion(crop.srcX, crop.srcY, crop.srcWidth, crop.srcHeight);
    }

    private void processSmallDot(Entity entity, TransformComponent transform, TextureRegion texture) {
        boolean xInside = (Info.cameraWorldX - transform.position.x) < camera.viewportWidth / 1.5f;
        boolean yInside = (Info.cameraWorldY - transform.position.y) < camera.viewportHeight / 1.5f;
        transform.isHidden = !(xInside && yInside);
    }
}
