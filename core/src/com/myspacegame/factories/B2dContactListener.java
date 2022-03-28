package com.myspacegame.factories;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.myspacegame.components.CollisionComponent;

public class B2dContactListener implements ContactListener {

    public B2dContactListener() {
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if(fixtureA.getUserData() instanceof Entity) {
            Entity entity = (Entity) fixtureA.getUserData();
            entityCollision(entity, fixtureB);
        } else if(fixtureB.getUserData() instanceof Entity) {
            Entity entity = (Entity) fixtureB.getUserData();
            entityCollision(entity, fixtureA);
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    private void entityCollision(Entity entityA, Fixture fixtureB) {
        if(fixtureB.getUserData() instanceof Entity) {
            Entity entityB = (Entity) fixtureB.getUserData();

            CollisionComponent collisionA = entityA.getComponent(CollisionComponent.class);
            CollisionComponent collisionB = entityB.getComponent(CollisionComponent.class);

            if(collisionA != null) {
                collisionA.collisionEntity = entityB;
            } else if(collisionB != null) {
                collisionB.collisionEntity = entityA;
            }
        }
    }

}