package com.myspacegame.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
import com.myspacegame.components.pieces.PieceComponent;

public class Piece {

    /*
    list containing
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

    public int pieceConfigId = 0;
    public float W = 0;
    public float H = 0;
    public Vector2 pos;
    public Polygon shape;
    public Array<Anchor> anchors;

//    public Array<Piece> newAnchors;
    public PieceComponent newPieceComponent;






    // these are used in the matrix
//    public int x;
//    public int y;
//    public int width;
//    public int height;
//    public Array<Info.Pair<Info.Anchor, Boolean>> anchors; // x - matrix(width X height) x, y - matrix y, z - right top left bottom
//    public final float angleToCenterRad; // 45deg
//    public final float diagonalLength;

    public PieceComponent pieceComponent;


    public Piece() {

    }

    public Piece(Piece piece) {
//        this.x = piece.x;
//        this.y = piece.y;
//        this.width = piece.width;
//        this.height = piece.height;
        this.anchors = piece.anchors;
//        this.angleToCenterRad = piece.angleToCenterRad;
//        this.diagonalLength = piece.diagonalLength;
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
