package com.tlongdev.hexle.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Logger;
import com.tlongdev.hexle.model.SlideDirection;

/**
 * @author Long
 * @since 2016. 04. 11.
 */
public class HexleInputProcessor implements InputProcessor {

    private static final String TAG = HexleInputProcessor.class.getSimpleName();

    public static final float MIN_DRAG_DISTANCE = 50.0f;

    private Logger logger;

    private HexleInputListener listener;

    private int startX;
    private int startY;

    private SlideDirection direction = null;

    public HexleInputProcessor() {
        logger = new Logger(TAG, Logger.DEBUG);
    }

    @Override
    public boolean keyDown(int keycode) {
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (listener != null) {
            listener.touchDown(screenX, Gdx.graphics.getHeight() - screenY);
        }
        startX = screenX;
        startY = Gdx.graphics.getHeight() - screenY;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (listener != null) {
            listener.touchUp(screenX, Gdx.graphics.getHeight() - screenY);
        }
        direction = null;
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector2 dragged = new Vector2(screenX, Gdx.graphics.getHeight() - screenY);
        Vector2 start = new Vector2(startX, startY);
        float dst = start.dst(dragged);
        float angle = (start.sub(dragged)).angleRad();

        float opposite = angle + MathUtils.PI;
        if (direction == null) {
            if (dst > MIN_DRAG_DISTANCE) {
                if (angle >= -MathUtils.PI / 6.0f && angle < MathUtils.PI / 6.0f ||
                        opposite >= -MathUtils.PI / 6.0f && opposite < MathUtils.PI / 6.0f) {
                    direction = SlideDirection.EAST;
                } else if (angle >= MathUtils.PI / 6.0f && angle < MathUtils.PI / 2.0f ||
                        opposite >= MathUtils.PI / 6.0f && opposite < MathUtils.PI / 2.0f) {
                    direction = SlideDirection.NORTH_EAST;
                } else if (angle >= MathUtils.PI / 2.0f && angle < 5.0f * MathUtils.PI / 6.0f ||
                        opposite >= MathUtils.PI / 2.0f && opposite < 5.0f * MathUtils.PI / 6.0f) {
                    direction = SlideDirection.NORTH_WEST;
                }
            }
        } else {
            if (listener != null) {
                listener.touchDragged(direction, 0);
            }
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        return true;
    }

    public void setListener(HexleInputListener listener) {
        this.listener = listener;
    }

    public interface HexleInputListener {
        void touchDown(int x, int y);

        void touchUp(int x, int y);

        void touchDragged(SlideDirection direction, float dst);
    }
}
