package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.components.*;
import com.myspacegame.components.pieces.PieceComponent;

public class CollisionSystem extends IteratingSystem {

    private final ComponentMapper<CollisionComponent> collisionMapper;
    private final ComponentMapper<TextureComponent> textureMapper;
    private final ComponentMapper<BodyComponent> bodyMapper;
    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<BulletComponent> bulletMapper;
    private final ComponentMapper<RockComponent> rockMapper;

    public CollisionSystem() {
        super(Family.all(CollisionComponent.class).one(PieceComponent.class, BulletComponent.class, RockComponent.class, WallComponent.class).get());

        collisionMapper = ComponentMapper.getFor(CollisionComponent.class);
        textureMapper = ComponentMapper.getFor(TextureComponent.class);
        bodyMapper = ComponentMapper.getFor(BodyComponent.class);
        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        bulletMapper = ComponentMapper.getFor(BulletComponent.class);
        rockMapper = ComponentMapper.getFor(RockComponent.class);

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        CollisionComponent collisionComponent = collisionMapper.get(entity);
        Array<Entity> collidedEntities = collisionComponent.collisionEntities;
        if(collidedEntities.size == 0) return;

        for(int i = 0; i < collidedEntities.size; i++) {
            Entity collidedEntity = collidedEntities.get(i);
            if(bulletMapper.has(entity)) {
                handleBulletCollision(entity, bulletMapper.get(entity), collidedEntity);
            } else if(bulletMapper.has(collidedEntity)) {
                handleBulletCollision(collidedEntity, bulletMapper.get(collidedEntity), entity);
            }
            collisionComponent.collisionEntities.removeIndex(i);
            i--;
        }


//		if(collidedEntities != null){
//			TypeComponent type = collidedEntities.getComponent(TypeComponent.class);
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
        Body bulletBody = bodyMapper.get(bulletEntity).body;

        if(pieceMapper.has(collidedEntity)) {
            // bullet - piece
			PieceComponent pieceComponent = pieceMapper.get(collidedEntity);
            pieceComponent.piece.hp -= bulletComponent.damage;
            if(pieceComponent.piece.hp <= 0) {
                pieceComponent.isDead = true;
            }

            bulletBody.setLinearVelocity(0, 0);
            bulletComponent.isReadyToDie = true;
            textureMapper.get(bulletEntity).textureRegion = null;
        } else if(rockMapper.has(collidedEntity)) {
            // bullet - rock
            bulletBody.setLinearVelocity(0, 0);
            bulletComponent.isReadyToDie = true;
            textureMapper.get(bulletEntity).textureRegion = null;
        }
    }

}