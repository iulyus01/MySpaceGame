package com.myspacegame.factories;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.myspacegame.Info;
import com.myspacegame.MainClass;

import java.util.*;

public class WorldFactory {

    private static WorldFactory instance = null;
    private final PooledEngine engine;
    private final World world;
    private final EntitiesFactory entitiesFactory;


    private Point startPoint;
    private Point endPoint;
    private Point current;

    private WorldFactory(MainClass game, PooledEngine engine) {
        this.engine = engine;

        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new B2dContactListener());
        world.setContactFilter(new B2dContactFilter());

        entitiesFactory = EntitiesFactory.getInstance(game, engine, world);
        LevelFactory levelFactory = LevelFactory.getInstance(game, engine, world);

        generateMap();

        levelFactory.createLevel(current);

    }

    public static WorldFactory getInstance(MainClass game, PooledEngine engine) {
        if(instance == null) instance = new WorldFactory(game, engine);
        return instance;
    }

    private void generateMap() {
        List<Point> list = new ArrayList<>(18);
        list.add(new Point(0, 0, new Vector2(10, 160)));
        list.add(new Point(1, 1, new Vector2(30, 152)));
        list.add(new Point(2, 2, new Vector2(52, 135)));
        list.add(new Point(3, 2, new Vector2(86, 135)));
        list.add(new Point(4, 1, new Vector2(70, 122)));
        list.add(new Point(5, 2, new Vector2(54, 100)));
        list.add(new Point(6, 1, new Vector2(120, 110)));
        list.add(new Point(7, 3, new Vector2(132, 72)));
        list.add(new Point(8, 1, new Vector2(118, 82)));
        list.add(new Point(9, 1, new Vector2(86, 73)));
        list.add(new Point(10, 5, new Vector2(65, 60)));
        list.add(new Point(11, 0, new Vector2(40, 60)));
        list.add(new Point(12, 1, new Vector2(100, 60)));
        list.add(new Point(13, 1, new Vector2(115, 40)));
        list.add(new Point(14, 3, new Vector2(78, 33)));
        list.add(new Point(15, 2, new Vector2(55, 33)));
        list.add(new Point(16, 7, new Vector2(33, 16)));
        list.add(new Point(17, 7, new Vector2(82, 6)));

        list.get(0).to = List.of(list.get(1));
        list.get(1).to = List.of(list.get(0), list.get(2));
        list.get(2).to = List.of(list.get(1), list.get(3), list.get(5));
        list.get(3).to = List.of(list.get(2), list.get(4), list.get(6));
        list.get(4).to = List.of(list.get(3), list.get(5));
        list.get(5).to = List.of(list.get(2), list.get(4), list.get(10));
        list.get(6).to = List.of(list.get(3), list.get(7));
        list.get(7).to = List.of(list.get(6), list.get(8), list.get(12), list.get(13));
        list.get(8).to = List.of(list.get(7), list.get(9));
        list.get(9).to = List.of(list.get(8), list.get(10));
        list.get(10).to = List.of(list.get(5), list.get(9), list.get(11), list.get(12), list.get(14), list.get(15));
        list.get(11).to = List.of(list.get(10));
        list.get(12).to = List.of(list.get(7), list.get(10));
        list.get(13).to = List.of(list.get(7), list.get(14));
        list.get(14).to = List.of(list.get(10), list.get(13), list.get(15), list.get(17));
        list.get(15).to = List.of(list.get(10), list.get(14), list.get(16));
        list.get(16).to = List.of(list.get(15));
        list.get(17).to = List.of(list.get(14));

        startPoint = list.get(0);
        endPoint = list.get(11);
        current = startPoint;
    }

    public Entity createEnemyRandomShip(int actorId, float x, float y) {
        List<Entity> enemyEntities = entitiesFactory.createShip(Info.ships.get(MathUtils.random(Info.ships.size() - 1)), x, y, actorId, false);
        for(Entity entity : enemyEntities) engine.addEntity(entity);
        return enemyEntities.get(0);
    }

    public World getWorld() {
        return world;
    }

    public PooledEngine getEngine() {
        return engine;
    }

    public static class Point {
        int id;
        int difficulty;
        Vector2 pos;
        List<Point> to;

        public Point(int id, int difficulty, Vector2 pos) {
            this.id = id;
            this.difficulty = difficulty;
            this.pos = pos;
        }
    }
}
