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


    private MainClass game;
    private static SystemManager instance = null;
    private PooledEngine engine;
    private final KeyboardController keyboardController;

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;
    private WorldFactory worldFactory;

    private final RenderingBeginSystem renderingBeginSystem;
    private AnchorSystem anchorSystem;
    private BuildingSystem buildingSystem;

    private final Vector3 mouseCoords;

    private SystemManager() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        renderingBeginSystem = new RenderingBeginSystem(batch);
        camera = renderingBeginSystem.getCamera();
        keyboardController = new KeyboardController(camera);

        mouseCoords = new Vector3();
    }

    public static SystemManager getInstance() {
        if(instance == null) instance = new SystemManager();
        return instance;
    }

    public void init(MainClass game, PooledEngine engine, WorldFactory worldFactory) {
        this.game = game;
        this.engine = engine;
        this.anchorSystem = new AnchorSystem();
        this.worldFactory = worldFactory;


        // physics system has to be before others, that's where the fixture center is computed
        engine.addSystem(new PhysicsSystem(worldFactory.getWorld()));
        // piece system sets transform position, so it needs to be after physics
        engine.addSystem(new PieceSystem(game, engine));
        // rendering
        engine.addSystem(renderingBeginSystem);
//        engine.addSystem(new RenderingBackgroundSystem(batch, shapeRenderer));
        engine.addSystem(new RenderingSystem(batch));
        engine.addSystem(new RenderingRotatingSystem(batch));
        engine.addSystem(new RenderingEndSystem(batch));
        engine.addSystem(new ShapesRenderingSystem(shapeRenderer));

        engine.addSystem(new DraggingSystem(worldFactory.getWorld()));
        engine.addSystem(new BulletSystem(game, engine));
        engine.addSystem(new RockSystem());
        engine.addSystem(new BackgroundSystem(camera));
        engine.addSystem(new CollisionSystem());



//        engine.addSystem(new PhysicsDebugSystem(worldFactory.getWorld(), camera));

    }

    public void resetGame() {
        worldFactory.destroyLevel();
        worldFactory.destroyPlayer();
        worldFactory.createBackgroundOnly();

        engine.removeSystem(engine.getSystem(BuildingSystem.class));
        engine.removeSystem(engine.getSystem(PlayerControlSystem.class));
        engine.removeSystem(engine.getSystem(TeleporterSystem.class));
        engine.removeSystem(engine.getSystem(AIControlSystem.class));
    }

    public void startGame() {
        worldFactory.startGame();
        this.buildingSystem = new BuildingSystem(keyboardController, game, engine);
        engine.addSystem(new PlayerControlSystem(keyboardController, game, engine, camera, worldFactory.getPlayerBody()));
        engine.addSystem(new TeleporterSystem(engine, worldFactory));
        engine.addSystem(new AIControlSystem(game, engine));
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

    public void dispose() {
        engine.removeAllEntities();
        engine.removeAllSystems();
        engine.clearPools();
        batch.dispose();
        shapeRenderer.dispose();
        worldFactory.dispose();
    }

    public KeyboardController getKeyboardController() {
        return keyboardController;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
}
