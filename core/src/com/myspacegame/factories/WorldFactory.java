package com.myspacegame.factories;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.entities.Area;

import java.util.*;

public class WorldFactory {

    private static WorldFactory instance = null;
    private final PooledEngine engine;
    private final World world;
    private final EntitiesFactory entitiesFactory;
    private final LevelFactory levelFactory;

    private final Array<Area> mapList;
    private Area startPoint;
    private Area endPoint;
    private Area current;

    private WorldFactory(MainClass game, PooledEngine engine) {
        this.engine = engine;
        this.world = new World(new Vector2(0, 0), true);
        world.setContactListener(new B2dContactListener());
        world.setContactFilter(new B2dContactFilter());

        this.entitiesFactory = EntitiesFactory.getInstance(game, engine, world);
        this.levelFactory = LevelFactory.getInstance(game, engine, world);

        mapList = generateMap();

        createLevel(current, true);
    }

    public static WorldFactory getInstance(MainClass game, PooledEngine engine) {
        if(instance == null) instance = new WorldFactory(game, engine);
        return instance;
    }

    private Array<Area> generateMap() {
        Array<Area> list = new Array<>(true, 18, Area.class);
        list.add(new Area(0, 0, new Vector2(10, 160)));
        list.add(new Area(1, 1, new Vector2(30, 152)));
        list.add(new Area(2, 2, new Vector2(52, 135)));
        list.add(new Area(3, 2, new Vector2(86, 135)));
        list.add(new Area(4, 1, new Vector2(70, 122)));
        list.add(new Area(5, 2, new Vector2(54, 100)));
        list.add(new Area(6, 1, new Vector2(120, 110)));
        list.add(new Area(7, 3, new Vector2(132, 72)));
        list.add(new Area(8, 1, new Vector2(118, 82)));
        list.add(new Area(9, 1, new Vector2(86, 73)));
        list.add(new Area(10, 4, new Vector2(65, 60)));
        list.add(new Area(11, 0, new Vector2(40, 60)));
        list.add(new Area(12, 1, new Vector2(100, 60)));
        list.add(new Area(13, 1, new Vector2(115, 40)));
        list.add(new Area(14, 3, new Vector2(78, 33)));
        list.add(new Area(15, 2, new Vector2(55, 33)));
        list.add(new Area(16, 5, new Vector2(33, 16)));
        list.add(new Area(17, 5, new Vector2(82, 6)));

        float cornerOffset = 10;
        list.get(0).to.add(new Info.Pair<>(list.get(1), new Vector2(Info.worldWidthLimit - cornerOffset, Info.worldHeightLimit - cornerOffset)));
        list.get(1).to.add(
                new Info.Pair<>(list.get(0), new Vector2(-Info.worldWidthLimit + cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(2), new Vector2(Info.worldWidthLimit - cornerOffset, Info.worldHeightLimit - cornerOffset)));
        list.get(2).to.add(
                new Info.Pair<>(list.get(1), new Vector2(-Info.worldWidthLimit + cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(3), new Vector2(Info.worldWidthLimit - cornerOffset, -10)),
                new Info.Pair<>(list.get(5), new Vector2(0, Info.worldHeightLimit - cornerOffset)));
        list.get(3).to.add(
                new Info.Pair<>(list.get(2), new Vector2(-Info.worldWidthLimit + cornerOffset, 10)),
                new Info.Pair<>(list.get(4), new Vector2(-Info.worldWidthLimit + cornerOffset, Info.worldHeightLimit - cornerOffset)),
                new Info.Pair<>(list.get(6), new Vector2(Info.worldWidthLimit - cornerOffset, Info.worldHeightLimit - cornerOffset))
        );
        list.get(4).to.add(
                new Info.Pair<>(list.get(3), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(5), new Vector2(-Info.worldWidthLimit + cornerOffset, Info.worldHeightLimit - cornerOffset)));
        list.get(5).to.add(
                new Info.Pair<>(list.get(2), new Vector2(0, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(4), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(10), new Vector2(10, Info.worldHeightLimit - cornerOffset)));
        list.get(6).to.add(
                new Info.Pair<>(list.get(3), new Vector2(-Info.worldWidthLimit + cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(7), new Vector2(20, Info.worldHeightLimit - cornerOffset)));
        list.get(7).to.add(
                new Info.Pair<>(list.get(6), new Vector2(-20, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(8), new Vector2(-Info.worldWidthLimit + cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(12), new Vector2(-Info.worldWidthLimit + cornerOffset, Info.worldHeightLimit - cornerOffset)),
                new Info.Pair<>(list.get(13), new Vector2(-20, Info.worldHeightLimit - cornerOffset))
        );
        list.get(8).to.add(
                new Info.Pair<>(list.get(7), new Vector2(Info.worldWidthLimit - cornerOffset, Info.worldHeightLimit - cornerOffset)),
                new Info.Pair<>(list.get(9), new Vector2(-Info.worldWidthLimit + cornerOffset, 0)));
        list.get(9).to.add(
                new Info.Pair<>(list.get(8), new Vector2(Info.worldWidthLimit - cornerOffset, 0)),
                new Info.Pair<>(list.get(10), new Vector2(-Info.worldWidthLimit + cornerOffset, Info.worldHeightLimit - cornerOffset)));

        list.get(10).to.add(
                new Info.Pair<>(list.get(5), new Vector2(-Info.worldWidthLimit + cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(9), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(11), new Vector2(-Info.worldWidthLimit + cornerOffset, 0)),
                new Info.Pair<>(list.get(12), new Vector2(Info.worldWidthLimit - cornerOffset, 0))
        );
        list.get(10).to.add(
                new Info.Pair<>(list.get(14), new Vector2(Info.worldWidthLimit - cornerOffset, Info.worldHeightLimit - cornerOffset)),
                new Info.Pair<>(list.get(15), new Vector2(-10, Info.worldHeightLimit - cornerOffset)));

        list.get(11).to.add(new Info.Pair<>(list.get(10), new Vector2(Info.worldWidthLimit - cornerOffset, 0)));
        list.get(12).to.add(
                new Info.Pair<>(list.get(7), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(10), new Vector2(-Info.worldWidthLimit + cornerOffset, 0)));
        list.get(13).to.add(
                new Info.Pair<>(list.get(7), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(14), new Vector2(-Info.worldWidthLimit + cornerOffset, 10)));
        list.get(14).to.add(
                new Info.Pair<>(list.get(10), new Vector2(-Info.worldWidthLimit + cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(13), new Vector2(Info.worldWidthLimit - cornerOffset, -10)),
                new Info.Pair<>(list.get(15), new Vector2(-Info.worldWidthLimit + cornerOffset, 0)),
                new Info.Pair<>(list.get(17), new Vector2(-Info.worldWidthLimit + cornerOffset, Info.worldHeightLimit - cornerOffset))
        );
        list.get(15).to.add(
                new Info.Pair<>(list.get(10), new Vector2(10, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(list.get(14), new Vector2(Info.worldWidthLimit - cornerOffset, 0)),
                new Info.Pair<>(list.get(16), new Vector2(-Info.worldWidthLimit + cornerOffset, Info.worldHeightLimit - cornerOffset)));
        list.get(16).to.add(new Info.Pair<>(list.get(15), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)));
        list.get(17).to.add(new Info.Pair<>(list.get(14), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)));

        startPoint = list.get(0);
        endPoint = list.get(11);
        current = startPoint;

        return list;
    }

    public Entity createEnemyRandomShip(int actorId, float x, float y) {
//        List<Entity> enemyEntities = entitiesFactory.createShip(Info.ships.get(MathUtils.random(Info.ships.size() - 1)), x, y, actorId, false);
        List<Entity> enemyEntities = entitiesFactory.createShip(Info.ships.get(2), x, y, actorId, false);
        for(Entity entity : enemyEntities) engine.addEntity(entity);
        return enemyEntities.get(0);
    }

    public void destroyLevel() {
        levelFactory.destroyLevel();
    }

    public void createLevel(Area area, boolean withPlayer) {
        levelFactory.createLevel(area, withPlayer);
    }

    public void setPlayerToTp(Vector2 pos) {
        levelFactory.setPlayerPos(pos);
    }

    public World getWorld() {
        return world;
    }

    public PooledEngine getEngine() {
        return engine;
    }

}
