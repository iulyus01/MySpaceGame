package com.myspacegame.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
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
    public float hp = 5;
    public int rotation = 0;
    public Vector2 pos = null;
    public Polygon shape;
    public Array<PieceEdge> edges;
    public Array<Anchor> anchors;
    public PieceComponent pieceComponent;

    public int checked = 0;


    public Piece() {

    }

    public Piece(Piece piece) {
        this.pieceConfigId = piece.pieceConfigId;
        this.W = piece.W;
        this.H = piece.H;
        this.pos = new Vector2(piece.pos);
        this.shape = new Polygon(piece.shape.getVertices().clone());
        // TODO not sure if this case exists, where the anchors and PieceComponent are the same
        this.anchors = piece.anchors;
        this.pieceComponent = piece.pieceComponent;
    }

    public Piece(int x, int y, int width, int height, boolean defaultAnchors) {
//        this.x = x;
//        this.y = y;
//        this.width = width;
//        this.height = height;
//
//        if(width != height) this.angleToCenterRad = (float) Math.atan2(height, 2);
//        else this.angleToCenterRad = 0.785f;
//        this.diagonalLength = (float) Math.sqrt((Info.blockSize * width) * (Info.blockSize * width) + (Info.blockSize * height) * (Info.blockSize * height));
//
//        if(defaultAnchors) {
//            anchors = new Array<>(false, (width + height) * 2, Info.Pair.class);
//            for(int i = 0; i < width; i++) {
//                anchors.add(new Info.Pair<>(new Info.Anchor(i, 0, 1), true));
//                anchors.add(new Info.Pair<>(new Info.Anchor(i, height - 1, 3), true));
//            }
//            for(int i = 0; i < height; i++) {
//                anchors.add(new Info.Pair<>(new Info.Anchor(0, i, 2), true));
//                anchors.add(new Info.Pair<>(new Info.Anchor(width - 1, i, 0), true));
//            }
//        }
    }

}
