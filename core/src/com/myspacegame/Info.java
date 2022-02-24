package com.myspacegame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.myspacegame.components.pieces.PieceComponent;

import java.util.Map;

public class Info {

    /*

    TODO info here about implementation
    in pieces.json
        - order is: bottom-right → top-right → top-left → bottom-left
        - startIndices remembers the start index of the edge where anchors are placed
        - startIndices remembers the end index of the edge where anchors are placed

        - pieces ids
            - 0: important
            - 1: hulls
            - 2: thrusters
            - 3: weapons



    */

    public static final String appName = "My Space Game (Temp)";

    public static int W = Gdx.graphics.getWidth();
    public static int H = Gdx.graphics.getHeight();

    public static final float PPM = 64.0f;
    public static final float PIXELS_TO_METRES = 1.0f / Info.PPM;

    public static float mouseWorldX;
    public static float mouseWorldY;
    public static float cameraWorldX;
    public static float cameraWorldY;

    public static final float blockSize = .24f;
    public static final float maxPieceSize = 5 * blockSize;
    public static final float defaultThrusterForce = 14f;
    public static final float defaultBulletImpulse = .022f;
    public static final float defaultBulletDensity = .2f;
//    public static final float defaultPieceLinearDamping = .2f;
//    public static final float defaultPieceAngularDamping = .6f;
    public static final float defaultPieceLinearDamping = .01f;
    public static final float defaultPieceAngularDamping = 0;

    public static final float tempBlockDiagonal = blockSize * 1.414213f;
    public static final float rad90Deg = 1.570f;


    public static PieceComponent temp = null;


    public static final short CATEGORY_PLAYER = 0x0001;  // 0000000000000001 in binary
    public static final short CATEGORY_BULLET = 0x0002; // 0000000000000010 in binary
    public static final short CATEGORY_OTHERS = 0x0004; // 0000000000000100 in binary
    public static final short CATEGORY_PIECE = 0x0008;

    public static final short MASK_EVERYTHING = -1;
    public static final short MASK_NOTHING = 0;
    public static final short MASK_PIECE_PLAYER = ~Info.CATEGORY_PLAYER;

    public static PlayerMode activeMode;

    public static class PieceConfig {
        public int id;
        public String name;
        public int width;
        public int height;
        public String textureName;
        public float[] vertices;
        public int[] startIndices;
        public int[] endIndices;
        public float[] posRatioList;
    }

    public static Map<Integer, PieceConfig> pieceConfigsMap;

    public static final String[] newShip0 = {
//            "c pos.x: 0 pos.y: 0 W: 1*block H: 1*block shapeId: block1x1",
            "c 0 0 1 1 block1x1",
            "",
            "c 1 - - - -",
    };

    public static final String[] newShip1 = {
//            "c pos.x: 0 pos.y: 0 W: 1*block H: 1*block shapeId: block1x1",
            "c 0 0 1 1 block1x1",
            "h 1 0 1 1 block1x1",
            "h 0 1 1 1 block1x1",
            "h 0 -1 1 1 block1x1",
            "h 2 0 1 1 block1x1",
            "w 1 1 1 1 block1x1",
            "w 1 -1 1 1 block1x1",
            "h 0 2 1 1 block1x1",
            "h -1 1 1 1 block1x1", // 8
            "h -1 -1 1 1 block1x1", // 9
            "h 0 -2 1 1 block1x1",
            "h 3 0 1 1 block3x1",
            "t3D 2 1 1 1 block1x1",
            "t1A 2 -1 1 1 block1x1",
            "t3A -1 2 1 1 block1x1",
            "t0W -2 1 1 1 block1x1", // 15
            "t0W -2 -1 1 1 block1x1", // 16
            "t1D -1 -2 1 1 block1x1",
            "t0W -1 0 1 1 block1x1",
            "t0W -2 0 1 1 block1x1",
            "",
            "0 1 2 18 3",
            "1 4 5 0 6",
            "2 5 7 8 0",
            "3 6 0 9 10",
            "4 11 12 1 13",
            "5 12 - 2 1",
            "6 13 1 3 -",
            "7 - - 14 2",
            "8 2 14 15 18",
            "9 3 18 16 17",
            "10 - 3 17 -",
            "11 - - 4 -",
            "12 - - 5 4",
            "13 - 4 6 -",
            "14 7 - - 8",
            "15 8 - - 19",
            "16 9 19 - -",
            "17 10 9 - -",
            "18 0 8 19 9",
            "19 18 15 - 16"
    };

    public static final String[] newNewShip1 = {
//            "pieceTypeId: 0, x: 0, y: 0, shapeId: 0",
            "0 0 0 1",
            "1 1 0 1",
            "1 0 1 1",
            "1 0 -1 1",
            "1 2 0 1",
            "3 1 1 1",
            "3 1 -1 1",
            "1 0 2 1",
            "1 -1 1 1", // 8
            "1 -1 -1 1", // 9
            "1 0 -2 1",
            "1 4 0 3",
            "23D 2 1 1",
            "21A 2 -1 1",
            "23A -1 2 1",
            "20W -2 1 1", // 15
            "20W -2 -1 1", // 16
            "21D -1 -2 1",
            "20W -1 0 1",
            "20W -2 0 1",
            "",
            "0 1 2 18 3",
            "1 4 5 0 6",
            "2 5 7 8 0",
            "3 6 0 9 10",
            "4 11 12 1 13",
            "5 12 - 2 1",
            "6 13 1 3 -",
            "7 - - 14 2",
            "8 2 14 15 18",
            "9 3 18 16 17",
            "10 - 3 17 -",
            "11 - - - - 4 - - -",
            "12 - - 5 4",
            "13 - 4 6 -",
            "14 7 - - 8",
            "15 8 - - 19",
            "16 9 19 - -",
            "17 10 9 - -",
            "18 0 8 19 9",
            "19 18 15 - 16"
    };

    public enum PlayerMode {
        MOVING, BUILDING
    }

    public enum EntityType {
        PIECE, BULLET
    }

    public enum Key {
        W, A, S, D
    }

    public enum ZOrder {
        OTHERS(0), WEAPONS(1), BULLETS(2), PIECE_DRAG(3);

        private final int value;

        ZOrder(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static class Pair<U, V> {
        public U first;
        public V second;

        public Pair(U first, V second) {
            this.first = first;
            this.second = second;
        }
    }

    public static Color colorClear;

    public static Color colorCyan;
    public static Color colorBlueLighten5;
    public static Color colorBlue;
    public static Color colorBlueDarken4;
    public static Color colorRed;

    public static void init() {
        colorClear = new Color(.184f, .215f, .258f, .4f);

        colorCyan = new Color(.160f, .713f, .964f, 1);
        colorBlue = new Color(.129f, .588f, .952f, 1);
        colorBlueLighten5 = new Color(.89f, .949f, .992f, 1);
        colorBlueDarken4 = new Color(.05f, .278f, .631f, 1);
        colorRed = new Color(.937f, .325f, .313f, 1);



        activeMode = PlayerMode.MOVING;
    }

    public static void computePieceFixtureCenter(PieceComponent pieceComponent) {
//        float angle = fixture.getBody().getAngle();

//        fixtureCenter.x = fixtureBottomLeft.x + piece.diagonalLength / 2f * (float) Math.cos(angle + piece.angleToCenterRad);
//        fixtureCenter.y = fixtureBottomLeft.y + piece.diagonalLength / 2f * (float) Math.sin(angle + piece.angleToCenterRad);

        Transform transform = pieceComponent.fixture.getBody().getTransform();
        pieceComponent.fixtureCenter.x = pieceComponent.piece.pos.x * Info.blockSize;
        pieceComponent.fixtureCenter.y = pieceComponent.piece.pos.y * Info.blockSize;
        transform.mul(pieceComponent.fixtureCenter);
    }

    public static void computePieceFixtureBottomLeft(Vector2 fixtureBottomLeft, Fixture fixture) {
        Transform transform = fixture.getBody().getTransform();
        PolygonShape shape = ((PolygonShape) fixture.getShape());
        shape.getVertex(shape.getVertexCount() - 1, fixtureBottomLeft);
        transform.mul(fixtureBottomLeft);
        // TODO this is broken rn
    }

    public static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

}
