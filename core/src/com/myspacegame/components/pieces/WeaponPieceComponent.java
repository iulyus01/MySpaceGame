package com.myspacegame.components.pieces;

import com.badlogic.ashley.core.Component;
import com.myspacegame.entities.HullPiece;
import com.myspacegame.entities.WeaponPiece;

public class WeaponPieceComponent implements Component {

    public WeaponPiece piece;
    public float reloadingTimeMax = .2f;
    public float reloadingTime = 0;
    public boolean isReloading = false;


}
