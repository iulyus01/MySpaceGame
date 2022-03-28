package com.myspacegame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.ui.UIStyles;
import com.myspacegame.utils.PieceConfig;
import com.myspacegame.utils.PieceEdge;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoadingScreen implements Screen {

    private final MainClass game;

    private final ShapeRenderer shapeRenderer;

    private float dotDelay;
    private final float dotDelayMax = .2f;
    private float dotNr = 0;
    private final float dotNrMax = 5;
    private float loadingDelay;
    private float loadingDelayMax = 1000;

    public LoadingScreen(MainClass game) {
        this.game = game;

        shapeRenderer = new ShapeRenderer();

        queueAssets();

        loadConfigs();

    }

    @Override
    public void show() {
        System.out.println("loading screen show");
    }

    private void update(float delta) {
//        if(app.assetManager.update() && loadingDelay > loadingDelayMax) {
        if(game.assetManager.update()) {
            game.uiStyles = new UIStyles(game);
//            game.setScreen(new MainMenuScreen(game));
            game.setScreen(new PlayScreen(game));
        }

        dotDelay += delta;
        loadingDelay += delta * 1000;
        if(dotDelay >= dotDelayMax) {
            dotDelay = 0;
            dotNr++;
            dotNr %= (dotNrMax + 1);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        update(delta);


        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for(int i = 0; i < dotNr; i++) {
            shapeRenderer.rect(Info.W / 2f - 65 + i * 30, Info.H / 2f, 10, 10);
        }
        shapeRenderer.rectLine(Info.W / 2f - 65, Info.H / 2f - 30, Info.W / 2f - 65 + 130, Info.H / 2f - 30, 1);
        shapeRenderer.rectLine(Info.W / 2f - 65, Info.H / 2f - 20, Info.W / 2f - 65 + 130, Info.H / 2f - 20, 1);
        shapeRenderer.rectLine(Info.W / 2f - 65, Info.H / 2f - 30, Info.W / 2f - 65, Info.H / 2f - 20, 1);
        shapeRenderer.rectLine(Info.W / 2f - 65 + 130, Info.H / 2f - 30, Info.W / 2f - 65 + 130, Info.H / 2f - 20, 1);
        shapeRenderer.rect(Info.W / 2f - 65, Info.H / 2f - 30, game.assetManager.getProgress() * 130f, 10);

        shapeRenderer.end();
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
        shapeRenderer.dispose();
    }

    private void queueAssets() {
        game.assetManager.load("ui/FlatSkin.json", Skin.class);
        game.assetManager.load("ui/FlatSkinReversed.json", Skin.class);

        game.assetManager.load("badlogic.jpg", Texture.class);
        game.assetManager.load("images/background/background.png", Texture.class);
        game.assetManager.load("images/background/BackgroundDot1.png", Texture.class);
        game.assetManager.load("images/background/BackgroundDot12.png", Texture.class);
        game.assetManager.load("images/background/BackgroundDot2.png", Texture.class);
        game.assetManager.load("images/background/BackgroundDot3.png", Texture.class);
        game.assetManager.load("images/background/BackgroundDot4.png", Texture.class);
        game.assetManager.load("images/pieces/core.png", Texture.class);
        game.assetManager.load("images/pieces/hull.png", Texture.class);
        game.assetManager.load("images/pieces/thruster.png", Texture.class);
        game.assetManager.load("images/pieces/gun.png", Texture.class);
        game.assetManager.load("images/bullet.png", Texture.class);
        game.assetManager.load("images/hover.png", Texture.class);
        game.assetManager.load("images/hover.png", Texture.class);
        game.assetManager.load("images/anchor.png", Texture.class);

//        game.assetManager.load("ui/GoUpButton.png", Texture.class);
//        game.assetManager.load("ui/GoUpButtonOver.png", Texture.class);
//        game.assetManager.load("ui/GoUpButtonDown.png", Texture.class);
//        game.assetManager.load("ui/GoDownButton.png", Texture.class);
//        game.assetManager.load("ui/GoDownButtonOver.png", Texture.class);
//        game.assetManager.load("ui/GoDownButtonDown.png", Texture.class);


        FileHandleResolver resolver = new InternalFileHandleResolver();
        game.assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        game.assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        // load to fonts via the generator (implicitely done by the FreetypeFontLoader).
        // Note: you MUST specify a FreetypeFontGenerator defining the ttf font file name and the size
        // of the font to be generated. The names of the fonts are arbitrary and are not pointing
        // to a file on disk (but must end with the font's file format '.ttf')!
        FreetypeFontLoader.FreeTypeFontLoaderParameter size1Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size1Params.fontFileName = "fonts/DoppioOne.ttf";
        size1Params.fontParameters.size = 22;
        game.assetManager.load("DoppioOneText.ttf", BitmapFont.class, size1Params);

        FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size2Params.fontFileName = "fonts/DoppioOne.ttf";
        size2Params.fontParameters.size = 30;
        game.assetManager.load("DoppioOne.ttf", BitmapFont.class, size2Params);

        FreetypeFontLoader.FreeTypeFontLoaderParameter size3Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size3Params.fontFileName = "fonts/DoppioOne.ttf";
        size3Params.fontParameters.size = 60;
        game.assetManager.load("DoppioOneTitle.ttf", BitmapFont.class, size3Params);

        // we also load a "normal" font generated via Hiero
//        app.assetManager.load("data/default.fnt", BitmapFont.class);
    }

    private void loadConfigs() {
        JSONParser jsonParser = new JSONParser();

        try {
            FileHandle handle = Gdx.files.local("configs/pieces1.json");
            Object obj = jsonParser.parse(handle.readString());

            JSONArray pieceListJsonArray = (JSONArray) obj;

            Info.pieceConfigsMap = new HashMap<>();
//            for(Object o : pieceListJsonArray) parsePieceConfigsObject((JSONObject) o);
            for(Object o : pieceListJsonArray) parsePieceConfigsObjectNew((JSONObject) o);

        } catch(ParseException e) {
            e.printStackTrace();
        }
    }

    private static void parsePieceConfigsObject(JSONObject piece) {
        int id = (int) (long) piece.get("id");
        String name = (String) piece.get("name");
        int width = (int) (long) piece.get("width");
        int height = (int) (long) piece.get("height");
        String textureName = (String) piece.get("texture");

        List<Float> vertices = new ArrayList<>();
        JSONArray verticesJArray = (JSONArray) piece.get("vertices");
        for(Object v : verticesJArray) vertices.add((float) (double) v);

        List<Integer> startIndices = new ArrayList<>();
        JSONArray startIndicesJArray = (JSONArray) piece.get("startIndices");
        for(Object v : startIndicesJArray) startIndices.add((int) (long) v);

        List<Integer> endIndices = new ArrayList<>();
        JSONArray endIndicesJArray = (JSONArray) piece.get("endIndices");
        for(Object v : endIndicesJArray) endIndices.add((int) (long) v);

        List<Float> posRatioList = new ArrayList<>();
        JSONArray posRatioListJArray = (JSONArray) piece.get("posRatio");
        for(Object v : posRatioListJArray) posRatioList.add((float) (double) v);

        PieceConfig pieceConfig = new PieceConfig();
        pieceConfig.id = id;
        pieceConfig.name = name;
        pieceConfig.width = width;
        pieceConfig.height = height;
        pieceConfig.textureName = textureName;
//        pieceConfig.vertices = new float[vertices.size()];
//        for(int i = 0; i < vertices.size(); i++) pieceConfig.vertices[i] = vertices.get(i);
//        pieceConfig.startIndices = new int[startIndices.size()];
//        for(int i = 0; i < startIndices.size(); i++) pieceConfig.startIndices[i] = startIndices.get(i);
//        pieceConfig.endIndices = new int[endIndices.size()];
//        for(int i = 0; i < endIndices.size(); i++) pieceConfig.endIndices[i] = endIndices.get(i);
//        pieceConfig.posRatioList = new float[posRatioList.size()];
//        for(int i = 0; i < posRatioList.size(); i++) pieceConfig.posRatioList[i] = posRatioList.get(i);

        Info.pieceConfigsMap.put(id, pieceConfig);
    }

    private static void parsePieceConfigsObjectNew(JSONObject piece) {
        int id = (int) (long) piece.get("id");
        String name = (String) piece.get("name");
        int width = (int) (long) piece.get("width");
        int height = (int) (long) piece.get("height");
        String textureName = (String) piece.get("texture");

        List<PieceEdge> edges = new ArrayList<>();
        JSONArray edgesJSONArray = (JSONArray) piece.get("edges");
        for(Object edge : edgesJSONArray) {
            PieceEdge pieceEdge = new PieceEdge();
            pieceEdge.x1 = (float) (double) ((JSONObject) edge).get("x1");
            pieceEdge.y1 = (float) (double) ((JSONObject) edge).get("y1");
            pieceEdge.x2 = (float) (double) ((JSONObject) edge).get("x2");
            pieceEdge.y2 = (float) (double) ((JSONObject) edge).get("y2");

            JSONArray anchorsJSONArray = (JSONArray) ((JSONObject) edge).get("anchors");
            pieceEdge.anchorRatios = new Array<>(true, anchorsJSONArray.size(), Float.class);
            for(Object o : anchorsJSONArray) pieceEdge.anchorRatios.add((float) (double) o);

            edges.add(pieceEdge);
        }


        PieceConfig pieceConfig = new PieceConfig();
        pieceConfig.id = id;
        pieceConfig.name = name;
        pieceConfig.width = width;
        pieceConfig.height = height;
        pieceConfig.textureName = textureName;
        pieceConfig.edges = edges;

        Info.pieceConfigsMap.put(id, pieceConfig);
    }
}
