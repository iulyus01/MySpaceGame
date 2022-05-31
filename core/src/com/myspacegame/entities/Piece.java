package com.myspacegame.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.utils.PieceEdge;

public class Piece {

    /*
    list containing
        - actorId - id of npc or player, -1 is neutral
        - pieceConfigId - id from configs.json
        - width and height or piece
        - position of piece - integer
            - only one position - center of piece - i guess it becomes float
            - relative to core
        - shape of piece - polygon
            - check shape collision by checking polygon sides
            - middle is 0, 0
                - gotta change all other places
        - anchor positions
            - index of array from graph
            - index of start vertex of polygon
            - index of end vertex of polygon
            - position on edge between start and end (value between 0 - 1)


    graph containing
        - all the info about the piece
        - actually i think only anchors
        - and list element
        - and PieceComponent

    anchor
        -

    */
    public int actorId = -1;
    public int pieceConfigId = 0;
    public float W = 0;
    public float H = 0;
    public float hp = 3;
    public int rotation = 0;
    public Vector2 pos = null;
    public Polygon shape;
    public Array<PieceEdge> edges;
    public Array<Anchor> anchors;
    public PieceComponent pieceComponent;

    public int checked = 0;

    public Piece() {}

}
