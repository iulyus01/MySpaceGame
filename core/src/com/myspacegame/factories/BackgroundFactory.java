package com.myspacegame.factories;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.CropComponent;
import com.myspacegame.components.TextureComponent;
import com.myspacegame.components.TransformComponent;
import com.myspacegame.components.background.BackgroundBigDotComponent;
import com.myspacegame.components.background.BackgroundComponent;
import com.myspacegame.components.background.BackgroundSmallDotComponent;

import java.util.ArrayList;
import java.util.List;

public class BackgroundFactory {

    private static BackgroundFactory instance = null;
    private final MainClass game;
    private final Engine engine;
    private final World world;
    private final BodyFactory bodyFactory;

    private final Array<Color> backgroundColors;
    //    private final List<TextureRegion> bigDotTextures;
    private final Array<TextureRegion> smallDotTextures;
    private final Array<TextureRegion> dotTextures;

    private BackgroundFactory(MainClass game, Engine engine, World world) {
        this.game = game;
        this.engine = engine;
        this.world = world;

        bodyFactory = BodyFactory.getInstance(world);

        backgroundColors = new Array<>(true, 3, Color.class);
        backgroundColors.add(new Color(0.086f, 0.169f, 0.149f, 1));
        backgroundColors.add(new Color(0.082f, 0.125f, 0.157f, 1));
        backgroundColors.add(new Color(0.071f, 0.082f, 0.129f, 1));
        backgroundColors.add(new Color(0.094f, 0.071f, 0.149f, 1));
        backgroundColors.add(new Color(0.149f, 0.055f, 0.125f, 1));
        backgroundColors.add(new Color(0.157f, 0.055f, 0.094f, 1));

//        bigDotTextures = new ArrayList<>();
//        bigDotTextures.add(new TextureRegion(game.assetManager.get("images/background/BackgroundDot1.png", Texture.class)));
//        bigDotTextures.add(new TextureRegion(game.assetManager.get("images/background/BackgroundDot12.png", Texture.class)));
//        bigDotTextures.add(new TextureRegion(game.assetManager.get("images/background/BackgroundDot2.png", Texture.class)));
//        bigDotTextures.add(new TextureRegion(game.assetManager.get("images/background/BackgroundDot3.png", Texture.class)));
//        bigDotTextures.add(new TextureRegion(game.assetManager.get("images/background/BackgroundDot4.png", Texture.class)));
        smallDotTextures = new Array<>(true, 3, TextureRegion.class);
        smallDotTextures.add(new TextureRegion(game.assetManager.get("images/background/SmallDot1.png", Texture.class)));
        smallDotTextures.add(new TextureRegion(game.assetManager.get("images/background/SmallDot2.png", Texture.class)));
        smallDotTextures.add(new TextureRegion(game.assetManager.get("images/background/SmallDot3.png", Texture.class)));
        dotTextures = new Array<>(true, 3, TextureRegion.class);
        dotTextures.add(new TextureRegion(game.assetManager.get("images/background/Dot1.png", Texture.class)));
        dotTextures.add(new TextureRegion(game.assetManager.get("images/background/Dot2.png", Texture.class)));
        dotTextures.add(new TextureRegion(game.assetManager.get("images/background/Dot3.png", Texture.class)));
    }

    public static BackgroundFactory getInstance(MainClass game, Engine engine, World world) {
        if(instance == null) instance = new BackgroundFactory(game, engine, world);
        return instance;
    }

    public void generate(int difficulty) {
        Info.colorClear = backgroundColors.get(difficulty);

        for(int i = 0; i < 80; i++) {
            int index = MathUtils.random(0, smallDotTextures.size - 1);
            float size = MathUtils.random(Info.blockSize / 10, Info.blockSize / 1.2f);
            createSmallDot(engine.createEntity(), index, size, size);
        }

//        createBigDot(engine.createEntity(), bigDotTextures.get(2));
    }

    public void destroy() {
        engine.removeAllEntities(Family.one(BackgroundComponent.class).get());
    }

//    private void createBigDot(Entity entity, TextureRegion texture) {
//        TextureComponent textureComponent = engine.createComponent(TextureComponent.class);
//        textureComponent.textureRegion = texture;
//        BackgroundComponent backgroundComponent = engine.createComponent(BackgroundComponent.class);
//        backgroundComponent.backgroundTexture = texture;
//
//        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
//        // transform x and y are irrelevant because they will change when cropping
//        transformComponent.width = Info.W;
//        transformComponent.height = Info.H;
//        transformComponent.position.z = -2;
//        float scale = Info.blockSize * 400 / textureComponent.textureRegion.getRegionWidth();
//        transformComponent.scale.x = scale;
//        transformComponent.scale.y = scale;
//
////        CropComponent cropComponent = engine.createComponent(CropComponent.class);
////        cropComponent.srcX = 0;
////        cropComponent.srcY = 0;
////        cropComponent.srcWidth = Info.W;
////        cropComponent.srcHeight = Info.H;
////        cropComponent.region = new TextureRegion(texture.getTexture(), cropComponent.srcX, cropComponent.srcY, cropComponent.srcWidth, cropComponent.srcHeight);
//
//        BackgroundBigDotComponent bigDotComponent = engine.createComponent(BackgroundBigDotComponent.class);
//        bigDotComponent.x = MathUtils.random(-Info.worldWidthLimit, Info.worldWidthLimit);
//        bigDotComponent.y = MathUtils.random(-Info.worldHeightLimit, Info.worldHeightLimit);
//        bigDotComponent.x = 0;
//        bigDotComponent.y = 0;
//        entity.add(backgroundComponent);
//        entity.add(bigDotComponent);
//        entity.add(textureComponent);
//        entity.add(transformComponent);
//        engine.addEntity(entity);
//    }

    private void createSmallDot(Entity entity, int index, float width, float height) {
        TextureComponent textureComponent = engine.createComponent(TextureComponent.class);
        textureComponent.textureRegion = smallDotTextures.get(index);

        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
        transformComponent.position.x = MathUtils.random(-Info.worldWidthLimit - 20, Info.worldWidthLimit + 20);
        transformComponent.position.y = MathUtils.random(-Info.worldHeightLimit - 20, Info.worldHeightLimit + 20);
        transformComponent.position.z = Info.ZOrder.BACKGROUND.getValue();
        transformComponent.scale.x = width / textureComponent.textureRegion.getRegionWidth();
        transformComponent.scale.y = height / textureComponent.textureRegion.getRegionHeight();
        transformComponent.angleRad = MathUtils.random(0, Info.rad360Deg);

        BackgroundComponent backgroundComponent = engine.createComponent(BackgroundComponent.class);
        BackgroundSmallDotComponent smallDotComponent = engine.createComponent(BackgroundSmallDotComponent.class);
        entity.add(backgroundComponent);
        entity.add(smallDotComponent);
        entity.add(textureComponent);
        entity.add(transformComponent);
        engine.addEntity(entity);

        createDot(engine.createEntity(), index, width * 16, height * 16, transformComponent.position.x, transformComponent.position.y);
    }

    private void createDot(Entity entity, int index, float width, float height, float x, float y) {
        TextureComponent textureComponent = engine.createComponent(TextureComponent.class);
        textureComponent.textureRegion = dotTextures.get(index);

        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
        transformComponent.position.x = x;
        transformComponent.position.y = y;
        transformComponent.position.z = Info.ZOrder.BACKGROUND.getValue();
        transformComponent.scale.x = width / textureComponent.textureRegion.getRegionWidth();
        transformComponent.scale.y = height / textureComponent.textureRegion.getRegionHeight();
        transformComponent.angleRad = MathUtils.random(0, Info.rad360Deg);

        BackgroundComponent backgroundComponent = engine.createComponent(BackgroundComponent.class);
        BackgroundSmallDotComponent smallDotComponent = engine.createComponent(BackgroundSmallDotComponent.class);
        entity.add(backgroundComponent);
        entity.add(smallDotComponent);
        entity.add(textureComponent);
        entity.add(transformComponent);
        engine.addEntity(entity);
    }



}
