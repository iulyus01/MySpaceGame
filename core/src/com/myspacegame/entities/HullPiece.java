package com.myspacegame.entities;

import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;

public class HullPiece extends Piece {

    public HullPiece(Piece piece) {
        super(piece);
    }

    public HullPiece(int x, int y, int width, int height) {
        super(x, y, width, height, true);
    }

    public HullPiece(int x, int y, int width, int height, Array<Anchor> anchors) {
        super(x, y, width, height, true);
        this.anchors = anchors;
    }

}
