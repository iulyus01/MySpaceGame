package com.myspacegame.factories;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.TextureComponent;
import com.myspacegame.components.TransformComponent;
import com.myspacegame.components.background.BackgroundComponent;
import com.myspacegame.components.background.BackgroundSmallDotComponent;

public class BackgroundFactory {

    private static BackgroundFactory instance = null;
    private final MainClass game;
    private final Engine engine;

    private final Array<Color> backgroundColors;
    private final Array<TextureRegion> smallDotTextures;
    private final Array<TextureRegion> dotTextures;

    private BackgroundFactory(MainClass game, Engine engine) {
        this.game = game;
        this.engine = engine;

        backgroundColors = new Array<>(true, 3, Color.class);
        backgroundColors.add(new Color(0.086f, 0.169f, 0.149f, 1));
        backgroundColors.add(new Color(0.082f, 0.125f, 0.157f, 1));
        backgroundColors.add(new Color(0.071f, 0.082f, 0.129f, 1));
        backgroundColors.add(new Color(0.094f, 0.071f, 0.149f, 1));
        backgroundColors.add(new Color(0.149f, 0.055f, 0.125f, 1));
        backgroundColors.add(new Color(0.157f, 0.055f, 0.094f, 1));

        smallDotTextures = new Array<>(true, 3, TextureRegion.class);
        smallDotTextures.add(new TextureRegion(game.assetManager.get("images/background/SmallDot1.png", Texture.class)));
        smallDotTextures.add(new TextureRegion(game.assetManager.get("images/background/SmallDot2.png", Texture.class)));
        smallDotTextures.add(new TextureRegion(game.assetManager.get("images/background/SmallDot3.png", Texture.class)));
        dotTextures = new Array<>(true, 3, TextureRegion.class);
        dotTextures.add(new TextureRegion(game.assetManager.get("images/background/Dot1.png", Texture.class)));
        dotTextures.add(new TextureRegion(game.assetManager.get("images/background/Dot2.png", Texture.class)));
        dotTextures.add(new TextureRegion(game.assetManager.get("images/background/Dot3.png", Texture.class)));
    }

    public static BackgroundFactory getInstance(MainClass game, Engine engine) {
        if(instance == null) instance = new BackgroundFactory(game, engine);
        return instance;
    }

    public void generate(int difficulty) {
        Info.colorClear = backgroundColors.get(difficulty);

        for(int i = 0; i < 80; i++) {
            int index = MathUtils.random(0, smallDotTextures.size - 1);
            float size = MathUtils.random(Info.blockSize / 10, Info.blockSize / 1.2f);
            createSmallDot(engine.createEntity(), index, size, size);
        }
    }

    public void destroy() {
        engine.removeAllEntities(Family.one(BackgroundComponent.class).get());
    }

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

    public void createNumber(int number) {
        Entity entity = engine.createEntity();

        if(number < 0 || number > 17) number = 0;
        TextureComponent textureComponent = engine.createComponent(TextureComponent.class);
        textureComponent.textureRegion = new TextureRegion(game.assetManager.get("images/numbers/" + number + ".png", Texture.class));

        float width = Info.blockSize * 20;
        float height = textureComponent.textureRegion.getRegionHeight() / (float) textureComponent.textureRegion.getRegionWidth() * width;

        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
        transformComponent.position.x = 0;
        transformComponent.position.y = 0;
        transformComponent.position.z = Info.ZOrder.BACKGROUND.getValue();
        transformComponent.scale.x = width / textureComponent.textureRegion.getRegionWidth();
        transformComponent.scale.y = height / textureComponent.textureRegion.getRegionHeight();
        transformComponent.angleRad = 0;

        BackgroundComponent backgroundComponent = engine.createComponent(BackgroundComponent.class);
        entity.add(backgroundComponent);
        entity.add(textureComponent);
        entity.add(transformComponent);
        engine.addEntity(entity);
    }



}
