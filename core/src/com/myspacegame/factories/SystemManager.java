package com.myspacegame.factories;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.myspacegame.Info;
import com.myspacegame.KeyboardController;
import com.myspacegame.MainClass;
import com.myspacegame.systems.*;

public class SystemManager {


    private static SystemManager instance = null;
    private PooledEngine engine;
    private final KeyboardController keyboardController;

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;

    private final RenderingBeginSystem renderingBeginSystem;
    private AnchorSystem anchorSystem;
    private BuildingSystem buildingSystem;

    private final Vector3 mouseCoords;

    private SystemManager() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        renderingBeginSystem = new RenderingBeginSystem(batch, shapeRenderer);
        camera = renderingBeginSystem.getCamera();
        keyboardController = new KeyboardController(camera);

        mouseCoords = new Vector3();
    }

    public static SystemManager getInstance() {
        if(instance == null) instance = new SystemManager();
        return instance;
    }

    public void init(MainClass game, PooledEngine engine, WorldFactory worldFactory) {
        this.engine = engine;
        this.anchorSystem = new AnchorSystem();
        this.buildingSystem = new BuildingSystem(keyboardController, game, engine);

        // physics system has to be before others, that's where the fixture center is computed
        engine.addSystem(new PhysicsSystem(worldFactory.getWorld()));
        // piece system sets transform position, so it needs to be after physics
        engine.addSystem(new PieceSystem(game, engine));
        // rendering
        engine.addSystem(renderingBeginSystem);
//        engine.addSystem(new RenderingBackgroundSystem(batch, shapeRenderer));
        engine.addSystem(new RenderingSystem(batch));
        engine.addSystem(new RenderingRotatingSystem(batch));
        engine.addSystem(new RenderingEndSystem(batch, shapeRenderer));


        engine.addSystem(new DraggingSystem(worldFactory.getWorld()));
        engine.addSystem(new PlayerControlSystem(keyboardController, game, engine, camera));
        engine.addSystem(new BulletSystem(game, engine));
        engine.addSystem(new RockSystem());
        engine.addSystem(new BackgroundSystem(camera));
        engine.addSystem(new CollisionSystem());

        engine.addSystem(new EnemyGenerationSystem(game, engine));


//        engine.addSystem(new PhysicsDebugSystem(worldFactory.getWorld(), camera));

    }

    public void toggleBuilding(boolean active) {
        if(active) {
            engine.addSystem(this.buildingSystem);
            engine.addSystem(this.anchorSystem);
        } else {
            engine.removeSystem(this.anchorSystem);
            engine.removeSystem(this.buildingSystem);
        }
    }

    public void update(float delta) {
        mouseCoords.x = Gdx.input.getX();
        mouseCoords.y = Gdx.input.getY();
        camera.unproject(mouseCoords);
        Info.mouseWorldX = mouseCoords.x;
        Info.mouseWorldY = mouseCoords.y;
        Info.cameraWorldX = camera.position.x;
        Info.cameraWorldY = camera.position.y;
        shapeRenderer.setProjectionMatrix(camera.combined);

        engine.update(delta);
    }

    public KeyboardController getKeyboardController() {
        return keyboardController;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
}
