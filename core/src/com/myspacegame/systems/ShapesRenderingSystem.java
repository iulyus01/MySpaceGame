package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
import com.myspacegame.components.ShapeDrawingComponent;
import com.myspacegame.components.TransformComponent;

public class ShapesRenderingSystem extends SortedIteratingSystem {

    private final ShapeRenderer shapeRenderer;

    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<ShapeDrawingComponent> shapeDrawingMapper;

    private final Array<Entity> drawingList;

    public ShapesRenderingSystem(ShapeRenderer shapeRenderer) {
        super(Family.all(ShapeDrawingComponent.class, TransformComponent.class).get(), new ZComparator());
        this.shapeRenderer = shapeRenderer;

        shapeDrawingMapper = ComponentMapper.getFor(ShapeDrawingComponent.class);
        transformMapper = ComponentMapper.getFor(TransformComponent.class);

        drawingList = new Array<>(true, 16, Entity.class);

//        Gdx.gl.glLineWidth(2);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for(Entity entity : drawingList) {
            TransformComponent transformComponent = transformMapper.get(entity);
            ShapeDrawingComponent shapeDrawingComponent = shapeDrawingMapper.get(entity);

            if(transformComponent.isHidden) continue;

            shapeDrawingComponent.shape.draw(shapeDrawingComponent, transformComponent, shapeRenderer);
            shapeDrawingComponent.shape.update(shapeDrawingComponent, transformComponent, deltaTime);
        }
        shapeRenderer.end();

        drawingList.clear();
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        drawingList.add(entity);

    }

}