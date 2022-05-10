package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.myspacegame.entities.Anchor;
import com.myspacegame.entities.Piece;

public class AnchorComponent implements Component, Pool.Poolable {
    public Anchor anchor;
    public Piece piece = null;
    public boolean active = true;
    public boolean toRemove = false;

    @Override
    public void reset() {
        piece = null;
        active = true;
        toRemove = false;
    }
}