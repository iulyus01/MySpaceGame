package com.myspacegame.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.factories.EntitiesFactory;
import com.myspacegame.factories.WorldFactory;

import java.util.ArrayList;
import java.util.List;

public class EnemyGenerationSystem extends com.badlogic.ashley.systems.IntervalSystem {

    private final PooledEngine engine;
    private final WorldFactory worldFactory;
    private final EntitiesFactory entitiesFactory;
    private final World world;

    private final List<Entity> enemies;
    private final int maxNrAlive = 5;

    public EnemyGenerationSystem(MainClass game, PooledEngine engine) {
        super(.5f);
        this.engine = engine;
        this.worldFactory = WorldFactory.getInstance(game, engine);
        this.world = this.worldFactory.getWorld();
        this.entitiesFactory = EntitiesFactory.getInstance(game, engine, world);

        enemies = new ArrayList<>();
        enemies.add(worldFactory.createEnemyRandomShip(20, 10));
    }

    @Override
    protected void updateInterval() {
        enemies.removeIf(entity -> entity.getComponent(PieceComponent.class) == null);
        if(enemies.size() < maxNrAlive) {
            float x = MathUtils.random(-Info.worldWidthLimit, Info.worldHeightLimit);
            float y = MathUtils.random(-Info.worldWidthLimit, Info.worldHeightLimit);
            enemies.add(worldFactory.createEnemyRandomShip(x, y));
        }
    }

}