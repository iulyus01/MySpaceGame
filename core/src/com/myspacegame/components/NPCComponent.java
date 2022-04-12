package com.myspacegame.components;

import com.badlogic.ashley.core.Component;
import com.myspacegame.Info;

public class NPCComponent implements Component {
    public Info.NPCType type = Info.NPCType.ENEMY;
}