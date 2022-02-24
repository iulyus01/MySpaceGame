package com.myspacegame.entities;

public class WeaponPiece extends Piece {

    public float angleRad;
    public boolean fixedAngle = false;

    public WeaponPiece(Piece piece) {
        super(piece);
    }

    public WeaponPiece(int x, int y, int width, int height) {
        super(x, y, width, height, true);
    }
}
