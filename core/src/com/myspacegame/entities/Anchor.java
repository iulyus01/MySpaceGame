package com.myspacegame.entities;

import com.badlogic.gdx.math.Vector2;

public class Anchor {
    public int edgeIndex;
    public int startVertex;
    public int endVertex;
    public float posRate;
    public final Vector2 pos;
    public Piece piece = null;
    public final Piece srcPiece;

    public Anchor(Piece srcPiece) {
        pos = new Vector2();
        this.srcPiece = srcPiece;
    }

    public Anchor(int edgeIndex, int startVertex, int endVertex, float posRate, Piece nextPiece, Piece srcPiece) {
        this.edgeIndex = edgeIndex;
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        this.posRate = posRate;
        this.piece = nextPiece;
        this.srcPiece = srcPiece;
        pos = new Vector2();

    }
}
