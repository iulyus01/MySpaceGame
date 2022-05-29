package com.myspacegame.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;

public class Area {
    public int id;
    public int difficulty;
    public Vector2 mapPos;
    public Array<Info.Pair<Area, Vector2>> to;

    public Area(int id, int difficulty, Vector2 mapPos) {
        this.id = id;
        this.difficulty = difficulty;
        this.mapPos = mapPos;
        this.to = new Array<>(true, 6, Info.Pair.class);
    }
}
