package com.myspacegame.components.pieces;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.myspacegame.entities.Piece;

public class PieceComponent implements Component, Pool.Poolable {

    public Piece piece;
    public Fixture fixture;
    public Vector2 fixtureCenter = new Vector2();
    public boolean isDead = false;
    public boolean isManuallyDetached = false;
    public boolean toRemoveAnchors = false;

    @Override
    public void reset() {
        piece = null;
        isDead = false;
        isManuallyDetached = false;
        toRemoveAnchors = false;
    }

}
