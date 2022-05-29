package com.myspacegame.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.ObjectSet;
import com.myspacegame.components.BodyComponent;
import com.myspacegame.components.RockComponent;
import com.myspacegame.components.ShipComponent;
import com.myspacegame.components.TransformComponent;

public class ShipSystem extends IteratingSystem {

    private final ComponentMapper<ShipComponent> shipMapper;
    private final ObjectSet<ShipComponent> temp;

    public ShipSystem() {
        super(Family.all(ShipComponent.class).get());

        shipMapper = ComponentMapper.getFor(ShipComponent.class);
        temp = new ObjectSet<>();
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
    }

}
