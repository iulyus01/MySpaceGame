package com.myspacegame.entities;

import com.badlogic.gdx.math.Vector2;
import com.myspacegame.components.AnchorComponent;

public class Anchor {

    public AnchorComponent anchorComponent;
    public Piece piece = null;
    public final Piece srcPiece;
    public final Vector2 pos;
    public int edgeIndex;
    public int edgeAnchorIndex;

    public Anchor(Piece srcPiece) {
        pos = new Vector2();
        this.srcPiece = srcPiece;
    }

    public Anchor(int edgeIndex, int edgeAnchorIndex, Piece destPiece, Piece srcPiece) {
        this.edgeIndex = edgeIndex;
        this.edgeAnchorIndex = edgeAnchorIndex;
        this.piece = destPiece;
        this.srcPiece = srcPiece;
        pos = new Vector2();

    }
}
