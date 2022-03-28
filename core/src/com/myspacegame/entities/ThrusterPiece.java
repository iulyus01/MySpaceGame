package com.myspacegame.entities;

import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;

public class ThrusterPiece extends Piece {

    public Info.Key activateKey;
    public int angleDirection;

    public ThrusterPiece(Piece piece, int angleDirection) {
        super(piece);
        this.angleDirection = angleDirection;
    }

    public ThrusterPiece(int x, int y, int width, int height, int angleDirection, Info.Key activateKey) {
        super(x, y, width, height, false);
        this.angleDirection = angleDirection;
        this.activateKey = activateKey;

        anchors = new Array<>(false, 1, Info.Pair.class);
        for(int i = 0; i < height; i++) {
            anchors.add(new Anchor(this));
        }
    }
}
