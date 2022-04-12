package com.myspacegame.factories;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.*;
import com.myspacegame.Info;
import com.myspacegame.components.BulletComponent;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.entities.Piece;
import com.myspacegame.entities.Projectile;

public class BodyFactory {

    private static BodyFactory instance = null;
    private final World world;

    private BodyFactory(World world) {
        this.world = world;
    }

    public static BodyFactory getInstance(World world) {
        if(instance == null) instance = new BodyFactory(world);
        return instance;
    }

    private FixtureDef initFixtureDef(Info.EntityType entityType) {
        FixtureDef fixtureDef = new FixtureDef();
        switch(entityType) {
            case PIECE:
                fixtureDef.density = Info.defaultPieceDensity;
                fixtureDef.friction = 0f;
                fixtureDef.restitution = 0f;
//                fixtureDef.filter.categoryBits = Info.CATEGORY_PIECE;
//                fixtureDef.filter.maskBits = Info.MASK_EVERYTHING;
                break;
            case BULLET:
                fixtureDef.density = Info.defaultBulletDensity;
                fixtureDef.friction = 0f;
                fixtureDef.restitution = 0f;
//                fixtureDef.filter.categoryBits = Info.CATEGORY_BULLET;
//                fixtureDef.filter.maskBits = Info.MASK_NOTHING;
                break;
            case WALL:
                fixtureDef.density = 10;
                fixtureDef.friction = .4f;
                fixtureDef.restitution = .01f;
                break;
        }
        return fixtureDef;
    }

    private BodyDef initBodyDef(Info.EntityType entityType) {
        BodyDef bodyDef = new BodyDef();
        switch(entityType) {
            case PIECE:
                bodyDef.type = BodyDef.BodyType.DynamicBody;
                bodyDef.fixedRotation = false;
                bodyDef.angularDamping = Info.defaultPieceAngularDamping;
                bodyDef.linearDamping = Info.defaultPieceLinearDamping;
                bodyDef.gravityScale = 0;
                break;
            case BULLET:
                bodyDef.type = BodyDef.BodyType.DynamicBody;
                bodyDef.fixedRotation = true;
                bodyDef.bullet = true;
                break;
            case WALL:
                bodyDef.type = BodyDef.BodyType.StaticBody;
                break;
        }
        return bodyDef;
    }

    public Body createPieceBody(float bodyX, float bodyY, float angle) {
        BodyDef bodyDef = initBodyDef(Info.EntityType.PIECE);
        bodyDef.position.x = bodyX;
        bodyDef.position.y = bodyY;
        bodyDef.angle = angle;

        return world.createBody(bodyDef);
    }

    public Body createBulletBody(float x, float y, float width, float height, float angleRad, float impulse, Entity entity) {
        BodyDef bodyDef = initBodyDef(Info.EntityType.BULLET);
        bodyDef.position.x = x;
        bodyDef.position.y = y;
        bodyDef.angle = angleRad;

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);

        FixtureDef fixtureDef = initFixtureDef(Info.EntityType.BULLET);
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(entity);
        shape.dispose();

        body.applyLinearImpulse(
                impulse * (float) Math.cos(angleRad), impulse * (float) Math.sin(angleRad),
                body.getWorldCenter().x, body.getWorldCenter().y,
                true
        );

        return body;
    }

    public Body createWallBody(float x, float y, float width, float height, Entity entity) {
        BodyDef bodyDef = initBodyDef(Info.EntityType.WALL);
        bodyDef.position.x = x;
        bodyDef.position.y = y;
        bodyDef.angle = 0;

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);

        FixtureDef fixtureDef = initFixtureDef(Info.EntityType.WALL);
        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(entity);
        shape.dispose();

        return body;
    }

    public Fixture createPieceFixture(Body body, Piece piece, Entity entity) {
        float[] vertices = new float[piece.shape.getVertices().length];
        for(int i = 0; i < piece.shape.getVertices().length;) {
            vertices[i] = piece.shape.getVertices()[i] + piece.pos.x * Info.blockSize;
            vertices[i + 1] = piece.shape.getVertices()[i + 1] + piece.pos.y * Info.blockSize;
            i += 2;
        }

        PolygonShape shape = new PolygonShape();
        shape.set(vertices);

        FixtureDef fixtureDef = initFixtureDef(Info.EntityType.PIECE);
        fixtureDef.shape = shape;
        fixtureDef.friction = 0;
        fixtureDef.restitution = 0;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(entity);

        shape.dispose();

        return fixture;
    }

    // this is used when swimming, you can't swim up if you're outside the water
    public void makeAllFixturesSensors(Body body) {
        for (Fixture fix : body.getFixtureList()) {
            fix.setSensor(true);
        }
    }

}
