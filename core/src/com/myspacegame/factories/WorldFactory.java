package com.myspacegame.factories;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.myspacegame.Info;
import com.myspacegame.MainClass;
import com.myspacegame.entities.Area;
import com.myspacegame.entities.ShipData;
import com.myspacegame.screens.PlayScreenHUD;

public class WorldFactory {

    private static WorldFactory instance = null;
    private final PooledEngine engine;
    private final World world;
    private final EntitiesFactory entitiesFactory;
    private final LevelFactory levelFactory;
    private PlayScreenHUD ui;

    private Array<Area> map;

    private Body playerBody;
    private ShipData playerShip;

    private WorldFactory(MainClass game, PooledEngine engine) {
        this.engine = engine;
        this.world = new World(new Vector2(0, 0), true);
        world.setContactListener(new B2dContactListener());
        world.setContactFilter(new B2dContactFilter());

        this.entitiesFactory = EntitiesFactory.getInstance(game, engine, world);
        this.levelFactory = LevelFactory.getInstance(game, engine, world);

        generateMap();
        createBackgroundOnly();
    }

    public static WorldFactory getInstance(MainClass game, PooledEngine engine) {
        if(instance == null) instance = new WorldFactory(game, engine);
        return instance;
    }

    private void generateMap() {
        map = new Array<>(true, 19, Area.class);
        map.add(new Area(0, 0, new Vector2(10, 160)));
        map.add(new Area(1, 1, new Vector2(30, 152)));
        map.add(new Area(2, 2, new Vector2(52, 135)));
        map.add(new Area(3, 2, new Vector2(86, 135)));
        map.add(new Area(4, 1, new Vector2(70, 122)));
        map.add(new Area(5, 2, new Vector2(54, 100)));
        map.add(new Area(6, 1, new Vector2(120, 110)));
        map.add(new Area(7, 3, new Vector2(132, 72)));
        map.add(new Area(8, 1, new Vector2(118, 82)));
        map.add(new Area(9, 1, new Vector2(86, 73)));
        map.add(new Area(10, 4, new Vector2(65, 60)));
        map.add(new Area(11, 0, new Vector2(40, 60)));
        map.add(new Area(12, 1, new Vector2(100, 60)));
        map.add(new Area(13, 1, new Vector2(115, 40)));
        map.add(new Area(14, 3, new Vector2(78, 33)));
        map.add(new Area(15, 2, new Vector2(55, 33)));
        map.add(new Area(16, 5, new Vector2(33, 16)));
        map.add(new Area(17, 5, new Vector2(82, 6)));
        map.add(new Area(18, 0, new Vector2(82, 6)));

        float cornerOffset = 10;
        map.get(0).to.add(new Info.Pair<>(map.get(1), new Vector2(Info.worldWidthLimit - cornerOffset, Info.worldHeightLimit - cornerOffset)));
        map.get(1).to.add(
                new Info.Pair<>(map.get(0), new Vector2(-Info.worldWidthLimit + cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(2), new Vector2(Info.worldWidthLimit - cornerOffset, Info.worldHeightLimit - cornerOffset)));
        map.get(2).to.add(
                new Info.Pair<>(map.get(1), new Vector2(-Info.worldWidthLimit + cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(3), new Vector2(Info.worldWidthLimit - cornerOffset, -10)),
                new Info.Pair<>(map.get(5), new Vector2(0, Info.worldHeightLimit - cornerOffset)));
        map.get(3).to.add(
                new Info.Pair<>(map.get(2), new Vector2(-Info.worldWidthLimit + cornerOffset, 10)),
                new Info.Pair<>(map.get(4), new Vector2(-Info.worldWidthLimit + cornerOffset, Info.worldHeightLimit - cornerOffset)),
                new Info.Pair<>(map.get(6), new Vector2(Info.worldWidthLimit - cornerOffset, Info.worldHeightLimit - cornerOffset))
        );
        map.get(4).to.add(
                new Info.Pair<>(map.get(3), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(5), new Vector2(-Info.worldWidthLimit + cornerOffset, Info.worldHeightLimit - cornerOffset)));
        map.get(5).to.add(
                new Info.Pair<>(map.get(2), new Vector2(0, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(4), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(10), new Vector2(10, Info.worldHeightLimit - cornerOffset)));
        map.get(6).to.add(
                new Info.Pair<>(map.get(3), new Vector2(-Info.worldWidthLimit + cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(7), new Vector2(20, Info.worldHeightLimit - cornerOffset)));
        map.get(7).to.add(
                new Info.Pair<>(map.get(6), new Vector2(-20, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(8), new Vector2(-Info.worldWidthLimit + cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(12), new Vector2(-Info.worldWidthLimit + cornerOffset, Info.worldHeightLimit - cornerOffset)),
                new Info.Pair<>(map.get(13), new Vector2(-20, Info.worldHeightLimit - cornerOffset))
        );
        map.get(8).to.add(
                new Info.Pair<>(map.get(7), new Vector2(Info.worldWidthLimit - cornerOffset, Info.worldHeightLimit - cornerOffset)),
                new Info.Pair<>(map.get(9), new Vector2(-Info.worldWidthLimit + cornerOffset, 0)));
        map.get(9).to.add(
                new Info.Pair<>(map.get(8), new Vector2(Info.worldWidthLimit - cornerOffset, 0)),
                new Info.Pair<>(map.get(10), new Vector2(-Info.worldWidthLimit + cornerOffset, Info.worldHeightLimit - cornerOffset)));

        map.get(10).to.add(
                new Info.Pair<>(map.get(5), new Vector2(-Info.worldWidthLimit + cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(9), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(11), new Vector2(-Info.worldWidthLimit + cornerOffset, 0)),
                new Info.Pair<>(map.get(12), new Vector2(Info.worldWidthLimit - cornerOffset, 0))
        );
        map.get(10).to.add(
                new Info.Pair<>(map.get(14), new Vector2(Info.worldWidthLimit - cornerOffset, Info.worldHeightLimit - cornerOffset)),
                new Info.Pair<>(map.get(15), new Vector2(-10, Info.worldHeightLimit - cornerOffset)));

        map.get(11).to.add(
                new Info.Pair<>(map.get(10), new Vector2(Info.worldWidthLimit - cornerOffset, 0)),
                new Info.Pair<>(null, new Vector2(0, 0))
        );
        map.get(12).to.add(
                new Info.Pair<>(map.get(7), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(10), new Vector2(-Info.worldWidthLimit + cornerOffset, 0)));
        map.get(13).to.add(
                new Info.Pair<>(map.get(7), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(14), new Vector2(-Info.worldWidthLimit + cornerOffset, 10)));
        map.get(14).to.add(
                new Info.Pair<>(map.get(10), new Vector2(-Info.worldWidthLimit + cornerOffset, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(13), new Vector2(Info.worldWidthLimit - cornerOffset, -10)),
                new Info.Pair<>(map.get(15), new Vector2(-Info.worldWidthLimit + cornerOffset, 0)),
                new Info.Pair<>(map.get(17), new Vector2(-Info.worldWidthLimit + cornerOffset, Info.worldHeightLimit - cornerOffset))
        );
        map.get(15).to.add(
                new Info.Pair<>(map.get(10), new Vector2(10, -Info.worldHeightLimit + cornerOffset)),
                new Info.Pair<>(map.get(14), new Vector2(Info.worldWidthLimit - cornerOffset, 0)),
                new Info.Pair<>(map.get(16), new Vector2(-Info.worldWidthLimit + cornerOffset, Info.worldHeightLimit - cornerOffset)));
        map.get(16).to.add(new Info.Pair<>(map.get(15), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)));
        map.get(17).to.add(new Info.Pair<>(map.get(14), new Vector2(Info.worldWidthLimit - cornerOffset, -Info.worldHeightLimit + cornerOffset)));
    }

    public void createBackgroundOnly() {
        levelFactory.createBackgroundOnly(map.first());
    }

    public void destroyPlayer() {
        levelFactory.removePlayer();
    }

    public void destroyLevel() {
        levelFactory.destroyLevel();
    }

    public void createLevel(Area area, boolean withPlayer) {
        levelFactory.createLevel(area, withPlayer);
        playerBody = levelFactory.getPlayerBody();
        playerShip = levelFactory.getPlayerShip();
    }

    public void startGame() {
        levelFactory.startGame();
        playerBody = levelFactory.getPlayerBody();
        playerShip = levelFactory.getPlayerShip();
    }

    public void setPlayerToTp(Vector2 pos) {
        levelFactory.setPlayerPos(pos);
    }

    public void gameOver(boolean winning) {
        if(winning) {
            engine.addEntity(entitiesFactory.createWinningEntity());
            levelFactory.removePlayer();
        } else {
            engine.addEntity(entitiesFactory.createLoosingEntity());
        }
        ui.toggleMenuButton(false);
    }

    public void setUI(PlayScreenHUD ui) {
        this.ui = ui;
    }

    public void dispose() {
        world.dispose();
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
