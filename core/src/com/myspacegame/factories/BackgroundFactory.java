package com.myspacegame.factories;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
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

    private final List<TextureRegion> bigDotTextures;
    private final List<TextureRegion> smallDotTextures;

    private BackgroundFactory(MainClass game, Engine engine, World world) {
        this.game = game;
        this.engine = engine;
        this.world = world;

        bodyFactory = BodyFactory.getInstance(world);

        bigDotTextures = new ArrayList<>();
        bigDotTextures.add(new TextureRegion(game.assetManager.get("images/background/BackgroundDot1.png", Texture.class)));
        bigDotTextures.add(new TextureRegion(game.assetManager.get("images/background/BackgroundDot12.png", Texture.class)));
        bigDotTextures.add(new TextureRegion(game.assetManager.get("images/background/BackgroundDot2.png", Texture.class)));
        bigDotTextures.add(new TextureRegion(game.assetManager.get("images/background/BackgroundDot3.png", Texture.class)));
        bigDotTextures.add(new TextureRegion(game.assetManager.get("images/background/BackgroundDot4.png", Texture.class)));
        smallDotTextures = new ArrayList<>();
        smallDotTextures.add(new TextureRegion(game.assetManager.get("images/background/SmallDot1.png", Texture.class)));
        smallDotTextures.add(new TextureRegion(game.assetManager.get("images/background/SmallDot2.png", Texture.class)));
        smallDotTextures.add(new TextureRegion(game.assetManager.get("images/background/SmallDot3.png", Texture.class)));
    }

    public static BackgroundFactory getInstance(MainClass game, Engine engine, World world) {
        if(instance == null) instance = new BackgroundFactory(game, engine, world);
        return instance;
    }

    public void generate() {
        for(int i = 0; i < 1000; i++) {
            float size = MathUtils.random(Info.blockSize / 8, Info.blockSize / 1.2f);
            createSmallDot(engine.createEntity(), smallDotTextures.get(MathUtils.random(0, smallDotTextures.size() - 1)), size, size);
        }
//        createBigDot(engine.createEntity(), bigDotTextures.get(3));
//        createBigDot(engine.createEntity(), "images/background/BackgroundDot2.png");
//        createBigDot(engine.createEntity(), "images/background/BackgroundDot2.png");
//        createBigDot(engine.createEntity(), "images/background/BackgroundDot3.png");
//        createBigDot(engine.createEntity(), "images/background/BackgroundDot3.png");
//        createBigDot(engine.createEntity(), "images/background/BackgroundDot4.png");
    }

    private void createBigDot(Entity entity, TextureRegion texture) {
        TextureComponent textureComponent = engine.createComponent(TextureComponent.class);
        textureComponent.textureRegion = texture;

        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
//        transformComponent.position.x = MathUtils.random(-200f, 200f);
//        transformComponent.position.y = MathUtils.random(-200f, 200f);
        transformComponent.position.x = 10;
        transformComponent.position.y = 10;
        transformComponent.position.z = -2;
        float scale = Info.PIXELS_TO_METRES * 8;
        transformComponent.scale.x = scale;
        transformComponent.scale.y = scale;

        CropComponent cropComponent = engine.createComponent(CropComponent.class);
        cropComponent.srcX = 0;
        cropComponent.srcY = 0;
        cropComponent.srcWidth = Info.W;
        cropComponent.srcHeight = Info.H;
        cropComponent.region = new TextureRegion(texture.getTexture(), cropComponent.srcX, cropComponent.srcY, cropComponent.srcWidth, cropComponent.srcHeight);

        BackgroundComponent backgroundComponent = engine.createComponent(BackgroundComponent.class);
        BackgroundBigDotComponent bigDotComponent = engine.createComponent(BackgroundBigDotComponent.class);
//        bigDotComponent.x = MathUtils.random(-200f, 200f);
//        bigDotComponent.y = MathUtils.random(-200f, 200f);
        bigDotComponent.x = 10;
        bigDotComponent.y = 10;
        entity.add(backgroundComponent);
        entity.add(bigDotComponent);
        entity.add(textureComponent);
        entity.add(cropComponent);
        entity.add(transformComponent);
        engine.addEntity(entity);
    }

    private void createSmallDot(Entity entity,  TextureRegion texture, float width, float height) {
        TextureComponent textureComponent = engine.createComponent(TextureComponent.class);
        textureComponent.textureRegion = texture;

        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
        transformComponent.position.x = MathUtils.random(-200f, 200f);
        transformComponent.position.y = MathUtils.random(-200f, 200f);
        transformComponent.position.z = -2;
        transformComponent.scale.x = width / texture.getRegionWidth();
        transformComponent.scale.y = height / texture.getRegionHeight();

        BackgroundComponent backgroundComponent = engine.createComponent(BackgroundComponent.class);
        BackgroundSmallDotComponent smallDotComponent = engine.createComponent(BackgroundSmallDotComponent.class);
        entity.add(backgroundComponent);
        entity.add(smallDotComponent);
        entity.add(textureComponent);
        entity.add(transformComponent);
        engine.addEntity(entity);
    }



}
