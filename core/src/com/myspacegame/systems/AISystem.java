package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.*;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.entities.Anchor;
import com.myspacegame.entities.CorePiece;
import com.myspacegame.entities.Piece;
import com.myspacegame.factories.BodyFactory;
import com.myspacegame.factories.WorldFactory;

public class AISystem extends IteratingSystem {

    private final PooledEngine engine;

    private final ComponentMapper<ShipComponent> shipMapper;
    private final ComponentMapper<NPCComponent> npcMapper;

    private final BodyFactory bodyFactory;

    public AISystem(MainClass game, PooledEngine engine) {
        super(Family.all(ShipCoreComponent.class, NPCComponent.class, ShipComponent.class).get());
        this.engine = engine;
        World world = WorldFactory.getInstance(game, engine).getWorld();
        this.bodyFactory = BodyFactory.getInstance(world);

        shipMapper = ComponentMapper.getFor(ShipComponent.class);
        npcMapper = ComponentMapper.getFor(NPCComponent.class);

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ShipComponent shipComponent = shipMapper.get(entity);
        NPCComponent npcComponent = npcMapper.get(entity);

        if(npcComponent.type == Info.NPCType.ENEMY) {

        }
    }
}
