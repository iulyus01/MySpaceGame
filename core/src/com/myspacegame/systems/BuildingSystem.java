package com.myspacegame.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
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
import com.myspacegame.factories.EntitiesFactory;
import com.myspacegame.factories.ShapeRenderingDebug;
import com.myspacegame.factories.WorldFactory;

public class BuildingSystem extends IteratingSystem {

    private final ComponentMapper<PieceComponent> pieceMapper;
    private final ComponentMapper<TransformComponent> transformMapper;
    private final ComponentMapper<TextureComponent> textureMapper;
    private final ComponentMapper<ShipComponent> shipMapper;
    private final ComponentMapper<PlayerComponent> playerMapper;
    private final KeyboardController controller;
    private final EntitiesFactory entitiesFactory;
    private final Engine engine;

    private final ShipComponent playerShip;
    private final Body playerShipBody;

    private boolean isDraggingPiece = false;
    private boolean isDraggingPieceBegin = false;
    private Entity draggingEntity = null;
    private Fixture draggedFixture = null;
    private short draggingFixtureMask = 0;
    private final Vector2 draggingPointOfPieceFromCenter;
    private final Vector2 pieceDistanceDunnoWhatThisIsFor;
    private final Array<Info.Pair<Anchor, Anchor>> toAttachAnchors;

    private final TextureRegion hoverTexture;
    private TextureComponent lastTextureComponent;

    public BuildingSystem(KeyboardController keyboardController, MainClass game, PooledEngine engine) {
        super(Family.all(TextureComponent.class, TransformComponent.class, PieceComponent.class).get());
        this.engine = engine;
        transformMapper = ComponentMapper.getFor(TransformComponent.class);
        textureMapper = ComponentMapper.getFor(TextureComponent.class);
        pieceMapper = ComponentMapper.getFor(PieceComponent.class);
        shipMapper = ComponentMapper.getFor(ShipComponent.class);
        playerMapper = ComponentMapper.getFor(PlayerComponent.class);
        controller = keyboardController;

        World world = WorldFactory.getInstance(game, engine).getWorld();
        entitiesFactory = EntitiesFactory.getInstance(game, engine, world);

        hoverTexture = new TextureRegion(game.assetManager.get("images/hover.png", Texture.class));
        draggingPointOfPieceFromCenter = new Vector2();
        pieceDistanceDunnoWhatThisIsFor = new Vector2();
        toAttachAnchors = new Array<>(false, 20, Info.Pair.class);

        Entity playerEntity = engine.getEntitiesFor(Family.all(PlayerComponent.class, ShipComponent.class, PieceComponent.class).get()).first();
        playerShip = shipMapper.get(playerEntity);
        playerShipBody = pieceMapper.get(playerEntity).fixture.getBody();

    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if(controller.isDragged) {
            if(isDraggingPiece) {
                if(isDraggingPieceBegin) draggingPieceBegin();
                draggingPieceUpdate();
            }
        } else {
            if(isDraggingPiece) draggingPieceEnd();
        }

    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        if(lastTextureComponent != null) lastTextureComponent.overlayTexture = null;
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
            if(pieceComponent.fixture.testPoint(Info.mouseWorldX, Info.mouseWorldY)) {
                textureComponent.overlayTexture = hoverTexture;
                if(controller.isDragged) {
                    isDraggingPiece = true;
                    isDraggingPieceBegin = true;
                    draggingEntity = entity;
                    draggedFixture = pieceComponent.fixture;
                }
                lastTextureComponent = textureComponent;
            } else {
                textureComponent.overlayTexture = null;
            }
        } else {
            textureComponent.overlayTexture = null;
        }

    }

    private void draggingPieceBegin() {
        draggingEntity.add(engine.createComponent(DraggingComponent.class));
        draggingFixtureMask = draggedFixture.getFilterData().maskBits;
        draggedFixture.getFilterData().maskBits = Info.MASK_NOTHING;
        draggedFixture.getBody().setLinearVelocity(0, 0);
        draggedFixture.getBody().setAngularVelocity(0);
        isDraggingPieceBegin = false;

        ShipComponent shipComponent = shipMapper.get(draggingEntity);
        PieceComponent pieceComponent = pieceMapper.get(draggingEntity);
        TransformComponent transformComponent = transformMapper.get(draggingEntity);
        if(shipComponent != null) {
            entitiesFactory.detachPiece(draggingEntity, shipComponent, pieceComponent, draggingPointOfPieceFromCenter);
            draggedFixture = pieceComponent.fixture;
            transformComponent.position.z = Info.ZOrder.PIECE_DRAG.getValue();
            // starting here the shipComponent should be null... or just not usable
        }

        draggingPointOfPieceFromCenter.x = Info.mouseWorldX - pieceComponent.fixtureCenter.x;
        draggingPointOfPieceFromCenter.y = Info.mouseWorldY - pieceComponent.fixtureCenter.y;

        float draggingPointAngleFromBottomLeftRad = MathUtils.atan2(draggingPointOfPieceFromCenter.y, draggingPointOfPieceFromCenter.x) - draggedFixture.getBody().getAngle();
        float draggingPointDistFromBottomLeft = (float) Math.sqrt(draggingPointOfPieceFromCenter.x * draggingPointOfPieceFromCenter.x + draggingPointOfPieceFromCenter.y * draggingPointOfPieceFromCenter.y);
        float angleToComputePiecePosOffset = playerShipBody.getAngle() + draggingPointAngleFromBottomLeftRad + Info.rad90Deg * 2;
        draggedFixture.getBody().setTransform(draggedFixture.getBody().getPosition(), playerShipBody.getAngle());
        draggedFixture.getBody().setTransform(
                pieceComponent.fixtureCenter.x + draggingPointOfPieceFromCenter.x + MathUtils.cos(angleToComputePiecePosOffset) * draggingPointDistFromBottomLeft,
                pieceComponent.fixtureCenter.y + draggingPointOfPieceFromCenter.y + MathUtils.sin(angleToComputePiecePosOffset) * draggingPointDistFromBottomLeft,
                draggedFixture.getBody().getAngle());


        draggingPointOfPieceFromCenter.x = Info.mouseWorldX - pieceComponent.fixtureCenter.x;
        draggingPointOfPieceFromCenter.y = Info.mouseWorldY - pieceComponent.fixtureCenter.y;


        float angle = pieceComponent.fixture.getBody().getAngle();
        pieceDistanceDunnoWhatThisIsFor.x = (float) (Math.cos(angle) * Info.blockSize);
        pieceDistanceDunnoWhatThisIsFor.y = (float) (Math.sin(angle) * Info.blockSize);


    }

    private void draggingPieceUpdate() {
        float newBodyX = Info.mouseWorldX - draggingPointOfPieceFromCenter.x;
        float newBodyY = Info.mouseWorldY - draggingPointOfPieceFromCenter.y;
        draggedFixture.getBody().setTransform(newBodyX, newBodyY, draggedFixture.getBody().getAngle());

        PieceComponent pieceComponent = pieceMapper.get(draggingEntity);
        Piece draggedPiece = pieceComponent.piece;

        // the body(fixture) won't fix itself, but another entity used as a piece ghost will be fixed
        // maybe
        toAttachAnchors.clear();
        for(Piece shipPiece : playerShip.piecesArray) {
            if(shipPiece == null) continue;
            if(shipPiece.pieceComponent.fixtureCenter.dst2(pieceComponent.fixtureCenter) > (Info.maxPieceSize * Info.maxPieceSize + Info.blockSize)) continue;

            for(Anchor shipPieceAnchor : shipPiece.anchors) {
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

            entitiesFactory.adjustAttachAnchors(playerShip, pieceComponent, toAttachAnchors);
            if(toAttachAnchors.size > 0) {
                entitiesFactory.attachPiece(draggingEntity, playerShip, playerShipBody, pieceComponent, toAttachAnchors);
            }
        }


        isDraggingPiece = false;
    }

}