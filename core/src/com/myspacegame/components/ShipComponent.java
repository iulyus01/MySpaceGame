package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.entities.Piece;

public class ShipComponent implements Component {

    public Piece core;
    public Array<Piece> piecesArray;



    public Array<Array<Piece>> ship;
    public int width = 1;
    public int height = 1;

}
