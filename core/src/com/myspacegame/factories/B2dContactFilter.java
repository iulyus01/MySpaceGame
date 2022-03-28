package com.myspacegame.factories;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.*;
import com.myspacegame.components.BulletComponent;
import com.myspacegame.components.CollisionComponent;
import com.myspacegame.components.DraggingComponent;
import com.myspacegame.components.pieces.PieceComponent;

public class B2dContactFilter implements ContactFilter {

    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<BulletComponent> bulletMapper;
    private final ComponentMapper<DraggingComponent> draggingMapper;

    public B2dContactFilter() {
        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        bulletMapper = ComponentMapper.getFor(BulletComponent.class);
        draggingMapper = ComponentMapper.getFor(DraggingComponent.class);
    }

    @Override
    public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
        if(!(fixtureA.getUserData() instanceof Entity) || !(fixtureB.getUserData() instanceof Entity)) return true;
        Entity entityA = (Entity) fixtureA.getUserData();
        Entity entityB = (Entity) fixtureB.getUserData();

        if(draggingMapper.has(entityA) || draggingMapper.has(entityB)) return false;

        if(bulletMapper.has(entityA)) return handleBulletContact(bulletMapper.get(entityA), entityB);
        if(bulletMapper.has(entityB)) return handleBulletContact(bulletMapper.get(entityB), entityA);
        return true;
    }

    private boolean handleBulletContact(BulletComponent bulletComponent, Entity entity) {
        if(pieceMapper.has(entity)) {
            // if bullet is created by same actor as piece, it won't collide
            return bulletComponent.createdByActorId != pieceMapper.get(entity).piece.actorId;
        }

        return true;
    }
}