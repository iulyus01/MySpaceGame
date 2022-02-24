package com.myspacegame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.myspacegame.Info;
import com.myspacegame.MainClass;

public class PlayScreenHUD {

    private final MainClass app;

    private final Stage stage;
    private final ShapeRenderer shapeRenderer;

    private String mouseCoordText = "test";
    private Label labelMouseCoord;
    private Label labelFPS;

    public PlayScreenHUD(MainClass app, SpriteBatch batch, ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        this.app = app;
        this.shapeRenderer = shapeRenderer;

        Viewport viewport = new ExtendViewport(Info.W, Info.H);

        stage = new Stage(viewport, batch);

        Gdx.input.setInputProcessor(stage);

        createUI();

    }

    public void update(float delta) {

        mouseCoordText = String.format("%.2f%n", Info.mouseWorldX) + " " + String.format("%.2f%n", Info.mouseWorldY);
        labelMouseCoord.setText(mouseCoordText);
        labelMouseCoord.setX(Gdx.input.getX() - 10);
        labelMouseCoord.setY(Info.H - Gdx.input.getY() + 10);

        labelFPS.setText(String.valueOf(Gdx.graphics.getFramesPerSecond()));
        labelFPS.setPosition(10, Info.H - 10 - labelFPS.getHeight());

        stage.act(delta);
    }

    public void draw() {
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        shapeRenderer.end();

        stage.draw();

    }

    public void dispose() {
        stage.dispose();
        shapeRenderer.dispose();
    }

    private void createUI() {
        Skin skin = app.assetManager.get("ui/FlatSkin.json", Skin.class);
        Skin skinReversed = app.assetManager.get("ui/FlatSkinReversed.json", Skin.class);

//        Table table = new Table();
//        table.setFillParent(true);
//        table.left().top();

//        table.add(mouseCoordLabel);

//        table.add(createLeftMenu(skin, skinReversed)).top().width(menuSideSize);
//        table.add(createTopMenu(skin, skinReversed)).top().growX();
//        table.add(createRightMenu(skin, skinReversed)).top().width(menuSideSize);

//        stage.addActor(table);
        labelMouseCoord = new Label("1", skin);
        labelMouseCoord.setAlignment(Align.bottomRight);

        labelFPS = new Label("1", skin);
        labelFPS.setAlignment(Align.bottomLeft);
        labelFPS.setPosition(10, Info.H - 10 - labelFPS.getHeight());

        stage.addActor(labelMouseCoord);
        stage.addActor(labelFPS);

    }
}
