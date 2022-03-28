package com.myspacegame;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

public class KeyboardController implements InputProcessor {

    public boolean aDown;
    public boolean dDown;
    public boolean wDown;
    public boolean sDown;

    public boolean bPressed = false;

    public boolean mouseLeft;
    public boolean mouseRight;
    public boolean mouseMiddle;
    public boolean isDragged;
    public Vector2 mouseLocation = new Vector2();

    private final OrthographicCamera camera;

    public KeyboardController(OrthographicCamera camera) {
        this.camera = camera;
    }

    @Override
    public boolean keyDown(int keycode) {
        boolean keyPressed = false;
        switch(keycode) {
            case Keys.A:
                aDown = true;
                keyPressed = true;
                break;
            case Keys.D:
                dDown = true;
                keyPressed = true;
                break;
            case Keys.W:
                wDown = true;
                keyPressed = true;
                break;
            case Keys.S:
                sDown = true;
                keyPressed = true;
                break;
        }
        return keyPressed;
    }

    @Override
    public boolean keyUp(int keycode) {
        boolean keyPressed = false;
        switch(keycode) {
            case Keys.A:
                aDown = false;
                keyPressed = true;
                break;
            case Keys.D:
                dDown = false;
                keyPressed = true;
                break;
            case Keys.W:
                wDown = false;
                keyPressed = true;
                break;
            case Keys.S:
                sDown = false;
                keyPressed = true;
                break;
            case Keys.B:
                bPressed = true;
                keyPressed = true;
                break;
        }
        return keyPressed;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        switch(button) {
            case Input.Buttons.LEFT:
                mouseLeft = true;
                break;
            case Input.Buttons.RIGHT:
                mouseRight = true;
                break;
            case Input.Buttons.MIDDLE:
                mouseMiddle = true;
                break;
        }

        mouseLocation.x = screenX;
        mouseLocation.y = screenY;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        isDragged = false;
        switch(button) {
            case Input.Buttons.LEFT:
                mouseLeft = false;
                break;
            case Input.Buttons.RIGHT:
                mouseRight = false;
                break;
            case Input.Buttons.MIDDLE:
                mouseMiddle = false;
                break;
        }

        mouseLocation.x = screenX;
        mouseLocation.y = screenY;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        isDragged = true;
        mouseLocation.x = screenX;
        mouseLocation.y = screenY;
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        mouseLocation.x = screenX;
        mouseLocation.y = screenY;
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if(amountY > 0) camera.zoom = camera.zoom * 1.08f;
        else camera.zoom = camera.zoom * .92f;

        // TODO uncomment
//        if(camera.zoom > 5) camera.zoom = 5;
        if(camera.zoom < .1f) camera.zoom = .1f;

        camera.update();
        System.out.println(camera.zoom);
        return false;
    }
}
