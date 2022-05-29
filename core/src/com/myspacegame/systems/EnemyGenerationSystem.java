package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.ShipCoreComponent;
import com.myspacegame.factories.EntitiesFactory;
import com.myspacegame.factories.WorldFactory;

import java.util.ArrayList;
import java.util.List;

public class EnemyGenerationSystem extends com.badlogic.ashley.systems.IntervalSystem {

    private final PooledEngine engine;
    private final WorldFactory worldFactory;
    private final EntitiesFactory entitiesFactory;
    private final World world;
    private final ComponentMapper<ShipCoreComponent> shipCoreMapper;

    private final List<Entity> enemies;
    private final int maxNrAlive = 3;
    private int enemyActorIdCounter = 1;

    public EnemyGenerationSystem(MainClass game, PooledEngine engine) {
        super(.5f);
        this.engine = engine;
        this.worldFactory = WorldFactory.getInstance(game, engine);
        this.world = this.worldFactory.getWorld();
        this.entitiesFactory = EntitiesFactory.getInstance(game, engine, world);
        shipCoreMapper = ComponentMapper.getFor(ShipCoreComponent.class);

        enemies = new ArrayList<>();
//        enemies.add(worldFactory.createEnemyRandomShip(getEnemyActorIdCounter(), 20, 10));
        engine.addEntityListener(new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {

            }

            @Override
            public void entityRemoved(Entity entity) {
                if(shipCoreMapper.has(entity)) {
                    enemies.remove(entity);
                }
            }
        });
    }

    @Override
    protected void updateInterval() {
        if(enemies.size() < maxNrAlive) {
            float x = MathUtils.random(-Info.worldWidthLimit, Info.worldHeightLimit);
            float y = MathUtils.random(-Info.worldWidthLimit, Info.worldHeightLimit);
            enemies.add(worldFactory.createEnemyRandomShip(getEnemyActorIdCounter(), x, y));
        }
    }

    private int getEnemyActorIdCounter() {
        enemyActorIdCounter += 1;
        return enemyActorIdCounter - 1;
    }

}