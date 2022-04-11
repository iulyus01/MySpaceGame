package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.MainClass;
import com.myspacegame.components.BodyComponent;
import com.myspacegame.components.BulletComponent;
import com.myspacegame.components.TransformComponent;
import com.myspacegame.components.WorldComponent;
import com.myspacegame.factories.EntitiesFactory;
import com.myspacegame.factories.WorldFactory;

public class EnemyGenerationSystem extends com.badlogic.ashley.systems.IntervalSystem {

    private final PooledEngine engine;
    private final WorldFactory worldFactory;
    private final EntitiesFactory entitiesFactory;
    private final World world;

    private final Array<Entity> enemies;
    private final int maxNrAlive = 2;

    public EnemyGenerationSystem(MainClass game, PooledEngine engine) {
        super(.5f);
        this.engine = engine;
        this.worldFactory = WorldFactory.getInstance(game, engine);
        this.world = this.worldFactory.getWorld();
        this.entitiesFactory = EntitiesFactory.getInstance(game, engine, world);

        enemies = new Array<>(false, 10, Entity.class);
    }

    @Override
    protected void updateInterval() {
        if(enemies.size < maxNrAlive) {
            float x = MathUtils.random(-200f, 200f);
            float y = MathUtils.random(-200f, 200f);
            enemies.add(worldFactory.createEnemyRandomShip(x, y));
        }
    }


}