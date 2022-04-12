package com.myspacegame.factories;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.pieces.*;
import com.myspacegame.entities.*;
import com.myspacegame.components.*;

import java.util.*;

public class EntitiesFactory {

    private static EntitiesFactory instance = null;
    private final MainClass game;
    private final Engine engine;
    private final BodyFactory bodyFactory;

    private final TextureRegion anchorTexture;
    private final TextureRegion pieceHoverTexture;

    private EntitiesFactory(MainClass game, Engine engine, World world) {
        this.game = game;
        this.engine = engine;

        bodyFactory = BodyFactory.getInstance(world);
        anchorTexture = new TextureRegion(game.assetManager.get("images/anchor.png", Texture.class));
        pieceHoverTexture = new TextureRegion(game.assetManager.get("images/hover.png", Texture.class));
    }

    public static EntitiesFactory getInstance(MainClass game, Engine engine, World world) {
        if(instance == null) instance = new EntitiesFactory(game, engine, world);
        return instance;
    }

    public static EntitiesFactory get() {
        return instance;
    }

    public List<Entity> createShip(String[] shipString, float shipX, float shipY, int actorId, boolean isPlayer) {
        ShipComponent shipComponent = engine.createComponent(ShipComponent.class);
        extractShipString(shipString, shipComponent, actorId);

        Body body = bodyFactory.createPieceBody(shipX, shipY, 0);

        List<Entity> entities = new ArrayList<>();

        buildShipEntities(entities, shipComponent, body, isPlayer);

        return entities;
    }

    public Entity createWall(float x, float y, float width, float height) {
        Entity entity = engine.createEntity();
        TextureComponent textureComponent = createTextureComponent("badlogic.jpg");
        TransformComponent transformComponent = createTransformComponent(width, height, 0, 0, textureComponent, Info.ZOrder.WALL);
        transformComponent.position.x = x;
        transformComponent.position.y = y;
        Body body = bodyFactory.createWallBody(x, y, width, height, entity);
        BodyComponent bodyComponent = createBodyComponent(body);
        CollisionComponent collisionComponent = createCollisionComponent();

        entity.add(bodyComponent);
        entity.add(textureComponent);
        entity.add(transformComponent);
        entity.add(collisionComponent);

        return entity;
    }

    public Entity createPiece(boolean random, float bodyX, float bodyY) {
        // TODO make this better
        if(random) {
            bodyX = (float) Math.random() * 40 - 20;
            bodyY = (float) Math.random() * 40 - 20;
        }
        Body body = bodyFactory.createPieceBody(bodyX, bodyY, 0);

        int pieceX = 0;
        int pieceY = 0;
        int pieceWidth = 1;
        int pieceHeight = 1;
        float angleOrientationRad = 0;

        Entity entity = engine.createEntity();

        TextureComponent textureComponent = createTextureComponent("images/hull.png");
        TransformComponent transformComponent = createTransformComponent(Info.blockSize * pieceWidth, Info.blockSize * pieceHeight, 0, angleOrientationRad, textureComponent, Info.ZOrder.PIECE);
        PieceComponent simplePieceComponent = createPieceComponent(pieceX, pieceY, pieceWidth, pieceHeight);
        simplePieceComponent.fixture = bodyFactory.createPieceFixture(body, simplePieceComponent.piece, entity);
        HullPieceComponent pieceComponent = createHullPieceComponent(simplePieceComponent.piece);
        CollisionComponent collisionComponent = createCollisionComponent();

        simplePieceComponent.piece.pieceComponent = simplePieceComponent;

        entity.add(simplePieceComponent);
        entity.add(pieceComponent);
        entity.add(transformComponent);
        entity.add(textureComponent);
        entity.add(collisionComponent);

        return entity;
    }

    public Entity createBullet(int actorId, float x, float y, float angleRad) {
        float sizeWidth = Info.blockSize * 5f / 2;
        float sizeHeight = sizeWidth * .65f;

        x += Math.cos(angleRad) * sizeWidth / 2;
        y += Math.sin(angleRad) * sizeWidth / 2;

        Entity entity = engine.createEntity();
        TextureComponent textureComponent = createTextureComponent("images/bullet.png");
        BulletComponent bulletComponent = createBulletComponent(actorId);
        TransformComponent transformComponent = createTransformComponent(sizeWidth, sizeHeight, angleRad, 0, textureComponent, Info.ZOrder.BULLETS);
        Body body = bodyFactory.createBulletBody(x, y, Info.blockSize * 2, Info.blockSize / 2, angleRad, Info.defaultBulletImpulse, entity);
        BodyComponent bodyComponent = createBodyComponent(body);
        CollisionComponent collisionComponent = createCollisionComponent();

        entity.add(bulletComponent);
        entity.add(textureComponent);
        entity.add(transformComponent);
        entity.add(bodyComponent);
        entity.add(collisionComponent);

        return entity;
    }

    public Entity createAnchorEntity(PieceComponent pieceComponent, Anchor anchor, boolean active) {
        Entity entity = engine.createEntity();

        float size = Info.blockSize / 6;
        AnchorComponent anchorComponent = engine.createComponent(AnchorComponent.class);
        anchorComponent.anchor = anchor;
        anchorComponent.piece = pieceComponent.piece;
        anchorComponent.active = active;
        TextureComponent textureComponent = engine.createComponent(TextureComponent.class);
        textureComponent.textureRegion = anchorTexture;
        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
        transformComponent.width = size;
        transformComponent.height = size;
        transformComponent.position.x = anchor.pos.x;
        transformComponent.position.y = anchor.pos.y;
        transformComponent.position.z = Info.ZOrder.ANCHOR.getValue();
        transformComponent.scale.x = transformComponent.width / anchorTexture.getRegionWidth();
        transformComponent.scale.y = transformComponent.height / anchorTexture.getRegionHeight();
        transformComponent.isHidden = (Math.abs(pieceComponent.fixtureCenter.x - Info.mouseWorldX) > Info.blockSize * 20 || Math.abs(pieceComponent.fixtureCenter.y - Info.mouseWorldY) > Info.blockSize * 20);

        anchor.anchorComponent = anchorComponent;

        entity.add(anchorComponent);
        entity.add(textureComponent);
        entity.add(transformComponent);

        return entity;
    }

    public Entity createPieceHoverEntity() {
        Entity entity = engine.createEntity();

        TextureComponent textureComponent = engine.createComponent(TextureComponent.class);
        textureComponent.textureRegion = pieceHoverTexture;
        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
        transformComponent.position.z = Info.ZOrder.HOVER_OVERLAY.getValue();

        entity.add(textureComponent);
        entity.add(transformComponent);

        return entity;
    }

    public void removeAnchorEntity(Anchor anchor) {
        if(anchor.anchorComponent == null) return;
        anchor.anchorComponent.toRemove = true;
        System.out.println("test remove anchor entity");
    }

    public void removePieceHoverEntity(Entity entity) {
        engine.removeEntity(entity);
    }




    private Piece getPieceType(String type, int rotation, ShipComponent shipComponent) {
        Piece piece;
        switch(type.charAt(0)) {
            case '0':
                piece = new CorePiece(0, 0, 0, 0);
                shipComponent.core = piece;
                break;
            case '1': default:
                piece = new HullPiece(0, 0, 0, 0);
                break;
            case '2':
                Info.Key[] possibleKeys = new Info.Key[]{Info.Key.W, Info.Key.A, Info.Key.S, Info.Key.D};
                Info.Key key = possibleKeys["WASD".indexOf(type.charAt(1))];
                piece = new ThrusterPiece(0, 0, 0, 0, rotation, key);
                break;
            case '3':
                piece = new WeaponPiece(0, 0, 0, 0);
                break;
        }
        return piece;
    }

    private void extractShipString(String[] shipString, ShipComponent shipComponent, int actorId) {
        int capacity = 0;
        for(String s : shipString) {
            if(s.length() == 0) break;
            capacity++;
        }

        // create pieces array
        shipComponent.piecesArray = new Array<>(false, capacity, Piece.class);
        for(int i = 0; i < capacity; i++) shipComponent.piecesArray.add(null);
        for(int i = 0; i < capacity; i++) {
            String[] spliced = shipString[i].split("\\s+");
            int pieceIndex = Integer.parseInt(spliced[0]);
            int rotation = Integer.parseInt(spliced[3]);
            Piece toAddPiece = getPieceType(spliced[1], rotation, shipComponent);

            int pieceConfigId = Integer.parseInt(spliced[2]);


            toAddPiece.actorId = actorId;
            toAddPiece.pieceConfigId = pieceConfigId;
            toAddPiece.W = Info.blockSize * Info.pieceConfigsMap.get(pieceConfigId).width;
            toAddPiece.H = Info.blockSize * Info.pieceConfigsMap.get(pieceConfigId).height;
            toAddPiece.rotation = rotation;
            toAddPiece.pos = new Vector2(Integer.parseInt(spliced[4]), Integer.parseInt(spliced[5]));
            toAddPiece.shape = new Polygon(Info.edgesToNewVerticesArray(Info.pieceConfigsMap.get(pieceConfigId).edges, Info.blockSize));
            toAddPiece.edges = Info.edgesToComputedEdges(Info.pieceConfigsMap.get(pieceConfigId).edges, Info.blockSize);

            // if pieceIndex is higher than piecesNr it will crash
            // TODO not sure if i have to treat this case ^^
            shipComponent.piecesArray.set(pieceIndex, toAddPiece);
        }
        // set pos: 0, 0 to core
        shipComponent.piecesArray.get(0).pos = new Vector2(0, 0);

        // create anchors graph
        for(int i = capacity + 1; i < shipString.length; i++) {
            String[] firstSplice = shipString[i].split("\\s+#+\\s+");
            int spliceI = 0;
            int pieceIndex = Integer.parseInt(firstSplice[spliceI++]);

            Piece piece = shipComponent.piecesArray.get(pieceIndex);
            piece.anchors = new Array<>();

            // go through edges
            for(; spliceI < firstSplice.length; spliceI++) {
                String[] spliced = firstSplice[spliceI].split("\\s+");
                int j = 0;
                int edgeId = Integer.parseInt(spliced[j++]);

                // go through anchors
                Piece nextPiece;
                for(; j < spliced.length; j += 2) {
                    int edgeAnchorId = Integer.parseInt(spliced[j]);
                    if(spliced[j + 1].charAt(0) == '-') nextPiece = null;
                    else {
                        int nextPieceId = Integer.parseInt(spliced[j + 1]);
                        nextPiece = shipComponent.piecesArray.get(nextPieceId);
                    }
                    piece.anchors.add(new Anchor(edgeId, edgeAnchorId, nextPiece, piece));

                }
            }
        }
    }

    private void buildShipEntities(List<Entity> entities, ShipComponent shipComponent, Body body, boolean isPlayer) {
        var array = shipComponent.piecesArray;
        for(Piece piece : array) {
            Entity entity = engine.createEntity();

            TransformComponent transformComponent = null;
            PieceComponent pieceComponent = createPieceComponent(piece);
            pieceComponent.fixture = bodyFactory.createPieceFixture(body, pieceComponent.piece, entity);
            Component specificPieceComponent = null;

            CollisionComponent collisionComponent = createCollisionComponent();
            TextureComponent textureComponent = createTextureComponent(Info.PIECE_TEXTURE_PATH + Info.pieceConfigsMap.get(piece.pieceConfigId).textureName);

            if(piece instanceof CorePiece) {
                transformComponent = createTransformComponent(piece.W, piece.H, 0, 0, textureComponent, Info.ZOrder.PIECE);
                specificPieceComponent = createCorePieceComponent((CorePiece) piece);
            } else if(piece instanceof HullPiece) {
                transformComponent = createTransformComponent(piece.W, piece.H, 0, 0, textureComponent, Info.ZOrder.PIECE);
                specificPieceComponent = createHullPieceComponent((HullPiece) piece);
            } else if(piece instanceof WeaponPiece) {
                transformComponent = createTransformComponent(piece.W, piece.H, 0, 0, textureComponent, Info.ZOrder.WEAPONS);
                specificPieceComponent = createWeaponPieceComponent((WeaponPiece) piece);

                // 112 comes from image gun.png, 112 pos on x and 112 on y
                TextureRotatingComponent rotatingComponent = createTextureRotatingComponent(Info.PIECE_TEXTURE_PATH + "gun.png", 112, 112);
                entity.add(rotatingComponent);
            } else if(piece instanceof ThrusterPiece) {
                transformComponent = createTransformComponent(piece.W, piece.H, 0, ((ThrusterPiece) piece).angleDirection * Info.rad90Deg, textureComponent, Info.ZOrder.PIECE);
                specificPieceComponent = createThrusterPieceComponent((ThrusterPiece) piece);
            } else {
                transformComponent = createTransformComponent(Info.blockSize, Info.blockSize, 0, 0, textureComponent, Info.ZOrder.PIECE);
            }

            entity.add(transformComponent);
            entity.add(textureComponent);
            entity.add(pieceComponent);
            entity.add(shipComponent);
            entity.add(collisionComponent);
            if(specificPieceComponent != null) entity.add(specificPieceComponent);
            if(piece instanceof CorePiece) entity.add(engine.createComponent(ShipCoreComponent.class));
            if(isPlayer) entity.add(engine.createComponent(PlayerComponent.class));
            else entity.add(engine.createComponent(NPCComponent.class));

            entities.add(entity);

            piece.pieceComponent = pieceComponent;
            for(Anchor a : piece.anchors) {
                Entity anchorEntity = createAnchorEntity(pieceComponent, a, a.piece == null && isPlayer);
                engine.addEntity(anchorEntity);
            }
        }
    }



    private TextureComponent createTextureComponent(String image) {
        TextureComponent textureComponent = engine.createComponent(TextureComponent.class);
        textureComponent.textureRegion = new TextureRegion(game.assetManager.get(image, Texture.class));
        return textureComponent;
    }

    private TextureRotatingComponent createTextureRotatingComponent(String image, float originX, float originY) {
        TextureRotatingComponent rotatingComponent = engine.createComponent(TextureRotatingComponent.class);
        rotatingComponent.textureRegion = new TextureRegion(game.assetManager.get(image, Texture.class));
        rotatingComponent.origin = new Vector2(originX, originY);
        return rotatingComponent;
    }

    private TransformComponent createTransformComponent(float sizeWidth, float sizeHeight, float angleRad, float angleOrientationRad, TextureComponent textureComponent, Info.ZOrder z) {
        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
        transformComponent.position.z = z.getValue();
        transformComponent.width = sizeWidth;
        transformComponent.height = sizeHeight;
        transformComponent.scale.x = transformComponent.width / textureComponent.textureRegion.getRegionWidth();
        transformComponent.scale.y = transformComponent.height / textureComponent.textureRegion.getRegionHeight();
        transformComponent.angleRad = angleRad;
        transformComponent.angleOrientationRad = angleOrientationRad;
        return transformComponent;
    }

    private BodyComponent createBodyComponent(Body body) {
        BodyComponent bodyComponent = engine.createComponent(BodyComponent.class);
        bodyComponent.body = body;
        return bodyComponent;
    }

    private PieceComponent createPieceComponent(int pieceX, int pieceY, int pieceWidth, int pieceHeight) {
        PieceComponent simplePieceComponent = engine.createComponent(PieceComponent.class);
        simplePieceComponent.piece = new Piece(pieceX, pieceY, pieceWidth, pieceHeight, true);
        return simplePieceComponent;
    }

    private PieceComponent createPieceComponent(Piece piece) {
        PieceComponent simplePieceComponent = engine.createComponent(PieceComponent.class);
        simplePieceComponent.piece = piece;
        return simplePieceComponent;
    }

    private CorePieceComponent createCorePieceComponent(CorePiece corePiece) {
        CorePieceComponent corePieceComponent = engine.createComponent(CorePieceComponent.class);
        corePieceComponent.piece = corePiece;
        return corePieceComponent;
    }

    private HullPieceComponent createHullPieceComponent(Piece piece) {
        HullPieceComponent hullPieceComponent = engine.createComponent(HullPieceComponent.class);
        hullPieceComponent.piece = new HullPiece(piece);
        return hullPieceComponent;
    }

    private HullPieceComponent createHullPieceComponent(HullPiece hullPiece) {
        HullPieceComponent hullPieceComponent = engine.createComponent(HullPieceComponent.class);
        hullPieceComponent.piece = hullPiece;
        return hullPieceComponent;
    }

    private CorePieceComponent createCorePieceComponent(Piece piece) {
        CorePieceComponent corePieceComponent = engine.createComponent(CorePieceComponent.class);
        corePieceComponent.piece = new CorePiece(piece);
        return corePieceComponent;
    }

    private ThrusterPieceComponent createThrusterPieceComponent(Piece piece, int angleRad) {
        ThrusterPieceComponent hullPieceComponent = engine.createComponent(ThrusterPieceComponent.class);
        hullPieceComponent.piece = new ThrusterPiece(piece, angleRad);
        return hullPieceComponent;
    }

    private ThrusterPieceComponent createThrusterPieceComponent(ThrusterPiece thrusterPiece) {
        ThrusterPieceComponent hullPieceComponent = engine.createComponent(ThrusterPieceComponent.class);
        hullPieceComponent.piece = thrusterPiece;
        return hullPieceComponent;
    }

    private WeaponPieceComponent createWeaponPieceComponent(Piece piece) {
        WeaponPieceComponent weaponPieceComponent = engine.createComponent(WeaponPieceComponent.class);
        weaponPieceComponent.piece = new WeaponPiece(piece);
        return weaponPieceComponent;
    }

    private WeaponPieceComponent createWeaponPieceComponent(WeaponPiece weaponPiece) {
        WeaponPieceComponent weaponPieceComponent = engine.createComponent(WeaponPieceComponent.class);
        weaponPieceComponent.piece = weaponPiece;
        return weaponPieceComponent;
    }

    private BulletComponent createBulletComponent(int actorId) {
        BulletComponent bulletComponent = engine.createComponent(BulletComponent.class);
        bulletComponent.createdByActorId = actorId;
        return bulletComponent;
    }

    private CollisionComponent createCollisionComponent() {
        return engine.createComponent(CollisionComponent.class);
    }

}
