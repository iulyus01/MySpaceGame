package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.entities.Piece;

public class ShipComponent implements Component {

    public Piece core;
    public Array<Piece> piecesArray;
    public float localCenterAngle0X;
    public float localCenterAngle0Y;

}
