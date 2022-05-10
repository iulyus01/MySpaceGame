package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.myspacegame.Info;
import com.myspacegame.components.TextureComponent;
import com.myspacegame.components.TransformComponent;
import com.myspacegame.components.background.BackgroundBigDotComponent;
import com.myspacegame.components.background.BackgroundComponent;
import com.myspacegame.components.background.BackgroundSmallDotComponent;
import com.myspacegame.factories.ShapeRenderingDebug;

public class BackgroundSystem extends IteratingSystem {

    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<BackgroundComponent> backgroundMapper;
    private final ComponentMapper<TextureComponent> textureMapper;
    private final ComponentMapper<BackgroundBigDotComponent> bigDotMapper;
    private final ComponentMapper<BackgroundSmallDotComponent> smallDotMapper;
    private final OrthographicCamera camera;

    public BackgroundSystem(OrthographicCamera camera) {
        super(Family.all(BackgroundComponent.class, TransformComponent.class).get());
        this.camera = camera;

        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        backgroundMapper = ComponentMapper.getFor(BackgroundComponent.class);
        textureMapper = ComponentMapper.getFor(TextureComponent.class);
        bigDotMapper = ComponentMapper.getFor(BackgroundBigDotComponent.class);
        smallDotMapper = ComponentMapper.getFor(BackgroundSmallDotComponent.class);

    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);


    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transformComponent = transformMapper.get(entity);
        BackgroundComponent backgroundComponent = backgroundMapper.get(entity);
        TextureComponent textureComponent = textureMapper.get(entity);
        if(transformComponent == null) return;
        if(bigDotMapper.has(entity)) {
            // big dot
            processBigDot(entity, transformComponent, backgroundComponent, textureComponent);
        } else if(smallDotMapper.has(entity)) {
            // small dot
            processSmallDot(transformComponent);
        }

    }

    private void processBigDot(Entity entity, TransformComponent transformComponent, BackgroundComponent backgroundComponent, TextureComponent textureComponent) {
        BackgroundBigDotComponent bigDotComponent = entity.getComponent(BackgroundBigDotComponent.class);

        // crop y should be reversed, dunno why, textureRegion's fault
        ShapeRenderingDebug.addToDrawThenRemove(() -> ShapeRenderingDebug.drawDebugLine(Info.cameraWorldX, Info.cameraWorldY, bigDotComponent.x, bigDotComponent.y));

        transformComponent.position.x = Info.cameraWorldX;
        transformComponent.position.y = Info.cameraWorldY;
        float srcWidth = Info.W;
        float srcHeight = Info.H;
        float srcX = (transformComponent.position.x - bigDotComponent.x) * Info.PPM + srcWidth / 2;
        float srcY = (bigDotComponent.y - transformComponent.position.y) * Info.PPM + srcHeight / 2;
        if(srcX < 0) {
//            srcWidth += srcX;
            transformComponent.position.x = Info.cameraWorldX - srcX * Info.PIXELS_TO_METRES;
            srcX = 0;
        }
        if(srcY < 0) {
//            srcHeight += srcY;
            transformComponent.position.y = Info.cameraWorldY - srcY * Info.PIXELS_TO_METRES;
            srcY = 0;
        }


        textureComponent.textureRegion.setRegion((int) srcX, (int) srcY, (int) srcWidth, (int) srcHeight);
//        textureComponent.textureRegion.setRegion(500, 500, 500, 500);
        System.out.println("crop: " + srcX + " " + srcY + " " + srcWidth + " " + srcHeight);
//        System.out.println("camera pos: " + Info.cameraWorldX + " " + Info.cameraWorldY);
    }

    private void processSmallDot(TransformComponent transform) {
        boolean xInside = Math.abs(Info.cameraWorldX - transform.position.x) < camera.viewportWidth;
        boolean yInside = Math.abs(Info.cameraWorldY - transform.position.y) < camera.viewportHeight;
        transform.isHidden = !(xInside && yInside);
    }
}
