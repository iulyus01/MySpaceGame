package com.myspacegame.entities;

import com.badlogic.gdx.Input;
import com.myspacegame.Info;

public class TractorBeamPiece extends Piece {

    public int activateKey;
    public float radius2;
    public float force;

    public TractorBeamPiece(float radius) {
        super();
        this.radius2 = radius * radius;
        this.force = 2;
        this.activateKey = Input.Keys.T;
    }

}
