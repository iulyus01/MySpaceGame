package com.myspacegame.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.myspacegame.Info;
import com.myspacegame.components.TextureComponent;

public class RenderingEndSystem extends IteratingSystem {

    private final SpriteBatch batch;
    private final ShapeRenderer shapes;

    public RenderingEndSystem(SpriteBatch batch, ShapeRenderer shapes) {
        super(Family.one(TextureComponent.class).get());
        this.batch = batch;
        this.shapes = shapes;

    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        batch.end();
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {

    }

}