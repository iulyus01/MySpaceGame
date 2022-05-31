package com.myspacegame.utils;

public class Functions {

    public static float rotatingThrustersFunction(float x) {
        if(x > .3f) return 1;
        if(x > .2f) return .8f;
        if(x > .1f) return .5f;
        if(x > .05f) return .1f;
        return .01f;
    }

}
