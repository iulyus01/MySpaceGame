package com.myspacegame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.factories.SystemManager;

public class PlayScreenHUD {

    private final MainClass game;
    private final PlayScreen playScreen;
    private final SystemManager systemManager;

    private final Stage stage;
    private final InputMultiplexer multiplexer;
    private final ShapeRenderer shapeRenderer;

    private Label labelMouseCoord;
    private Label labelFPS;
    private Button menuButton;

    private boolean isMenuShown = true;

    public PlayScreenHUD(MainClass game, PlayScreen playScreen, SystemManager systemManager, ShapeRenderer shapeRenderer) {
        this.game = game;
        this.shapeRenderer = shapeRenderer;
        this.playScreen = playScreen;
        this.systemManager = systemManager;

        Viewport viewport = new ExtendViewport(Info.W, Info.H);

        stage = new Stage(viewport, systemManager.getBatch());

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        createUI();

    }

    public void update(float delta) {
        String mouseCoordText = String.format("%.2f%n", Info.mouseWorldX) + " " + String.format("%.2f%n", Info.mouseWorldY);
        labelMouseCoord.setText(mouseCoordText);

        labelFPS.setText(String.valueOf(Gdx.graphics.getFramesPerSecond()));

        if(systemManager.getKeyboardController().escPressed) {
            if(!isMenuShown) {
                createMenuTable(true);
            }
            systemManager.getKeyboardController().escPressed = false;
        }

        stage.act(delta);
    }

    public void draw() {
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        shapeRenderer.end();

        stage.draw();

    }

    private void createUI() {
        Skin skin = game.assetManager.get("ui/FlatSkin.json", Skin.class);

        labelMouseCoord = new Label("1", skin);
        labelMouseCoord.setAlignment(Align.bottomRight);
        labelMouseCoord.setPosition(60, Info.H - 80);

        labelFPS = new Label("1", skin);
        labelFPS.setAlignment(Align.bottomLeft);
        labelFPS.setPosition(Info.W - 30, Info.H - 10 - labelFPS.getHeight());

        menuButton = new TextButton("menu", game.uiStyles.getTextButtonStyle());
        menuButton.setPosition(Info.W / 2f - menuButton.getWidth() / 2, Info.H / 6f);
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("menu button clicked");
                super.clicked(event, x, y);
                createMenuTable(false);
                systemManager.resetGame();
                toggleMenuButton(true);
            }
        });
        toggleMenuButton(true);

        stage.addActor(labelMouseCoord);
        stage.addActor(labelFPS);
        stage.addActor(menuButton);

        createMenuTable(false);
    }

    private void createMenuTable(boolean resetPlay) {
        Table menuTable = new Table();

        Label titleLabel = new Label(Info.appName, game.uiStyles.getTitleLabelStyle());

        TextButton playButton = new TextButton(resetPlay ? "reset" : "play", game.uiStyles.getTextButtonStyle());
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if(resetPlay) {
                    systemManager.resetGame();
                    isMenuShown = true;
                    menuTable.clear();
                    createMenuTable(false);
                } else {
                    Info.speedrun = false;
                    systemManager.startGame();
                    menuTable.clear();
                    isMenuShown = false;
                }
            }
        });
        TextButton playSpeedrunButton = new TextButton("play speedrun", game.uiStyles.getTextButtonStyle());
        playSpeedrunButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                Info.speedrun = true;
                systemManager.startGame();
                menuTable.clear();
                isMenuShown = false;
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


        menuTable.setFillParent(true);
        menuTable.add(titleLabel).padBottom(180).row();
        menuTable.add(playButton).padBottom(20).row();
        if(isMenuShown) menuTable.add(playSpeedrunButton).padBottom(20).row();
        menuTable.add(exitButton).padBottom(200);

        isMenuShown = true;
        stage.addActor(menuTable);
    }

    public void toggleMenuButton(boolean disabled) {
        menuButton.setDisabled(disabled);
        menuButton.setColor(menuButton.getColor().r, menuButton.getColor().g, menuButton.getColor().b, disabled ? 0 : 1);
        isMenuShown = !disabled || isMenuShown;
    }

    public Stage getStage() {
        return stage;
    }

    public InputMultiplexer getMultiplexer() {
        return multiplexer;
    }
}
