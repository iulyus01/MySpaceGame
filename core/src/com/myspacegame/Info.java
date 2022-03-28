package com.myspacegame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.entities.Piece;
import com.myspacegame.utils.PieceConfig;
import com.myspacegame.utils.PieceEdge;

import java.util.List;
import java.util.Map;

public class Info {

    /*

    TODO info here about implementation
    in pieces.json
        - order is: bottom-right → top-right → top-left → bottom-left
        - startIndices remembers the start index of the edge where anchors are placed
        - startIndices remembers the end index of the edge where anchors are placed

        - piece type ids:
            - 0: important
            - 1: hulls
            - 2: thrusters
            - 3: weapons



    */

    // strings
    public static final String appName = "My Space Game (Temp)";
    public static final String PIECE_TEXTURE_PATH = "images/pieces/";

    // misc
    public static final int W = Gdx.graphics.getWidth();
    public static final int H = Gdx.graphics.getHeight();

    // utils
    public static float mouseWorldX;
    public static float mouseWorldY;
    public static float cameraWorldX;
    public static float cameraWorldY;
    public static Vector2 tempVector2 = new Vector2();

    // box2D
    public static final float PPM = 24.0f;
    public static final float PIXELS_TO_METRES = 1.0f / Info.PPM;
//    public static final float blockSize = .24f;
    public static final float blockSize = .5f;
    public static final float maxPieceSize = 5 * blockSize;
//    public static final float defaultThrusterForce = 14f;
    public static final float defaultThrusterForce = 140f;
    public static final float defaultThrusterImpulse = 4f;
    public static final float defaultBulletImpulse = 1.8f;
    public static final float defaultBulletDensity = 1.2f;
    public static final float defaultPieceLinearDamping = .01f;
    public static final float defaultPieceAngularDamping = 0;
    public static final float maxHorVerVelocity = 8f; // maximum horizontal vertical speed

    public static final float tempBlockDiagonal = blockSize * 1.414213f;
    public static final float rad90Deg = 1.570f;



    public static final short CATEGORY_PLAYER = 0x0001;  // 0000000000000001 in binary
    public static final short CATEGORY_BULLET = 0x0002; // 0000000000000010 in binary
    public static final short CATEGORY_OTHERS = 0x0004; // 0000000000000100 in binary
    public static final short CATEGORY_PIECE = 0x0008;

    public static final short MASK_EVERYTHING = -1;
    public static final short MASK_NOTHING = 0;
    public static final short MASK_PIECE_PLAYER = ~Info.CATEGORY_PLAYER;

    public static PlayerMode activeMode;

    public static Map<Integer, PieceConfig> pieceConfigsMap;

    public static final String[] newNewShip1 = {
//            "pieceTypeId: 0, x: 0, y: 0, shapeId: 0",
            "0 0 0 0",
            "1 1 0 1",
            "1 0 1 1",
            "1 0 -1 1",
            "1 2 0 1",
            "3 1 1 5",
            "3 1 -1 5",
            "1 0 2 1",
            "1 -1 1 1", // 8
            "1 -1 -1 1", // 9
            "1 0 -2 1",
            "1 4 0 3",
            "23D 2 1 4",
            "21A 2 -1 4",
            "23A -1 2 4",
            "20W -2 1 4", // 15
            "20W -2 -1 4", // 16
            "21D -1 -2 4",
            "20W -1 0 4",
            "20W -2 0 4",
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
    public static final String[] newNewNewShip1 = {
            // pieceTypeId: 0, shapeId: 0, rotation: 0, x: 0, y: 0
            "0 0 0 0 0 0",
            "1 1 1 0 1 0",
            "2 1 1 0 0 1",
            "3 1 1 0 0 -1",
            "4 1 1 0 2 0",
            "5 3 4 0 1 1",
            "6 3 4 0 1 -1",
            "7 1 1 0 0 2",
            "8 1 1 0 -1 1",
            "9 1 1 0 -1 -1",
            "10 1 1 0 0 -2",
            "11 1 2 0 4 0",
            "12 2D 3 3 2 1",
            "13 2A 3 1 2 -1",
            "14 2A 3 3 -1 2",
            "15 2W 3 0 -2 1",
            "16 2W 3 0 -2 -1",
            "17 2D 3 1 -1 -2",
            "18 2W 3 0 -1 0",
            "",
            // pieceId # edgeId edgeAnchorId id edgeAnchorId id # edgeId edgeAnchorId id
            "0 # 0 0 1 # 1 0 2 # 2 0 18 # 3 0 3",
            "1 # 0 0 4 # 1 0 5 # 2 0 0 # 3 0 6",
            "2 # 0 0 5 # 1 0 7 # 2 0 8 # 3 0 0",
            "3 # 0 0 6 # 1 0 0 # 2 0 9 # 3 0 10",
            "4 # 0 0 11 # 1 0 12 # 2 0 1 # 3 0 13",
            "5 # 0 0 - # 1 0 - # 2 0 2 # 3 0 1",
            "6 # 0 0 - # 1 0 1 # 2 0 3 # 3 0 -",
            "7 # 0 0 - # 1 0 - # 2 0 - # 3 0 2",
            "8 # 0 0 2 # 1 0 14 # 2 0 15 # 3 0 -",
            "9 # 0 0 3 # 1 0 - # 2 0 16 # 3 0 17",
            "10 # 0 0 - # 1 0 3 # 2 0 - # 3 0 -",
            "11 # 0 0 - # 1 0 - 1 - 2 - # 2 0 4 # 3 0 - 1 - 2 - ",
            "12 # 0 0 4",
            "13 # 0 0 4",
            "14 # 0 0 8",
            "15 # 0 0 8",
            "16 # 0 0 9",
            "17 # 0 0 9",
            "18 # 0 0 0",
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

    public static class Quadruple<A, B, C, D> {
        public A first;
        public B second;
        public C third;
        public D forth;

        public Quadruple(A first, B second, C third, D forth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.forth = forth;
        }

        public void set(A first, B second, C third, D forth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.forth = forth;
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

    public static float[] edgesToNewVerticesArray(List<PieceEdge> edges, float multiplication) {
        float[] vertices = new float[(edges.size() + 1) * 2];
        int i = 0;
        int k = 0;
        vertices[i++] = edges.get(k).x1 * multiplication;
        vertices[i++] = edges.get(k).y1 * multiplication;
        for(; k < edges.size(); k++) {
            vertices[i++] = edges.get(k).x2 * multiplication;
            vertices[i++] = edges.get(k).y2 * multiplication;
        }
        return vertices;
    }

    public static Array<PieceEdge> edgesToComputedEdges(List<PieceEdge> edges, float multiplication) {
        Array<PieceEdge> newEdges = new Array<>(false, edges.size(), PieceEdge.class);
        for(PieceEdge edge : edges) {
            PieceEdge newPieceEdge = new PieceEdge();
            newPieceEdge.x1 = edge.x1 * multiplication;
            newPieceEdge.y1 = edge.y1 * multiplication;
            newPieceEdge.x2 = edge.x2 * multiplication;
            newPieceEdge.y2 = edge.y2 * multiplication;
            newPieceEdge.anchorRatios = new Array<>();
            for(Float ratio : edge.anchorRatios) {
                newPieceEdge.anchorRatios.add(ratio);
            }
            newEdges.add(newPieceEdge);
        }
        return newEdges;
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

    public static String getPieceName(Piece piece) {
        if(piece == null) return "null";
        return Info.pieceConfigsMap.get(piece.pieceConfigId).name;
    }

    public static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

}
