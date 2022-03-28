package com.myspacegame.screens;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.myspacegame.Info;
import com.myspacegame.KeyboardController;
import com.myspacegame.MainClass;
import com.myspacegame.factories.ShapeRenderingDebug;
import com.myspacegame.factories.WorldFactory;
import com.myspacegame.systems.*;

public class PlayScreen implements Screen {

    private final PooledEngine engine;
    private final KeyboardController keyboardController;
    private final OrthographicCamera camera;

    private final PlayScreenHUD ui;

    private final Vector3 mouseCoords;
    private final ShapeRenderer shapeRenderer;

    public PlayScreen(MainClass game) {
        engine = new PooledEngine();
        shapeRenderer = new ShapeRenderer();

        SpriteBatch batch = new SpriteBatch();
        RenderingBeginSystem renderingBeginSystem = new RenderingBeginSystem(batch, shapeRenderer);
        camera = renderingBeginSystem.getCamera();
        keyboardController = new KeyboardController(camera);

        ui = new PlayScreenHUD(game, batch, new ShapeRenderer(), camera);

        ShapeRenderingDebug.init(shapeRenderer);


        WorldFactory worldFactory = WorldFactory.getInstance(game, engine);

        // physics system has to be before others, that's where the fixture center is computed
        engine.addSystem(new PhysicsSystem(worldFactory.getWorld()));
        // piece system sets transform position, so it needs to be after physics
        engine.addSystem(new PieceSystem(game, engine));

        engine.addSystem(new DraggingSystem());
        engine.addSystem(new PlayerControlSystem(keyboardController, game, engine, camera));
        engine.addSystem(new BulletSystem(game, engine));
        engine.addSystem(new BackgroundSystem(worldFactory.getWorld(), engine));
        engine.addSystem(new CollisionSystem(game, engine));

        // rendering
        engine.addSystem(renderingBeginSystem);
        engine.addSystem(new RenderingSystem(batch, shapeRenderer));
        engine.addSystem(new RenderingRotatingSystem(batch));
        engine.addSystem(new RenderingBuildingSystem(game, batch));
        engine.addSystem(new RenderingEndSystem(batch, shapeRenderer));


//        engine.addSystem(new PhysicsDebugSystem(worldFactory.getWorld(), camera));




//        engine.addSystem(new AnimationSystem());
//        engine.addSystem(new EnemySystem());

        mouseCoords = new Vector3();


    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(keyboardController);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(Info.colorClear.r, Info.colorClear.g, Info.colorClear.b, Info.colorClear.a);
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

        mouseCoords.x = Gdx.input.getX();
        mouseCoords.y = Gdx.input.getY();
        camera.unproject(mouseCoords);
        Info.mouseWorldX = mouseCoords.x;
        Info.mouseWorldY = mouseCoords.y;

        shapeRenderer.setProjectionMatrix(camera.combined);
        engine.update(delta);
        Info.cameraWorldX = camera.position.x;
        Info.cameraWorldY = camera.position.y;

        ShapeRenderingDebug.draw();

        ui.update(delta);
        ui.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
