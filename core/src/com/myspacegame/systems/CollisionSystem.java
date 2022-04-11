package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.myspacegame.MainClass;
import com.myspacegame.components.*;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.factories.EntitiesFactory;
import com.myspacegame.factories.WorldFactory;

public class CollisionSystem extends IteratingSystem {

    private final ComponentMapper<CollisionComponent> collisionMapper;
    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<BulletComponent> bulletMapper;
    private final ComponentMapper<TextureComponent> textureMapper;
    private final EntitiesFactory entitiesFactory;

    public CollisionSystem(MainClass game, PooledEngine engine) {
        super(Family.all(CollisionComponent.class).one(PieceComponent.class, BulletComponent.class).get());

        collisionMapper = ComponentMapper.getFor(CollisionComponent.class);
        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        bulletMapper = ComponentMapper.getFor(BulletComponent.class);
        textureMapper = ComponentMapper.getFor(TextureComponent.class);


        World world = WorldFactory.getInstance(game, engine).getWorld();
        entitiesFactory = EntitiesFactory.getInstance(game, engine, world);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        CollisionComponent collisionComponent = collisionMapper.get(entity);
        Entity collidedEntity = collisionComponent.collisionEntity;
        if(collidedEntity == null) return;

        if(bulletMapper.has(entity)) {
            handleBulletCollision(entity, bulletMapper.get(entity), collidedEntity);
        } else if(bulletMapper.has(collidedEntity)) {
            handleBulletCollision(collidedEntity, bulletMapper.get(collidedEntity), entity);
        }

        collisionComponent.collisionEntity = null;

//		if(collidedEntity != null){
//			TypeComponent type = collidedEntity.getComponent(TypeComponent.class);
//			if(type != null){
//				switch(type.type){
//				case TypeComponent.ENEMY:
//					//do player hit enemy thing
//					System.out.println("player hit enemy");
//					break;
//				case TypeComponent.SCENERY:
//					//do player hit scenery thing
//					System.out.println("player hit scenery");
//					break;
//				case TypeComponent.OTHER:
//					//do player hit other thing
//					System.out.println("player hit other");
//					break; //technically this isn't needed
//				}
//				collisionComponent.collisionEntity = null; // collision handled reset component
//			}
//		}

    }

    private void handleBulletCollision(Entity bulletEntity, BulletComponent bulletComponent, Entity collidedEntity) {
        if(pieceMapper.has(collidedEntity)) {
            // bullet - piece
			PieceComponent pieceComponent = pieceMapper.get(collidedEntity);
            pieceComponent.piece.hp -= bulletComponent.damage;
            if(pieceComponent.piece.hp <= 0) {
                pieceComponent.isDead = true;
            }

            bulletComponent.isDead = true;
            textureMapper.get(bulletEntity).textureRegion = null;

//			getEngine().removeEntity(bulletEntity);
//			getEngine().removeEntity(collidedEntity);
//			System.out.println("bullet collided " + bulletComponent.createdByActorId + " actor: " + pieceComponent.piece.actorId);
//			TextureComponent textureComponent = textureMapper.get(collidedEntity);
        }
    }

}