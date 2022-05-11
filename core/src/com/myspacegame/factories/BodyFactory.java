package com.myspacegame.factories;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
import com.myspacegame.components.ShipComponent;
import com.myspacegame.entities.Piece;

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
            case ROCK:
                fixtureDef.density = 8;
                fixtureDef.friction = .1f;
                fixtureDef.restitution = .2f;
                break;
        }
        return fixtureDef;
    }

    private BodyDef initBodyDef(Info.EntityType entityType, boolean single) {
        BodyDef bodyDef = new BodyDef();
        switch(entityType) {
            case PIECE:
                bodyDef.type = BodyDef.BodyType.DynamicBody;
                bodyDef.fixedRotation = false;
                bodyDef.angularDamping = single ? Info.defaultSinglePieceAngularDamping : Info.defaultPieceAngularDamping;
                bodyDef.linearDamping = single ? Info.defaultSinglePieceLinearDamping : Info.defaultPieceLinearDamping;
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
            case ROCK:
                bodyDef.type = BodyDef.BodyType.DynamicBody;
                bodyDef.fixedRotation = false;
                bodyDef.linearDamping = Info.defaultRockLinearDamping;
                bodyDef.angularDamping = Info.defaultRockAngularDamping;
                break;
        }
        return bodyDef;
    }

    public Body createPieceBody(float bodyX, float bodyY, float angle, boolean single) {
        BodyDef bodyDef = initBodyDef(Info.EntityType.PIECE, single);
        bodyDef.position.x = bodyX;
        bodyDef.position.y = bodyY;
        bodyDef.angle = angle;

        return world.createBody(bodyDef);
    }

    public Body createBulletBody(float x, float y, float width, float height, float angleRad, float impulse, Entity entity) {
        BodyDef bodyDef = initBodyDef(Info.EntityType.BULLET, false);
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

    public Body createRockBody(int index, float x, float y, float angleRad, float sizeRatio, float impulseX, float impulseY, Entity entity) {
        BodyDef bodyDef = initBodyDef(Info.EntityType.ROCK, false);
        bodyDef.position.x = x;
        bodyDef.position.y = y;
        bodyDef.angle = angleRad;

        Body body = world.createBody(bodyDef);

        Array<Array<Float>> arrayOfDots = Info.rockShapesMap.get(index).shape;

        for(int i = 0; i < arrayOfDots.size; i++) {
            float[] dots = new float[arrayOfDots.get(i).size];
            for(int j = 0; j < dots.length; j++) {
                dots[j] = arrayOfDots.get(i).get(j) * sizeRatio;
            }

            PolygonShape shape = new PolygonShape();
            shape.set(dots);

            FixtureDef fixtureDef = initFixtureDef(Info.EntityType.ROCK);
            fixtureDef.shape = shape;
            Fixture fixture = body.createFixture(fixtureDef);
            fixture.setUserData(entity);
            shape.dispose();
        }


        body.applyLinearImpulse(
                impulseX, impulseY,
                body.getWorldCenter().x, body.getWorldCenter().y,
                true
        );

        return body;
    }

    public Body createWallBody(float x, float y, float width, float height, Entity entity) {
        BodyDef bodyDef = initBodyDef(Info.EntityType.WALL, false);
        bodyDef.position.x = x;
        bodyDef.position.y = y;
        bodyDef.angle = 0;

        Body body = world.createBody(bodyDef);
        body.setUserData("body");

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
        float[] vertices = new float[piece.shape.getTransformedVertices().length];
        for(int i = 0; i < piece.shape.getTransformedVertices().length;) {
            vertices[i] = piece.shape.getTransformedVertices()[i] + piece.pos.x * Info.blockSize;
            vertices[i + 1] = piece.shape.getTransformedVertices()[i + 1] + piece.pos.y * Info.blockSize;
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
