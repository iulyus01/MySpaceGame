package com.myspacegame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.myspacegame.Info;
import com.myspacegame.MainClass;

public class MainMenuScreen implements Screen {

    private final MainClass game;

//    private final DynamicBackground background;
    private final Stage stage;

    public MainMenuScreen(MainClass game) {
        this.game = game;

//        background = new DynamicBackground(14, .8f);
        stage = new Stage();

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {

        Label titleLabel = new Label(Info.appName, game.uiStyles.getTitleLabelStyle());

        TextButton playButton = new TextButton("play", game.uiStyles.getTextButtonStyle());
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                Info.speedrun = false;
                game.setScreen(new PlayScreen(game));
            }
        });
        TextButton playSpeedrunButton = new TextButton("play speedrun", game.uiStyles.getTextButtonStyle());
        playSpeedrunButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                Info.speedrun = true;
                game.setScreen(new PlayScreen(game));
            }
        });
        TextButton exitButton = new TextButton("exit", game.uiStyles.getTextButtonStyle());
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                Gdx.app.exit();
            }
        });


        Table table = new Table();
        table.setFillParent(true);
        table.add(titleLabel).padBottom(180).row();
        table.add(playButton).padBottom(20).row();
        table.add(playSpeedrunButton).padBottom(20).row();
        table.add(exitButton).padBottom(200);

        stage.addActor(table);

    }

    private void update(float delta) {
//        background.update(delta);
        stage.act(delta);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glEnable(GL30.GL_BLEND);
        ScreenUtils.clear(Info.colorClear);

        update(delta);

//        background.draw(shapeRenderer);
        stage.draw();
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
