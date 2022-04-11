package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.myspacegame.entities.Anchor;
import com.myspacegame.entities.Piece;

public class AnchorComponent implements Component {
    public Anchor anchor;
    public Piece piece = null;
    public boolean active = true;
    public boolean toRemove = false;
}