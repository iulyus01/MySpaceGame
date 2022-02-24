package com.myspacegame.entities;

public class CorePiece extends Piece {

    public CorePiece(Piece piece) {
        super(piece);
    }

    public CorePiece(int x, int y, int width, int height) {
        super(x, y, width, height, true);
    }
}
