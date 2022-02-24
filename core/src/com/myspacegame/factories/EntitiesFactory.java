package com.myspacegame.factories;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
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
    private final World world;
    private final BodyFactory bodyFactory;

    private EntitiesFactory(MainClass game, Engine engine, World world) {
        this.game = game;
        this.engine = engine;
        this.world = world;

        bodyFactory = BodyFactory.getInstance(world);
    }

    public static EntitiesFactory getInstance(MainClass game, Engine engine, World world) {
        if(instance == null) instance = new EntitiesFactory(game, engine, world);
        return instance;
    }

    public List<Entity> createShip(String[] shipString, float shipX, float shipY, boolean isPlayer) {
        ShipComponent shipComponent = engine.createComponent(ShipComponent.class);
        extractShipString(shipString, shipComponent);

        Body body = bodyFactory.createPieceBody(shipX, shipY, 0);
//        body.setTransform(body.getPosition(), Info.rad90Deg / 3);

        List<Entity> entities = new ArrayList<>();

        buildEntities(entities, shipComponent, body, isPlayer);

        return entities;
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
        int totalPieceHeight = 1;
        float angleOrientationRad = 0;
        Info.ZOrder z = Info.ZOrder.OTHERS;

        TextureComponent textureComponent = createTextureComponent("images/hull.png");
        TransformComponent transformComponent = createTransformComponent(Info.blockSize * pieceWidth, Info.blockSize * pieceHeight, 0, angleOrientationRad, textureComponent, z);
        PieceComponent simplePieceComponent = createPieceComponent(pieceX, pieceY, pieceWidth, pieceHeight);
        simplePieceComponent.fixture = bodyFactory.createPieceFixture(body, simplePieceComponent.piece);
        HullPieceComponent pieceComponent = createHullPieceComponent(simplePieceComponent.piece);

        simplePieceComponent.piece.pieceComponent = simplePieceComponent;

        Entity entity = engine.createEntity();
        entity.add(simplePieceComponent);
        entity.add(pieceComponent);
        entity.add(transformComponent);
        entity.add(textureComponent);

        return entity;
    }

    public Entity createBullet(float x, float y, float angleRad) {
        float sizeWidth = Info.blockSize * 2.4f;
        float sizeHeight = Info.blockSize;
        Body body = bodyFactory.createBulletBody(x, y, Info.blockSize, Info.blockSize / 4, angleRad, Info.defaultBulletImpulse);

        TextureComponent textureComponent = createTextureComponent("images/bullet.png");
        BulletComponent bulletComponent = createBulletComponent();
        TransformComponent transformComponent = createTransformComponent(sizeWidth, sizeHeight, angleRad, 0, textureComponent, Info.ZOrder.BULLETS);
        BodyComponent bodyComponent = createBodyComponent(body);

        Entity entity = engine.createEntity();
        entity.add(bulletComponent);
        entity.add(textureComponent);
        entity.add(transformComponent);
        entity.add(bodyComponent);

        return entity;
    }

    public void detachPiece(Entity entity, ShipComponent shipComponent, PieceComponent pieceComponent, Vector2 draggingPoint) {
        Piece piece = pieceComponent.piece;
        shipComponent.piecesArray.removeValue(piece, true);
        for(Anchor anchor: piece.anchors) {
            if(anchor.piece == null) continue;
            int i = 0;
            while(i < anchor.piece.anchors.size && anchor.piece.anchors.get(i).piece != piece) i++;
            if(i < anchor.piece.anchors.size) anchor.piece.anchors.get(i).piece = null;
            anchor.piece = null;
        }

        piece.pos.x = 0;
        piece.pos.y = 0;

        Body body = bodyFactory.createPieceBody(pieceComponent.fixtureCenter.x, pieceComponent.fixtureCenter.y, pieceComponent.fixture.getBody().getAngle());
        Fixture fixture = bodyFactory.createPieceFixture(body, piece);

        draggingPoint.x = Info.mouseWorldX - pieceComponent.fixtureCenter.x;
        draggingPoint.y = Info.mouseWorldY - pieceComponent.fixtureCenter.y;

        pieceComponent.fixture.getBody().destroyFixture(pieceComponent.fixture);
        pieceComponent.fixture = fixture;
        pieceComponent.fixture.getFilterData().maskBits = Info.MASK_NOTHING;

        entity.remove(PlayerComponent.class);
        entity.remove(ShipComponent.class);


        System.out.println("test DETACH");

    }

    public void adjustAttachAnchors(ShipComponent playerShip, PieceComponent pieceComponent, Array<Info.Pair<Anchor, Anchor>> toAttachAnchors) {
        float maxDistanceCheck2 = Info.maxPieceSize * Info.maxPieceSize * 2.25f;

//        bigCheck:
        for(int i = 0; i < toAttachAnchors.size; i++) {
            boolean overlaps;
            var pair = toAttachAnchors.get(i);
            Anchor shipAnchor = pair.first;
            Anchor pieceAnchor = pair.second;

            float offsetX = shipAnchor.pos.x - pieceAnchor.pos.x;
            float offsetY = shipAnchor.pos.y - pieceAnchor.pos.y;

            float[] pieceVertices = pieceComponent.piece.shape.getVertices();
            Polygon piecePolygon = new Polygon(pieceVertices);
            piecePolygon.setRotation(pieceComponent.fixture.getBody().getAngle() * MathUtils.radDeg);
            piecePolygon.setPosition(pieceComponent.fixtureCenter.x + offsetX, pieceComponent.fixtureCenter.y + offsetY);
            piecePolygon.setScale(.9f, .9f);
            ShapeRenderingDebug.addToDrawWithId(() -> ShapeRenderingDebug.drawDebugPolygon(piecePolygon.getTransformedVertices()), 2, 2000);

            for(Piece shipPiece : playerShip.piecesArray) {
                if(shipPiece.pieceComponent.fixtureCenter.dst2(pieceComponent.fixtureCenter) > maxDistanceCheck2) continue;
                float[] vertices = shipPiece.shape.getVertices();
                Polygon polygon = new Polygon(vertices);
                polygon.setPosition(shipPiece.pieceComponent.fixtureCenter.x, shipPiece.pieceComponent.fixtureCenter.y);
                polygon.setRotation(piecePolygon.getRotation());

                overlaps = Intersector.overlapConvexPolygons(piecePolygon.getTransformedVertices(), polygon.getTransformedVertices(), null);
                if(overlaps) {
                    ShapeRenderingDebug.addToDrawWithId(() -> ShapeRenderingDebug.drawDebugPolygon(polygon.getTransformedVertices()), 3, 2000);
                    toAttachAnchors.removeIndex(i);
                    i--;
                    System.out.println("OVERLAPS");
                    break;
                }
            }
        }
    }

    public void attachPiece(Entity entity, ShipComponent shipComponent, Body shipBody, PieceComponent pieceComponent, Array<Info.Pair<Anchor, Anchor>> attachAnchors) {
        if(shipBody == null) return;

        Piece piece = pieceComponent.piece;
        Anchor shipAnchor = attachAnchors.first().first;
        Anchor pieceAnchor = attachAnchors.first().second;

        float offsetX = shipAnchor.pos.x - pieceAnchor.pos.x;
        float offsetY = shipAnchor.pos.y - pieceAnchor.pos.y;

        shipComponent.piecesArray.add(piece);

        float distX = pieceComponent.fixtureCenter.x + offsetX - shipComponent.core.pieceComponent.fixtureCenter.x;
        float distY = pieceComponent.fixtureCenter.y + offsetY - shipComponent.core.pieceComponent.fixtureCenter.y;
        float angle = MathUtils.atan2(distY, distX);
        float dist = (float) Math.sqrt(distX * distX + distY * distY);

        float pieceX = shipComponent.core.pieceComponent.fixtureCenter.x + MathUtils.cos(angle - shipBody.getAngle()) * dist;
        float pieceY = shipComponent.core.pieceComponent.fixtureCenter.y + MathUtils.sin(angle - shipBody.getAngle()) * dist;

        piece.pos.x = Math.round(((pieceComponent.fixtureCenter.x + offsetX - shipComponent.core.pieceComponent.fixtureCenter.x) / Info.blockSize) * 2) / 2f;
        piece.pos.y = Math.round(((pieceComponent.fixtureCenter.y + offsetY - shipComponent.core.pieceComponent.fixtureCenter.y) / Info.blockSize) * 2) / 2f;
//        piece.pos.x = (pieceX - shipComponent.core.pieceComponent.fixtureCenter.x) / Info.blockSize;
//        piece.pos.y = (pieceY - shipComponent.core.pieceComponent.fixtureCenter.y) / Info.blockSize;


        for(var pair : attachAnchors) {
            pair.first.piece = piece;
            pair.second.piece = pair.first.srcPiece;
        }

        Body pieceBody = pieceComponent.fixture.getBody();

        pieceComponent.fixture.getBody().destroyFixture(pieceComponent.fixture);
        pieceComponent.fixture = bodyFactory.createPieceFixture(shipBody, pieceComponent.piece);
        world.destroyBody(pieceBody);

        entity.add(engine.createComponent(PlayerComponent.class));
        entity.add(shipComponent);

        Info.temp = pieceComponent;
        System.out.println(Info.temp.fixtureCenter.x + " " + Info.temp.fixtureCenter.y);

    }



    private void extractShipString(String[] shipString, ShipComponent shipComponent) {
        int capacity = 0;
        for(String s : shipString) {
            if(s.length() == 0) break;
            capacity++;
        }

        shipComponent.piecesArray = new Array<>(false, capacity, Piece.class);
        for(int i = 0; i < capacity; i++) {
            String[] spliced = shipString[i].split("\\s+");
            Piece toAddPiece = getPieceType(spliced[0], shipComponent);

            int pieceConfigId = Integer.parseInt(spliced[3]);

            toAddPiece.pieceConfigId = pieceConfigId;
            toAddPiece.W = Info.blockSize * Info.pieceConfigsMap.get(pieceConfigId).width;
            toAddPiece.H = Info.blockSize * Info.pieceConfigsMap.get(pieceConfigId).height;
            toAddPiece.pos = new Vector2(Integer.parseInt(spliced[1]), Integer.parseInt(spliced[2]));
            float[] computedVertices = new float[Info.pieceConfigsMap.get(pieceConfigId).vertices.length];
            for(int j = 0; j < Info.pieceConfigsMap.get(pieceConfigId).vertices.length; j++)
                computedVertices[j] = Info.pieceConfigsMap.get(pieceConfigId).vertices[j] * Info.blockSize;
            toAddPiece.shape = new Polygon(computedVertices);
            shipComponent.piecesArray.add(toAddPiece);
        }

        for(int i = capacity + 1; i < shipString.length; i++) {
            String[] spliced = shipString[i].split("\\s+");
            int index = Integer.parseInt(spliced[0]);

            Piece piece = shipComponent.piecesArray.get(index);
            piece.anchors = new Array<>(false, Info.pieceConfigsMap.get(piece.pieceConfigId).startIndices.length, Anchor.class);

            for(int j = 0; j < Info.pieceConfigsMap.get(piece.pieceConfigId).startIndices.length; j++) {
                index = j + 1;
                Piece nextPiece;
                if(spliced[index].charAt(0) == '-') nextPiece = null;
                else nextPiece = shipComponent.piecesArray.get(Integer.parseInt(spliced[index]));
                piece.anchors.add(new Anchor(
                        j,
                        Info.pieceConfigsMap.get(piece.pieceConfigId).startIndices[j],
                        Info.pieceConfigsMap.get(piece.pieceConfigId).endIndices[j],
                        Info.pieceConfigsMap.get(piece.pieceConfigId).posRatioList[j],
                        nextPiece,
                        piece
                ));
            }
        }
    }

    private Piece getPieceType(String type, ShipComponent shipComponent) {
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
                int angle = (type.charAt(1) - '0');
                Info.Key key;
                if(type.charAt(2) == 'D') key = Info.Key.D;
                else if(type.charAt(2) == 'W') key = Info.Key.W;
                else if(type.charAt(2) == 'A') key = Info.Key.A;
                else key = Info.Key.S;
                piece = new ThrusterPiece(0, 0, 0, 0, angle, key);
                break;
            case '3':
                piece = new WeaponPiece(0, 0, 0, 0);
                break;
        }
        return piece;
    }

    private void buildEntities(List<Entity> entities, ShipComponent shipComponent, Body body, boolean isPlayer) {
        var array = shipComponent.piecesArray;
        for(Piece piece : array) {
            Entity entity = engine.createEntity();

            TextureComponent textureComponent = null;
            TransformComponent transformComponent = null;
            PieceComponent pieceComponent = createPieceComponent(piece);
            pieceComponent.fixture = bodyFactory.createPieceFixture(body, piece);
            Component specificPieceComponent = null;

            if(piece instanceof CorePiece) {
                textureComponent = createTextureComponent("images/pieces/core.png");
                transformComponent = createTransformComponent(piece.W, piece.H, 0, 0, textureComponent, Info.ZOrder.OTHERS);
                specificPieceComponent = createCorePieceComponent((CorePiece) piece);
            } else if(piece instanceof HullPiece) {
                textureComponent = createTextureComponent("images/pieces/hull.png");
                transformComponent = createTransformComponent(piece.W, piece.H, 0, 0, textureComponent, Info.ZOrder.OTHERS);
                specificPieceComponent = createHullPieceComponent((HullPiece) piece);
            } else if(piece instanceof WeaponPiece) {
                textureComponent = createTextureComponent("images/pieces/hull.png");
                transformComponent = createTransformComponent(piece.W, piece.H, 0, 0, textureComponent, Info.ZOrder.WEAPONS);
                specificPieceComponent = createWeaponPieceComponent((WeaponPiece) piece);

                TextureRotatingComponent rotatingComponent = engine.createComponent(TextureRotatingComponent.class);
                rotatingComponent.textureRegion = new TextureRegion(game.assetManager.get("images/pieces/gun.png", Texture.class));
                rotatingComponent.origin = new Vector2(rotatingComponent.textureRegion.getRegionHeight() / 2f, 14);
                entity.add(rotatingComponent);
            } else if(piece instanceof ThrusterPiece) {
                textureComponent = createTextureComponent("images/pieces/thruster.png");
                transformComponent = createTransformComponent(piece.W, piece.H, 0, ((ThrusterPiece) piece).angleRad * Info.rad90Deg, textureComponent, Info.ZOrder.OTHERS);
                specificPieceComponent = createThrusterPieceComponent((ThrusterPiece) piece);
            }

            if(textureComponent != null) entity.add(textureComponent);
            if(transformComponent != null) entity.add(transformComponent);
            if(specificPieceComponent != null) entity.add(specificPieceComponent);
            entity.add(pieceComponent);
            entity.add(shipComponent);
            entities.add(entity);
            if(isPlayer) entity.add(engine.createComponent(PlayerComponent.class));

            piece.pieceComponent = pieceComponent;
        }
    }

    private TextureComponent createTextureComponent(String image) {
        TextureComponent textureComponent = engine.createComponent(TextureComponent.class);
        textureComponent.textureRegion = new TextureRegion(game.assetManager.get(image, Texture.class));
        return textureComponent;
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

    private ShipComponent createShipComponent(Array<Array<Piece>> ship, int shipWidth, int shipHeight) {
        ShipComponent shipComponent = engine.createComponent(ShipComponent.class);
        shipComponent.ship = ship;
        shipComponent.width = shipWidth;
        shipComponent.height = shipHeight;
        return shipComponent;
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

    @SuppressWarnings("unused")
    private CorePieceComponent createCorePieceComponent(Piece piece) {
        CorePieceComponent corePieceComponent = engine.createComponent(CorePieceComponent.class);
        corePieceComponent.piece = new CorePiece(piece);
        return corePieceComponent;
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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

    private BulletComponent createBulletComponent() {
        return engine.createComponent(BulletComponent.class);
    }

}
