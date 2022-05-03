package com.myspacegame.factories;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.pieces.HullPieceComponent;
import com.myspacegame.components.pieces.ThrusterPieceComponent;
import com.myspacegame.components.pieces.WeaponPieceComponent;

import java.util.List;

public class WorldFactory {

    private static WorldFactory instance = null;
    private final PooledEngine engine;
    private final World world;
    private final BackgroundFactory backgroundFactory;
    private final EntitiesFactory entitiesFactory;

    private WorldFactory(MainClass game, PooledEngine engine) {
        this.engine = engine;

        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new B2dContactListener());
        world.setContactFilter(new B2dContactFilter());

        backgroundFactory = BackgroundFactory.getInstance(game, engine, world);
        entitiesFactory = EntitiesFactory.getInstance(game, engine, world);

        generateBackground();
        addPlayer();
        createWalls();
    }

    public static WorldFactory getInstance(MainClass game, PooledEngine engine) {
        if(instance == null) instance = new WorldFactory(game, engine);
        return instance;
    }

    public Entity createEnemyRandomShip(int actorId, float x, float y) {
        List<Entity> enemyEntities = entitiesFactory.createShip(Info.ships.get(MathUtils.random(Info.ships.size() - 1)), x, y, actorId, false);
        for(Entity entity : enemyEntities) engine.addEntity(entity);
        return enemyEntities.get(0);
    }

    private void generateBackground() {
        backgroundFactory.generate();
    }

    private void addPlayer() {
        List<Entity> playerEntities = entitiesFactory.createShip(Info.ships.get(3), 10, 10, Info.StaticActorIds.PLAYER.getValue(), true);
        for(Entity entity : playerEntities) engine.addEntity(entity);



        for(int i = 0; i < 10; i++) engine.addEntity(entitiesFactory.createPiece(HullPieceComponent.class, true, 0, 0));
        for(int i = 0; i < 10; i++) engine.addEntity(entitiesFactory.createPiece(WeaponPieceComponent.class, true, 0, 0));
        for(int i = 0; i < 10; i++) engine.addEntity(entitiesFactory.createPiece(ThrusterPieceComponent.class, true, 0, 0));
        for(int i = 0; i < 10; i++) engine.addEntity(entitiesFactory.createPiece(HullPieceComponent.class, true, 0, 0));
        for(int i = 0; i < 10; i++) engine.addEntity(entitiesFactory.createPiece(WeaponPieceComponent.class, true, 0, 0));
        for(int i = 0; i < 10; i++) engine.addEntity(entitiesFactory.createPiece(ThrusterPieceComponent.class, true, 0, 0));
        for(int i = 0; i < 10; i++) engine.addEntity(entitiesFactory.createPiece(HullPieceComponent.class, true, 0, 0));
        for(int i = 0; i < 10; i++) engine.addEntity(entitiesFactory.createPiece(WeaponPieceComponent.class, true, 0, 0));
        for(int i = 0; i < 10; i++) engine.addEntity(entitiesFactory.createPiece(ThrusterPieceComponent.class, true, 0, 0));
    }

    private void createWalls() {
        Entity wallEntity = entitiesFactory.createWall(Info.worldWidthLimit, 0, 1, Info.worldHeightLimit * 2);
        engine.addEntity(wallEntity);
        wallEntity = entitiesFactory.createWall(0, Info.worldHeightLimit, Info.worldWidthLimit * 2, 1);
        engine.addEntity(wallEntity);
        wallEntity = entitiesFactory.createWall(-Info.worldWidthLimit, 0, 1, Info.worldHeightLimit * 2);
        engine.addEntity(wallEntity);
        wallEntity = entitiesFactory.createWall(0, -Info.worldHeightLimit, Info.worldWidthLimit * 2, 1);
        engine.addEntity(wallEntity);
    }

    public World getWorld() {
        return world;
    }

    public PooledEngine getEngine() {
        return engine;
    }
}
