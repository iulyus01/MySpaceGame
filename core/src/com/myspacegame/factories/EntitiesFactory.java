package com.myspacegame.factories;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
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

        Body body = bodyFactory.createPieceBody(shipX, shipY, 0, false);

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
        entity.add(engine.createComponent(WallComponent.class));

        return entity;
    }

    public Entity createPieceEntity(Class<? extends Component> componentType, boolean random, float bodyX, float bodyY) {
        if(random) {
            bodyX = (float) Math.random() * 60 - 30;
            bodyY = (float) Math.random() * 60 - 30;
        }

        Entity entity = engine.createEntity();
        Body body = bodyFactory.createPieceBody(bodyX, bodyY, 0, true);


        Component specificPieceComponent;
        Piece piece;
        int rotation = 0;
        float angleOrientationRad = 0;
        if(componentType == WeaponPieceComponent.class) {
            piece = new WeaponPiece();
            specificPieceComponent = createWeaponPieceComponent((WeaponPiece) piece);
            piece.pieceConfigId = 4;

            // 112 comes from image gun.png, 112 pos on x and 112 on y
            TextureRotatingComponent rotatingComponent = createTextureRotatingComponent(Info.PIECE_TEXTURE_PATH + "gun.png", 112, 112);
            entity.add(rotatingComponent);
        } else if(componentType == ThrusterPieceComponent.class) {
            rotation = MathUtils.random(0, 3);
            piece = new ThrusterPiece(rotation);
            specificPieceComponent = createThrusterPieceComponent((ThrusterPiece) piece);
            piece.pieceConfigId = 3;

            angleOrientationRad = rotation * Info.rad90Deg;
        } else {
            piece = new HullPiece();
            specificPieceComponent = createHullPieceComponent((HullPiece) piece);
            int rand = MathUtils.random(1, 3);
            if(rand == 2) rand = 6;
            else if(rand == 3) rand = 7;
            piece.pieceConfigId = rand;
        }
        piece.actorId = Info.StaticActorIds.NONE.getValue();
        piece.W = Info.blockSize * Info.pieceConfigsMap.get(piece.pieceConfigId).width;
        piece.H = Info.blockSize * Info.pieceConfigsMap.get(piece.pieceConfigId).height;
        piece.hp = Info.pieceConfigsMap.get(piece.pieceConfigId).hp;
        piece.rotation = rotation;
        piece.pos = new Vector2(0, 0);
        piece.shape = new Polygon(Info.edgesToNewVerticesArray(Info.pieceConfigsMap.get(piece.pieceConfigId).edges, Info.blockSize));
        piece.edges = Info.configEdgesToComputedEdges(Info.pieceConfigsMap.get(piece.pieceConfigId).edges, Info.blockSize);
        piece.anchors = new Array<>();
        for(int i = 0; i < piece.edges.size; i++) {
            // i - edgeIndex
            for(int j = 0; j < piece.edges.get(i).anchorRatios.size; j++) {
                // j - edgeAnchorIndex
                piece.anchors.add(new Anchor(i, j, null, piece));
            }
        }


        TextureComponent textureComponent = createTextureComponent(Info.PIECE_TEXTURE_PATH + Info.pieceConfigsMap.get(piece.pieceConfigId).textureName);
        TransformComponent transformComponent = createTransformComponent(piece.W, piece.H, 0, angleOrientationRad, textureComponent, Info.ZOrder.PIECE);
        PieceComponent pieceComponent = createPieceComponent(piece);
        pieceComponent.fixture = bodyFactory.createPieceFixture(body, pieceComponent.piece, entity);
        pieceComponent.piece.pieceComponent = pieceComponent;
        CollisionComponent collisionComponent = createCollisionComponent();

        entity.add(transformComponent);
        entity.add(textureComponent);
        entity.add(pieceComponent);
        entity.add(specificPieceComponent);
        entity.add(collisionComponent);

        for(Anchor a : piece.anchors) {
            Entity anchorEntity = createAnchorEntity(pieceComponent, a, false);
            engine.addEntity(anchorEntity);
        }

        return entity;
    }

    public Entity createBulletEntity(int actorId, float x, float y, float angleRad) {
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
//        transformComponent.isHidden = (Math.abs(pieceComponent.fixtureCenter.x - Info.mouseWorldX) > Info.blockSize * 20 || Math.abs(pieceComponent.fixtureCenter.y - Info.mouseWorldY) > Info.blockSize * 20);

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

    public Entity createRockEntity(int index, float x, float y, boolean random) {
        if(random) {
            index = MathUtils.random(0, Info.rockShapesMap.size() - 1);
        }
        float sizeRatio = MathUtils.random(MathUtils.random() < 0.7 ? Info.blockSize * 4 : Info.blockSize * 6, Info.blockSize * 2);
        float angleRad = MathUtils.random(0, Info.rad360Deg);

        Entity entity = engine.createEntity();
        TextureComponent textureComponent = createTextureComponent("images/Rock" + index + ".png");

        float width;
        float height;
        if(textureComponent.textureRegion.getRegionWidth() > textureComponent.textureRegion.getRegionHeight()) {
            width = sizeRatio;
            height = textureComponent.textureRegion.getRegionHeight() / (float) textureComponent.textureRegion.getRegionWidth() * sizeRatio;
        } else {
            width = textureComponent.textureRegion.getRegionWidth() / (float) textureComponent.textureRegion.getRegionHeight() * sizeRatio;
            height = sizeRatio;
        }
        width *= 2;
        height *= 2;
        TransformComponent transformComponent = createTransformComponent(width, height, angleRad, 0, textureComponent, Info.ZOrder.ROCKS);
        RockComponent rockComponent = createRockComponent();

        Body body = bodyFactory.createRockBody(index, x, y, angleRad, sizeRatio, 0, 0, entity);
        BodyComponent bodyComponent = createBodyComponent(body);

        CollisionComponent collisionComponent = createCollisionComponent();

        entity.add(rockComponent);
        entity.add(textureComponent);
        entity.add(transformComponent);
        entity.add(bodyComponent);
        entity.add(collisionComponent);

        return entity;
    }

    public void removePieceHoverEntity(Entity entity) {
        engine.removeEntity(entity);
    }




    private Piece getPieceType(String type, int rotation, ShipComponent shipComponent) {
        Piece piece;
        switch(type.charAt(0)) {
            case '0':
                piece = new CorePiece();
                shipComponent.core = piece;
                break;
            case '1': case '2': default:
                piece = new HullPiece();
                break;
            case '3':
                piece = new ThrusterPiece(rotation);
                break;
            case '4':
                piece = new WeaponPiece();
                break;
            case '5':
                piece = new TractorBeamPiece(Info.defaultTractorBeamRadius);
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
            int rotation = Integer.parseInt(spliced[2]);
            Piece toAddPiece = getPieceType(spliced[1], rotation, shipComponent);

            int pieceConfigId = Info.parseFirstInteger(spliced[1]);


            toAddPiece.actorId = actorId;
            toAddPiece.pieceConfigId = pieceConfigId;
            toAddPiece.W = Info.blockSize * Info.pieceConfigsMap.get(pieceConfigId).width;
            toAddPiece.H = Info.blockSize * Info.pieceConfigsMap.get(pieceConfigId).height;
            toAddPiece.hp = Info.pieceConfigsMap.get(pieceConfigId).hp;
            toAddPiece.rotation = rotation;
            toAddPiece.pos = new Vector2(Integer.parseInt(spliced[3]), Integer.parseInt(spliced[4]));
            toAddPiece.shape = new Polygon(Info.edgesToNewVerticesArray(Info.pieceConfigsMap.get(pieceConfigId).edges, Info.blockSize));
            toAddPiece.edges = Info.configEdgesToComputedEdges(Info.pieceConfigsMap.get(pieceConfigId).edges, Info.blockSize);

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

            TransformComponent transformComponent;
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
                transformComponent = createTransformComponent(piece.W, piece.H, 0, piece.rotation * Info.rad90Deg, textureComponent, Info.ZOrder.PIECE);
                specificPieceComponent = createThrusterPieceComponent((ThrusterPiece) piece);
            } else if(piece instanceof TractorBeamPiece) {
                transformComponent = createTransformComponent(piece.W, piece.H, 0, piece.rotation * Info.rad90Deg, textureComponent, Info.ZOrder.PIECE);
                specificPieceComponent = createTractorBeamPieceComponent((TractorBeamPiece) piece);
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

    private HullPieceComponent createHullPieceComponent(HullPiece hullPiece) {
        HullPieceComponent hullPieceComponent = engine.createComponent(HullPieceComponent.class);
        hullPieceComponent.piece = hullPiece;
        return hullPieceComponent;
    }

    private ThrusterPieceComponent createThrusterPieceComponent(ThrusterPiece thrusterPiece) {
        ThrusterPieceComponent hullPieceComponent = engine.createComponent(ThrusterPieceComponent.class);
        hullPieceComponent.piece = thrusterPiece;
        return hullPieceComponent;
    }

    private WeaponPieceComponent createWeaponPieceComponent(WeaponPiece weaponPiece) {
        WeaponPieceComponent weaponPieceComponent = engine.createComponent(WeaponPieceComponent.class);
        weaponPieceComponent.piece = weaponPiece;
        return weaponPieceComponent;
    }

    private TractorBeamPieceComponent createTractorBeamPieceComponent(TractorBeamPiece tractorBeamPiece) {
        TractorBeamPieceComponent tractorBeamPieceComponent = engine.createComponent(TractorBeamPieceComponent.class);
        tractorBeamPieceComponent.piece = tractorBeamPiece;
        return tractorBeamPieceComponent;
    }

    private BulletComponent createBulletComponent(int actorId) {
        BulletComponent bulletComponent = engine.createComponent(BulletComponent.class);
        bulletComponent.createdByActorId = actorId;
        return bulletComponent;
    }

    private RockComponent createRockComponent() {
        return engine.createComponent(RockComponent.class);
    }

    private CollisionComponent createCollisionComponent() {
        CollisionComponent collisionComponent = engine.createComponent(CollisionComponent.class);
        collisionComponent.collisionEntities = new Array<>(false, 8, Entity.class);
        return collisionComponent;
    }

}
