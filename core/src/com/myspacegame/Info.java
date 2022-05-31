package com.myspacegame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.components.pieces.PieceComponent;
import com.myspacegame.entities.Piece;
import com.myspacegame.utils.PieceConfig;
import com.myspacegame.utils.PieceEdge;
import com.myspacegame.utils.RockConfig;

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
    public static final String appName = "My Space Game";
    public static final String PIECE_TEXTURE_PATH = "images/pieces/";

    // misc
    public static final int W = Gdx.graphics.getWidth();
    public static final int H = Gdx.graphics.getHeight();
    public static final float worldWidthLimit = 50f;
    public static final float worldHeightLimit = 50f;
    public static boolean speedrun = false;

    // utils
    public static float mouseWorldX;
    public static float mouseWorldY;
    public static float cameraWorldX;
    public static float cameraWorldY;
    public static final float rad15Deg = (float) Math.PI / 12;
    public static final float rad30Deg = (float) Math.PI / 6;
    public static final float rad45Deg = (float) Math.PI / 4;
    public static final float rad60Deg = (float) Math.PI / 3;
    public static final float rad90Deg = (float) Math.PI / 2;
    public static final float rad120Deg = (float) Math.PI / 1.5f;
    public static final float rad180Deg = (float) Math.PI;
    public static final float rad360Deg = (float) Math.PI * 2;
    public static Vector2 tempVector2 = new Vector2();
    public static TextureRegion testTexture;
    public static int[] movementKeys = new int[]{Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D};


    // box2D
    public static final float PPM = 24.0f;
    public static final float PIXELS_TO_METRES = 1.0f / Info.PPM;
//    public static final float blockSize = .24f;
    public static final float blockSize = .5f;
    public static final float maxPieceSize = 5 * blockSize;
    public static final float baseTeleporterRadius = 8 * blockSize;
    public static final float defaultPieceDensity = 4f;
//    public static final float defaultThrusterForce = 400f;
    public static final float defaultThrusterForce = 240f;
    public static final float defaultThrusterImpulse = 7f;
    public static final float defaultBulletImpulse = 16f;
    public static final float defaultBulletDensity = 1.8f;
    public static final float defaultPieceLinearDamping = 0.4f;
    public static final float defaultPieceAngularDamping = 5;
    public static final float defaultSinglePieceLinearDamping = 0.2f;
    public static final float defaultSinglePieceAngularDamping = 0.2f;
    public static final float defaultTractorBeamRadius = 70 * blockSize;
    public static final float defaultRockLinearDamping = .1f;
    public static final float defaultRockAngularDamping = .1f;
    public static final float maxHorVerVelocity = 20f; // maximum horizontal vertical speed

    public static final short CATEGORY_PLAYER = 0x0001;  // 0000000000000001 in binary
    public static final short CATEGORY_BULLET = 0x0002; // 0000000000000010 in binary
    public static final short CATEGORY_OTHERS = 0x0004; // 0000000000000100 in binary
    public static final short CATEGORY_PIECE = 0x0008;

    public static final short MASK_EVERYTHING = -1;
    public static final short MASK_NOTHING = 0;
    public static final short MASK_PIECE_PLAYER = ~Info.CATEGORY_PLAYER;




    public static PlayerMode activeMode;
    public static boolean playerIsDead = true;

    public static Map<Integer, PieceConfig> pieceConfigsMap;
    public static Map<Integer, RockConfig> rockShapesMap;

    // pieceId  pieceTypeId  shapeId  rotation  x  y
    // pieceId # edgeId edgeAnchorId id edgeAnchorId id # edgeId edgeAnchorId id
    public static List<String[]> ships;

    // enums
    public enum PlayerMode {
        MOVING, BUILDING
    }
    public enum EntityType {
        PIECE, BULLET, WALL, ROCK, TELEPORTER
    }
    public enum NPCType {
        ALLY, NEUTRAL, ENEMY
    }
    public enum ZOrder {
        BACKGROUND(-2),
        OTHERS(0),
        TELEPORTER(1),
        ROCK(3),
        PIECE(5),
        WEAPONS(8),
        BULLET(9),
        PIECE_DRAG(10),
        ANCHOR(15),
        HOVER_OVERLAY(20),
        WALL(30),
        GAME_OVER(40);

        private final int value;
        ZOrder(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
    public enum StaticActorIds {
        NONE(-1), PLAYER(0);

        private final int value;
        StaticActorIds(int value) {
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

        public void set(U first, V second) {
            this.first = first;
            this.second = second;
        }
    }

    public static class Triple<A, B, C> {
        public A first;
        public B second;
        public C third;

        public Triple(A first, B second, C third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }

    public static class Quintuple<A, B, C, D, E> {
        public A first;
        public B second;
        public C third;
        public D forth;
        public E fifth;

        public Quintuple(A first, B second, C third, D forth, E fifth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.forth = forth;
            this.fifth = fifth;
        }

        public void set(A first, B second, C third, D forth, E fifth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.forth = forth;
            this.fifth = fifth;
        }
    }

    public static Color colorClear;

    public static Color colorCyan;
    public static Color colorBlueLighten5;
    public static Color colorBlue;
    public static Color colorBlueDarken4;
    public static Color colorRed;
    public static Color colorPurple;

    public static void init() {
        colorClear = new Color(0.137f, 0.121f, 0.125f, 1);

        colorCyan = new Color(.160f, .713f, .964f, 1);
        colorBlue = new Color(.129f, .588f, .952f, 1);
        colorBlueLighten5 = new Color(.89f, .949f, .992f, 1);
        colorBlueDarken4 = new Color(.05f, .278f, .631f, 1);
        colorRed = new Color(.937f, .325f, .313f, 1);
        colorPurple = new Color(1, .3f, .95f, 1);



        activeMode = PlayerMode.MOVING;
        testTexture = new TextureRegion(new Texture("badlogic.jpg"));
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

    public static Array<PieceEdge> configEdgesToComputedEdges(List<PieceEdge> edges, float multiplication) {
        // this basically makes the edge's size relative to block's size (block size as the multiplication)
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

    public static void rotateEdges(Array<PieceEdge> edges, float angleRad) {
        float sin = MathUtils.sin(angleRad);
        float cos = MathUtils.cos(angleRad);
        float xNew;
        float yNew;
        for(PieceEdge edge : edges) {
            xNew = Math.round((edge.x1 * cos - edge.y1 * sin) * 1000) / 1000f;
            yNew = Math.round((edge.x1 * sin + edge.y1 * cos) * 1000) / 1000f;
            edge.x1 = xNew;
            edge.y1 = yNew;

            xNew = Math.round((edge.x2 * cos - edge.y2 * sin) * 1000) / 1000f;
            yNew = Math.round((edge.x2 * sin + edge.y2 * cos) * 1000) / 1000f;
            edge.x2 = xNew;
            edge.y2 = yNew;
        }
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

    public static String getPieceName(Piece piece) {
        if(piece == null) return "null";
        return Info.pieceConfigsMap.get(piece.pieceConfigId).name;
    }

    public static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    public static void rotatePointBy(Vector2 point, float angleRad) {
        float sin = MathUtils.sin(angleRad);
        float cos = MathUtils.cos(angleRad);

        float newX = point.x * cos - point.y * sin;
        float newY = point.x * sin + point.y * cos;

        point.x = newX;
        point.y = newY;
    }

    public static int parseFirstInteger(String nrS) {
        int nr = 0;
        for(int i = 0; i < nrS.length(); i++) {
            if(nrS.charAt(i) < '0' || nrS.charAt(i) > '9') return nr;
            nr = nr * 10 + (nrS.charAt(i) - '0');
        }
        return nr;
    }
/*

    TODO make thruster image a full piece.. to be able to attach more one to other
    TODO change thruster fixture shape


    TODO nice to make enemies autogenerated
    TODO make a maze or something similar, with more and more advanced enemies till you get to the end
    TODO story? you were lost in a mission to a far away station and now you have to get back to the teleporter and go back home
    enemies are more and more powerful the further you go, you have to efficiently destroy enemies to obtain more pieces
    to be able to progress further

    what is this? and how does it work?
    bodyComponent.body.setLinearVelocity(MathUtils.lerp(bodyComponent.body.getLinearVelocity().x, 0, 0.1f), bodyComponent.body.getLinearVelocity().y);

    piece
        - ship piece components:
            1. TransformComponent
            2. TextureComponent
            3. PieceComponent
            4. ShipComponent
            5. Component specificPieceComponent
            6. CollisionComponent
            7. PlayerComponent / NPCComponent
            8.? ShipCoreComponent
        - alone piece components:
            1. TransformComponent
            2. TextureComponent
            3. PieceComponent
            4. Component specificPieceComponent
            5. CollisionComponent





*/



}
