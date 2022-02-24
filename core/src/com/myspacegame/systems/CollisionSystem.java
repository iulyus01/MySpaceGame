package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.myspacegame.components.CollisionComponent;
import com.myspacegame.components.PlayerComponent;
import com.myspacegame.components.TypeComponent;

public class CollisionSystem  extends IteratingSystem {
	 ComponentMapper<CollisionComponent> collisionMapper;
	 ComponentMapper<PlayerComponent> playerMapper;

	public CollisionSystem() {
		// only need to worry about player collisions
		super(Family.all(CollisionComponent.class, PlayerComponent.class).get());
		
		 collisionMapper = ComponentMapper.getFor(CollisionComponent.class);
		 playerMapper = ComponentMapper.getFor(PlayerComponent.class);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		// get player collision component
		CollisionComponent collisionComponent = collisionMapper.get(entity);
		
		Entity collidedEntity = collisionComponent.collisionEntity;
		if(collidedEntity != null){
			TypeComponent type = collidedEntity.getComponent(TypeComponent.class);
			if(type != null){
				switch(type.type){
				case TypeComponent.ENEMY:
					//do player hit enemy thing
					System.out.println("player hit enemy");
					break;
				case TypeComponent.SCENERY:
					//do player hit scenery thing
					System.out.println("player hit scenery");
					break;
				case TypeComponent.OTHER:
					//do player hit other thing
					System.out.println("player hit other");
					break; //technically this isn't needed				
				}
				collisionComponent.collisionEntity = null; // collision handled reset component
			}
		}
		
	}

}