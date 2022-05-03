package com.myspacegame.entities;

public class WeaponPiece extends Piece {

    public float angleRad = 0;
    public float reloadingTimeMax = .2f;
    public float reloadingTime = 0;
    public boolean fixedAngle = false;
    public boolean isReloading = false;

    public WeaponPiece() {
        super();
    }
}
