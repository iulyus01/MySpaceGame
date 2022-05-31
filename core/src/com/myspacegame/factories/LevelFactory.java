package com.myspacegame.factories;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.components.*;
import com.myspacegame.components.pieces.*;
import com.myspacegame.entities.Area;
import com.myspacegame.entities.ShipData;

import java.util.List;

public class LevelFactory {

    private static LevelFactory instance = null;
    private final PooledEngine engine;
    private final World world;
    private final BackgroundFactory backgroundFactory;
    private final EntitiesFactory entitiesFactory;

    private final ComponentMapper<ShipComponent> shipMapper;

    private Area currentArea;
    private Body playerBody;
    private ShipData playerShip;

    // enemies actorId starts at 1
    private int actorId = 1;

    private LevelFactory(MainClass game, PooledEngine engine, World world) {
        this.engine = engine;
        this.world = world;

        backgroundFactory = BackgroundFactory.getInstance(game, engine);
        entitiesFactory = EntitiesFactory.getInstance(game, engine, world);

        shipMapper = ComponentMapper.getFor(ShipComponent.class);

    }

    public void setPlayerPos(Vector2 pos) {
        Family family = Family.all(PlayerComponent.class, ShipCoreComponent.class).get();
        Entity playerEntity = engine.getEntitiesFor(family).first();
        ShipData shipData = shipMapper.get(playerEntity).shipData;
        shipData.core.pieceComponent.fixture.getBody().setTransform(pos, shipData.core.pieceComponent.fixture.getBody().getAngle());
    }

    public void createBackgroundOnly(Area currentArea) {
        this.currentArea = currentArea;
        addBackground(currentArea.difficulty);
        addBackgroundLevel();

    }

    public void createLevel(Area currentArea, boolean withPlayer) {
        this.currentArea = currentArea;

        addBackground(currentArea.difficulty);

        addBackgroundLevel();

        if(withPlayer) addPlayer();

        addRockWalls();

        addRocks();

        addTeleports();

        addEnemies();

        addRandomPieces();
    }

    public void destroyLevel() {
        removeBackground();

        removeRocks();

        removeTeleports();

        removeLostPieces();

        removeShips();

        removeGameOverStuff();
    }

    public void startGame() {
        addPlayer();

        addRockWalls();

        addRocks();

        addTeleports();

        addEnemies();

        addRandomPieces();
    }

    public static LevelFactory getInstance(MainClass game, PooledEngine engine, World world) {
        if(instance == null) instance = new LevelFactory(game, engine, world);
        return instance;
    }

    public void removePlayer() {
        entitiesFactory.removePlayer();
    }

    private void addBackground(int difficulty) {
        backgroundFactory.generate(difficulty);
    }

    private void addBackgroundLevel() {
        backgroundFactory.createNumber(currentArea.id);
    }

    public void addPlayer() {
        Info.playerIsDead = false;

        List<Entity> playerEntities = entitiesFactory.createShip(Info.ships.get(3), 0, 0, Info.StaticActorIds.PLAYER.getValue(), true);
        for(Entity entity : playerEntities) engine.addEntity(entity);

        playerBody = playerEntities.get(0).getComponent(PieceComponent.class).fixture.getBody();
        playerShip = playerEntities.get(0).getComponent(ShipComponent.class).shipData;
    }

    private void addRockWalls() {
        Entity entity;
        int nr = 80;
        float posX;
        float posY;
        for(int i = 0; i < nr; i++) {
            // right
            posX = MathUtils.random(Info.worldWidthLimit, Info.worldWidthLimit + 20);
            posY = MathUtils.random(-Info.worldHeightLimit, Info.worldHeightLimit);
            entity = entitiesFactory.createRockEntity(0, posX, posY, true);
            engine.addEntity(entity);
        }
        for(int i = 0; i < nr + 8; i++) {
            // top
            posX = MathUtils.random(-Info.worldWidthLimit - 20, Info.worldWidthLimit + 20);
            posY = MathUtils.random(Info.worldHeightLimit, Info.worldHeightLimit + 20);
            entity = entitiesFactory.createRockEntity(0, posX, posY, true);
            engine.addEntity(entity);
        }
        for(int i = 0; i < nr; i++) {
            // left
            posX = MathUtils.random(-Info.worldWidthLimit - 20, -Info.worldWidthLimit);
            posY = MathUtils.random(-Info.worldHeightLimit, Info.worldHeightLimit);
            entity = entitiesFactory.createRockEntity(0, posX, posY, true);
            engine.addEntity(entity);
        }
        for(int i = 0; i < nr + 8; i++) {
            // bottom
            posX = MathUtils.random(-Info.worldWidthLimit - 20, Info.worldWidthLimit + 20);
            posY = MathUtils.random(-Info.worldHeightLimit - 20, -Info.worldHeightLimit);
            entity = entitiesFactory.createRockEntity(0, posX, posY, true);
            engine.addEntity(entity);
        }

    }

    private void addRocks() {
        Entity entity;
        int nr = 6;
        float posX;
        float posY;
        for(int i = 0; i < nr; i++) {
            // right
            posX = MathUtils.random(-Info.worldWidthLimit, Info.worldWidthLimit);
            posY = MathUtils.random(-Info.worldHeightLimit, Info.worldHeightLimit);
            if(Math.abs(posX) < Info.blockSize * 20 && Math.abs(posY) < Info.blockSize * 20) {
                i--;
                continue;
            }
            entity = entitiesFactory.createRockEntity(0, posX, posY, true);
            engine.addEntity(entity);
        }
    }

    private void addTeleports() {
        Entity entity;
        for(int i = 0; i < currentArea.to.size; i++) {
            entity = entitiesFactory.createTeleporterEntity(currentArea, currentArea.to.get(i).first, currentArea.to.get(i).second);
            engine.addEntity(entity);
        }
    }

    private void addEnemies() {
        int difficulty = currentArea.difficulty;
        int min = 0;
        int max = Info.ships.size() - 1;
        if(difficulty == 0) {
            max = 1;
        } else if(difficulty == 1) {
            max = 2;
        } else if(difficulty == 2) {
            min = 1;
            max = 3;
        } else if(difficulty == 3) {
            min = 1;
            max = 4;
        } else if(difficulty == 4) {
            min = 2;
            max = 5;
        } else if(difficulty == 5) {
            min = 3;
            max = 5;
        }

        int nr = Info.speedrun ? 1 : 3 + difficulty;
        for(int i = 0; i < nr; i++) {
            int randIndex = MathUtils.random(min, max);
            float x;
            float y;
            do {
                x = MathUtils.random(-Info.worldWidthLimit, Info.worldWidthLimit);
                y = MathUtils.random(-Info.worldHeightLimit, Info.worldHeightLimit);
            } while(Math.abs(x) < 10 && Math.abs(y) < 10);
            List<Entity> enemyEntities = entitiesFactory.createShip(Info.ships.get(randIndex), x, y, getActorId(), false);
            for(Entity entity : enemyEntities) engine.addEntity(entity);
        }
    }

    private void addRandomPieces() {
        int piecesNr = 40 + currentArea.difficulty * 10;
        for(int i = 0; i < piecesNr; i++) {
            float rand = MathUtils.random(0, 3.3f);
            Class<? extends Component> componentClass;
            if(rand <= 1) componentClass = HullPieceComponent.class;
            else if(rand <= 2) componentClass = WeaponPieceComponent.class;
            else if(rand <= 3) componentClass = ThrusterPieceComponent.class;
            else componentClass = TractorBeamPieceComponent.class;
            engine.addEntity(entitiesFactory.createPieceEntity(componentClass, true, 0, 0));
        }
    }

    private void removeBackground() {
        backgroundFactory.destroy();

    }

    private void removeRocks() {
        entitiesFactory.removeRockEntities();
    }

    private void removeTeleports() {
        entitiesFactory.removeTeleportEntities();
    }

    private void removeLostPieces() {
        entitiesFactory.removeLostPieces();
    }

    private void removeShips() {
        entitiesFactory.removeShips();
    }

    private void removeGameOverStuff() {
        entitiesFactory.removeGameOverStuff();
    }

    private int getActorId() {
        actorId++;
        return actorId - 1;
    }

    public World getWorld() {
        return world;
    }

    public PooledEngine getEngine() {
        return engine;
    }

    public Body getPlayerBody() {
        return playerBody;
    }

    public ShipData getPlayerShip() {
        return playerShip;
    }
}
