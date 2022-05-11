package com.myspacegame.factories;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.pieces.HullPieceComponent;
import com.myspacegame.components.pieces.ThrusterPieceComponent;
import com.myspacegame.components.pieces.WeaponPieceComponent;

import java.util.ArrayList;
import java.util.List;

public class LevelFactory {

    private static LevelFactory instance = null;
    private final PooledEngine engine;
    private final World world;
    private final BackgroundFactory backgroundFactory;
    private final EntitiesFactory entitiesFactory;

    private final int currentLevel = 0;
    private final List<Color> backgroundColor;

    private LevelFactory(MainClass game, PooledEngine engine, World world) {
        this.engine = engine;
        this.world = world;

        backgroundFactory = BackgroundFactory.getInstance(game, engine, world);
        entitiesFactory = EntitiesFactory.getInstance(game, engine, world);

        backgroundColor = new ArrayList<>();
//        backgroundColor.add();
    }

    public void createLevel(WorldFactory.Point currentPoint) {
        generateBackground(currentPoint.difficulty);

        addPlayer();

        createWalls();

//        addRocks();

        for(int i = 0; i < 5; i++) engine.addEntity(entitiesFactory.createPieceEntity(HullPieceComponent.class, true, 0, 0));
        for(int i = 0; i < 5; i++) engine.addEntity(entitiesFactory.createPieceEntity(WeaponPieceComponent.class, true, 0, 0));
        for(int i = 0; i < 5; i++) engine.addEntity(entitiesFactory.createPieceEntity(ThrusterPieceComponent.class, true, 0, 0));
    }

    public static LevelFactory getInstance(MainClass game, PooledEngine engine, World world) {
        if(instance == null) instance = new LevelFactory(game, engine, world);
        return instance;
    }

    public Entity createEnemyRandomShip(int actorId, float x, float y) {
        List<Entity> enemyEntities = entitiesFactory.createShip(Info.ships.get(MathUtils.random(Info.ships.size() - 1)), x, y, actorId, false);
        for(Entity entity : enemyEntities) engine.addEntity(entity);
        return enemyEntities.get(0);
    }

    private void generateBackground(int difficulty) {
        backgroundFactory.generate(difficulty);
    }

    private void addPlayer() {
        List<Entity> playerEntities = entitiesFactory.createShip(Info.ships.get(3), 10, 10, Info.StaticActorIds.PLAYER.getValue(), true);
        for(Entity entity : playerEntities) engine.addEntity(entity);

    }

    private void createWalls() {
        Entity entity;
        int nr = 80;
        float posX;
        float posY;
        for(int i = 0; i < nr; i++) {
            // right
            posX = MathUtils.random(Info.worldWidthLimit, Info.worldWidthLimit + 20);
            posY = MathUtils.random(-Info.worldHeightLimit, Info.worldHeightLimit);
            entity = entitiesFactory.createRockEntity(0, posX, posY, true);
            engine.addEntity(entity);
        }
        for(int i = 0; i < nr + 8; i++) {
            // top
            posX = MathUtils.random(-Info.worldWidthLimit - 20, Info.worldWidthLimit + 20);
            posY = MathUtils.random(Info.worldHeightLimit, Info.worldHeightLimit + 20);
            entity = entitiesFactory.createRockEntity(0, posX, posY, true);
            engine.addEntity(entity);
        }
        for(int i = 0; i < nr; i++) {
            // left
            posX = MathUtils.random(-Info.worldWidthLimit - 20, -Info.worldWidthLimit);
            posY = MathUtils.random(-Info.worldHeightLimit, Info.worldHeightLimit);
            entity = entitiesFactory.createRockEntity(0, posX, posY, true);
            engine.addEntity(entity);
        }
        for(int i = 0; i < nr + 8; i++) {
            // bottom
            posX = MathUtils.random(-Info.worldWidthLimit - 20, Info.worldWidthLimit + 20);
            posY = MathUtils.random(-Info.worldHeightLimit - 20, -Info.worldHeightLimit);
            entity = entitiesFactory.createRockEntity(0, posX, posY, true);
            engine.addEntity(entity);
        }

    }

    public World getWorld() {
        return world;
    }

    public PooledEngine getEngine() {
        return engine;
    }
}