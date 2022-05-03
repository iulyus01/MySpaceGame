package com.myspacegame.screens;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.factories.ShapeRenderingDebug;
import com.myspacegame.factories.SystemManager;
import com.myspacegame.factories.WorldFactory;

public class PlayScreen implements Screen {

    private final PlayScreenHUD ui;
    private final SystemManager systemManager;

    public PlayScreen(MainClass game) {
        PooledEngine engine = new PooledEngine();
        WorldFactory worldFactory = WorldFactory.getInstance(game, engine);

        systemManager = SystemManager.getInstance();
        systemManager.init(game, engine, worldFactory);

        ui = new PlayScreenHUD(game, systemManager.getBatch(), new ShapeRenderer(), systemManager.getCamera());

        ShapeRenderingDebug.init(systemManager.getShapeRenderer());


//        engine.addSystem(new AnimationSystem());
//        engine.addSystem(new EnemySystem());


    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(systemManager.getKeyboardController());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(Info.colorClear.r, Info.colorClear.g, Info.colorClear.b, Info.colorClear.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));


        systemManager.update(delta);

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
