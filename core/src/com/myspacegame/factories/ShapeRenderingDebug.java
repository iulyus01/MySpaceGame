package com.myspacegame.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.myspacegame.Info;

import java.util.*;

public class ShapeRenderingDebug {

    private static ShapeRenderer shapes;
    private static List<Runnable> runnables;
    private static List<Runnable> toRemoveRunnables;
    private static List<Integer> toRemoveRunnablesFromMap;
    private static Map<Integer, Runnable> runnableMap;

    private static int id = 0;

    public static void init(ShapeRenderer shapeRenderer) {
        shapes = shapeRenderer;
        runnables = new ArrayList<>();
        toRemoveRunnables = new ArrayList<>();
        toRemoveRunnablesFromMap = new ArrayList<>();
        runnableMap = new HashMap<>();
    }

    public static void draw() {
        // drawing
        for(Runnable r : runnables) {
            r.run();
        }
        for(var entry : runnableMap.entrySet()) {
            entry.getValue().run();
        }


        // removing
        runnables.removeAll(toRemoveRunnables);
        toRemoveRunnables.clear();
        for(Integer integer : toRemoveRunnablesFromMap) {
            runnableMap.remove(integer);
        }

    }

    public static void addToDraw(Runnable runnable) {
        runnables.add(runnable);
    }

    public static void addToDrawWithId(Runnable runnable, int id) {
        runnableMap.put(id, runnable);
    }
    public static void addToDrawWithId(Runnable runnable, int id, int timeoutMills) {
        runnableMap.put(id, runnable);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runnableMap.remove(id);
            }
        }, timeoutMills);
    }
    public static void addToDrawWithId(Runnable runnable) {
        runnableMap.put(id, runnable);
        id++;
    }

    public static void addToDrawThenRemove(Runnable runnable) {
        runnables.add(runnable);
        toRemoveRunnables.add(runnable);
    }

    public static void drawDebugLine(float x1, float y1, float x2, float y2) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(1, .3f, .95f, 1);
        shapes.rectLine(x1, y1, x2, y2, Info.blockSize / 20);
        shapes.end();
    }

    public static void drawDebugLine(float x1, float y1, float x2, float y2, Color color) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(color);
        shapes.rectLine(x1, y1, x2, y2, Info.blockSize / 16);
        shapes.end();
    }

    public static void addToDrawWithIdThenRemove(Runnable runnable, int id) {
        runnableMap.put(id, runnable);
        toRemoveRunnablesFromMap.add(id);
    }

    public static void drawDebugPolygon(float[] vertices) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(1, .3f, .95f, 1);
        for(int i = 0; i < vertices.length - 2;) {
            shapes.rectLine(vertices[i], vertices[i + 1], vertices[i + 2], vertices[i + 3], Info.blockSize / 20);
            i += 2;
        }
        shapes.end();
    }
}
