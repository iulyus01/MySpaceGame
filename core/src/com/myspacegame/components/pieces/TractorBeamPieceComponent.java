package com.myspacegame.components.pieces;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.myspacegame.entities.TractorBeamPiece;

public class TractorBeamPieceComponent implements Component, Pool.Poolable {

    public TractorBeamPiece piece;
    public float prepareToPullDelay = 0;
    public float prepareToPullDelayMax = .3f;
    public float ceasePullDelay = 0;
    public float ceasePullDelayMax = .5f;
    // 0 -  inactive, 1 - preparing, 2 - pulling, 3 - ceasing
    public int state = 0;
    public boolean active = false;


    @Override
    public void reset() {
        prepareToPullDelay = 0;
        ceasePullDelay = 0;
        state = 0;
        active = false;
    }
}
