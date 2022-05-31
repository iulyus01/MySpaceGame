package com.myspacegame.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.myspacegame.Info;
import com.myspacegame.components.TextureComponent;

public class RenderingBeginSystem extends IteratingSystem {

    private final SpriteBatch batch;
    private final OrthographicCamera camera;

    public RenderingBeginSystem(SpriteBatch batch) {
        super(Family.one(TextureComponent.class).get());
        this.batch = batch;

        camera = new OrthographicCamera(Info.W * Info.PIXELS_TO_METRES, Info.H * Info.PIXELS_TO_METRES);
        camera.position.set(0, 0, 0);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        batch.setProjectionMatrix(camera.combined);
        batch.enableBlending();
        batch.begin();
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {

    }

    public OrthographicCamera getCamera() {
        return camera;
    }

}