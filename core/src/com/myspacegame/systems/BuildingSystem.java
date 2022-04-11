package com.myspacegame.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
import com.myspacegame.KeyboardController;
import com.myspacegame.MainClass;
import com.myspacegame.components.*;
import com.myspacegame.components.pieces.*;
import com.myspacegame.entities.Anchor;
import com.myspacegame.entities.Piece;
import com.myspacegame.factories.BodyFactory;
import com.myspacegame.factories.EntitiesFactory;
import com.myspacegame.factories.ShapeRenderingDebug;
import com.myspacegame.factories.WorldFactory;

public class BuildingSystem extends IteratingSystem {

    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<TextureComponent> textureMapper;
    private final ComponentMapper<ShipComponent> shipMapper;
    private final KeyboardController controller;
    private final EntitiesFactory entitiesFactory;
    private final BodyFactory bodyFactory;
    private final Engine engine;
    private final World world;

    private final ShipComponent playerShip;
    private final Body playerShipBody;

    private boolean isDraggingPiece = false;
    private boolean isDraggingPieceBegin = false;
    private Entity draggingEntity = null;
    private Entity hoverEntity = null;
    private boolean hoverIsActive = false;
    private Fixture draggedFixture = null;
    private short draggingFixtureMask = 0;
    private final Array<Info.Pair<Anchor, Anchor>> toAttachAnchors;

    private TextureComponent lastTextureComponent;

    public BuildingSystem(KeyboardController keyboardController, MainClass game, PooledEngine engine) {
        super(Family.one(PieceComponent.class).exclude(NPCComponent.class).get());
        this.engine = engine;
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        textureMapper = ComponentMapper.getFor(TextureComponent.class);
        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        shipMapper = ComponentMapper.getFor(ShipComponent.class);
        controller = keyboardController;

        world = WorldFactory.getInstance(game, engine).getWorld();
        entitiesFactory = EntitiesFactory.getInstance(game, engine, world);
        bodyFactory = BodyFactory.getInstance(world);

        toAttachAnchors = new Array<>(false, 20, Info.Pair.class);

        Entity playerEntity = engine.getEntitiesFor(Family.all(PlayerComponent.class, ShipComponent.class, PieceComponent.class).get()).first();
        playerShip = shipMapper.get(playerEntity);
        playerShipBody = pieceMapper.get(playerEntity).fixture.getBody();

    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        transformMapper.get(hoverEntity).isHidden = !hoverIsActive;
        hoverIsActive = false;

        if(controller.isDragged) {
            if(isDraggingPieceBegin) {
                draggingPieceBegin();
                isDraggingPiece = true;
                isDraggingPieceBegin = false;
            }
            if(isDraggingPiece) {
                draggingPieceUpdate();
            }
        } else {
            if(isDraggingPiece) draggingPieceEnd();
        }

    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        hoverEntity = entitiesFactory.createPieceHoverEntity();
        engine.addEntity(hoverEntity);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        if(lastTextureComponent != null) lastTextureComponent.overlayTexture = null;
        entitiesFactory.removePieceHoverEntity(hoverEntity);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if(isDraggingPiece && playerShip != null) return;
        PieceComponent pieceComponent = pieceMapper.get(entity);
        TextureComponent textureComponent = textureMapper.get(entity);

        Piece piece = pieceComponent.piece;

        Vector2 fixtureCenter = pieceComponent.fixtureCenter;
        boolean closeToEntityX = Math.abs(Info.mouseWorldX - fixtureCenter.x) < Info.blockSize * piece.W * 4f;
        boolean closeToEntityY = Math.abs(Info.mouseWorldY - fixtureCenter.y) < Info.blockSize * piece.H * 4f;
        if(closeToEntityX && closeToEntityY) {
            // test mouse hover
            if(pieceComponent.fixture.testPoint(Info.mouseWorldX, Info.mouseWorldY)) {
                updatePieceHoverTransform(transformMapper.get(hoverEntity), textureMapper.get(hoverEntity), transformMapper.get(entity));
                hoverIsActive = true;
//                textureComponent.overlayTexture = hoverTexture;
                if(controller.isDragged) {
                    isDraggingPieceBegin = true;
                    draggingEntity = entity;
                    draggedFixture = pieceComponent.fixture;
                }
                lastTextureComponent = textureComponent;
            }

        }

    }

    private void draggingPieceBegin() {
        DraggingComponent draggingComponent = engine.createComponent(DraggingComponent.class);
        draggingComponent.playerShipAngleRad = playerShipBody.getAngle();
        draggingComponent.first = true;
        draggingEntity.add(draggingComponent);
        draggingFixtureMask = draggedFixture.getFilterData().maskBits;
        draggedFixture.getFilterData().maskBits = Info.MASK_NOTHING;

        ShipComponent shipComponent = shipMapper.get(draggingEntity);
        PieceComponent pieceComponent = pieceMapper.get(draggingEntity);
        TransformComponent transformComponent = transformMapper.get(draggingEntity);
        if(shipComponent != null) {
            pieceComponent.isManuallyDetached = true;
            transformComponent.position.z = Info.ZOrder.PIECE_DRAG.getValue();
            draggedFixture = pieceComponent.fixture;
        }
    }

    private void draggingPieceUpdate() {
        PieceComponent pieceComponent = pieceMapper.get(draggingEntity);

        Piece draggedPiece = pieceComponent.piece;

        // idea
        // the body(fixture) won't fix itself, but another entity used as a piece ghost will be fixed
        // maybe
        // i'll just draw lines... yea..
        toAttachAnchors.clear();
        for(Piece shipPiece : playerShip.piecesArray) {
            if(shipPiece == null) continue;
            if(shipPiece.pieceComponent.fixtureCenter.dst2(pieceComponent.fixtureCenter) > (Info.maxPieceSize * Info.maxPieceSize + Info.blockSize)) continue;

            for(int i = 0; i < shipPiece.anchors.size; i++) {
                Anchor shipPieceAnchor = shipPiece.anchors.get(i);
                if(shipPieceAnchor == null) continue;
                for(Anchor draggedPieceAnchor : draggedPiece.anchors) {

                    float distance = Info.dist(draggedPieceAnchor.pos.x, draggedPieceAnchor.pos.y, shipPieceAnchor.pos.x, shipPieceAnchor.pos.y);

                    if(distance < Info.blockSize / 2) {
                        ShapeRenderingDebug.addToDrawThenRemove(() -> ShapeRenderingDebug.drawDebugLine(
                                draggedPieceAnchor.pos.x, draggedPieceAnchor.pos.y, shipPieceAnchor.pos.x, shipPieceAnchor.pos.y
                        ));
                        // TODO make these lines ^^ actual lines

                        toAttachAnchors.add(new Info.Pair<>(
                                shipPieceAnchor,
                                draggedPieceAnchor
                        ));
                    }
                }

            }
        }
    }

    private void draggingPieceEnd() {
        draggingEntity.remove(DraggingComponent.class);
        TransformComponent transformComponent = transformMapper.get(draggingEntity);
        transformComponent.position.z = Info.ZOrder.OTHERS.getValue();
        draggedFixture.getFilterData().maskBits = draggingFixtureMask;
        draggedFixture = null;

        if(toAttachAnchors.size > 0) {
            PieceComponent pieceComponent = pieceMapper.get(draggingEntity);

            buildingAdjustAttachAnchors(playerShip, pieceComponent, toAttachAnchors);
            if(toAttachAnchors.size > 0) {
                buildingAttachPiece(draggingEntity, playerShip, playerShipBody, pieceComponent, toAttachAnchors);
            }
        }

        isDraggingPiece = false;
    }


    public void buildingAdjustAttachAnchors(ShipComponent playerShip, PieceComponent pieceComponent, Array<Info.Pair<Anchor, Anchor>> toAttachAnchors) {
        float maxDistanceCheck2 = Info.maxPieceSize * Info.maxPieceSize * 2.25f;

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
                    break;
                }
            }
        }
    }

    public void buildingAttachPiece(Entity entity, ShipComponent shipComponent, Body shipBody, PieceComponent pieceComponent, Array<Info.Pair<Anchor, Anchor>> attachAnchors) {
        if(shipBody == null) return;

        Piece piece = pieceComponent.piece;
        piece.actorId = shipComponent.piecesArray.get(0).actorId;
        Anchor shipAnchor = attachAnchors.first().first;
        Anchor pieceAnchor = attachAnchors.first().second;

        float offsetX = shipAnchor.pos.x - pieceAnchor.pos.x;
        float offsetY = shipAnchor.pos.y - pieceAnchor.pos.y;

        shipComponent.piecesArray.add(piece);

        piece.pos.x = Math.round(((pieceComponent.fixtureCenter.x + offsetX - shipComponent.core.pieceComponent.fixtureCenter.x) / Info.blockSize) * 2) / 2f;
        piece.pos.y = Math.round(((pieceComponent.fixtureCenter.y + offsetY - shipComponent.core.pieceComponent.fixtureCenter.y) / Info.blockSize) * 2) / 2f;

        for(var pair : attachAnchors) {
            entitiesFactory.removeAnchorEntity(pair.first);
            entitiesFactory.removeAnchorEntity(pair.second);
            pair.first.piece = piece;
            pair.second.piece = pair.first.srcPiece;
        }

        Body pieceBody = pieceComponent.fixture.getBody();

        pieceComponent.fixture.getBody().destroyFixture(pieceComponent.fixture);
        pieceComponent.fixture = bodyFactory.createPieceFixture(shipBody, piece, entity);
        world.destroyBody(pieceBody);

        entity.add(engine.createComponent(PlayerComponent.class));
        entity.add(shipComponent);

    }

    private void updatePieceHoverTransform(TransformComponent transformComponent, TextureComponent textureComponent, TransformComponent pieceTransform) {
        transformComponent.width = pieceTransform.width;
        transformComponent.height = pieceTransform.height;
        transformComponent.position.x = pieceTransform.position.x;
        transformComponent.position.y = pieceTransform.position.y;
        transformComponent.scale.x = transformComponent.width / textureComponent.textureRegion.getRegionWidth();
        transformComponent.scale.y = transformComponent.height / textureComponent.textureRegion.getRegionHeight();
        transformComponent.angleRad = pieceTransform.angleRad;
        transformComponent.angleOrientationRad = 0;
    }

}