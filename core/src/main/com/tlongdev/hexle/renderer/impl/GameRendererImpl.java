package com.tlongdev.hexle.renderer.impl;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.tlongdev.hexle.Config;
import com.tlongdev.hexle.animation.TileViewAccessor;
import com.tlongdev.hexle.animation.Vector2Accessor;
import com.tlongdev.hexle.controller.GameController;
import com.tlongdev.hexle.input.HexleInputProcessor;
import com.tlongdev.hexle.model.Field;
import com.tlongdev.hexle.model.GameModel;
import com.tlongdev.hexle.model.SlideDirection;
import com.tlongdev.hexle.renderer.GameRenderer;
import com.tlongdev.hexle.view.FieldView;
import com.tlongdev.hexle.view.TileView;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Quad;

/**
 * @author longi
 * @since 2016.04.10.
 */
public class GameRendererImpl implements GameRenderer, Disposable, FieldView.OnAnimationListener,
        HexleInputProcessor.HexleInputListener, FieldView.OnSlideEndListener {

    private static final String TAG = GameRendererImpl.class.getSimpleName();

    private Logger logger;

    private GameController controller;

    private GameModel model;

    private ShapeRenderer shapeRenderer;

    private TweenManager tweenManager;

    private FieldView fieldView;

    private boolean animating = false;

    private boolean touchWhileAnim = false;

    private boolean noInput = false;

    private int animatingTiles;

    public GameRendererImpl() {
        init();
    }

    private void init() {
        logger = new Logger(TAG, Logger.DEBUG);
        shapeRenderer = new ShapeRenderer();

        Tween.registerAccessor(Vector2.class, new Vector2Accessor());
        Tween.registerAccessor(TileView.class, new TileViewAccessor());
        tweenManager = new TweenManager();

        fieldView = new FieldView(tweenManager);
        fieldView.setAnimationListener(this);
        fieldView.setSlideEndListener(this);
    }

    @Override
    public void render() {
        //Render the game
        shapeRenderer.begin(ShapeType.Filled);
        if (fieldView != null) {
            fieldView.render(shapeRenderer);
        }
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        logger.info("resize: " + width + "x" + height);
        if (fieldView != null) {
            fieldView.setDimensions(width, height);
        }
    }

    @Override
    public void dispose() {
        logger.info("dispose");
        shapeRenderer.dispose();
    }

    @Override
    public void notifyModelChanged() {
        logger.info("notifyModelChanged");
        Field field = model.getField();

        for (int i = 0; i < Config.FIELD_ROWS; i++) {
            for (int j = 0; j < Config.FIELD_COLUMNS; j++) {
                fieldView.getTileViews()[i][j].setTile(field.getTiles()[i][j]);
            }
            fieldView.getFillerTileViews()[i].setTile(field.getFillerTiles()[i]);
        }

        fieldView.animateFinishShift();
    }

    @Override
    public void update(float dt) {
        tweenManager.update(dt);
    }

    @Override
    public FieldView getFieldView() {
        return fieldView;
    }

    @Override
    public void touchDown(int x, int y) {
        logger.info("touchDown");
        //Only forward input if animation is not happening
        if (!animating) {
            noInput = false;
        } else {
            //Touch down while still animating
            touchWhileAnim = true;
        }

        if (!noInput) {
            if (fieldView != null) {
                fieldView.touchDown(x, y);
            }
        }
    }

    @Override
    public void touchUp(int x, int y) {
        logger.info("touchDown");
        //Only forward input if animation is not happening
        if (!animating) {
            //Don't forward input if dragging started while animating
            if (!touchWhileAnim) {
                noInput = false;
            }
            //Released touch while not animating
            touchWhileAnim = false;
        }

        if (!noInput && fieldView != null) {
            fieldView.touchUp();
        }
    }

    @Override
    public void touchDragged(SlideDirection direction, float dst) {
        //Only forward input if animation is not happening
        //Don't forward input if dragging started while animating
        if (!animating && !touchWhileAnim) {
            noInput = false;
        }

        if (!noInput && fieldView != null) {
            fieldView.setDrag(direction, dst);
        }
    }

    @Override
    public void onAnimationStarted() {
        logger.info("onAnimationStarted");
        animating = true;
        noInput = true;
    }

    @Override
    public void onNoMatchAnimationFinished() {
        logger.info("onNoMatchAnimationFinished");
        animating = false;
    }

    @Override
    public void onFinishShiftAnimationFinished() {
        logger.info("onFinishShiftAnimationFinished");
        animating = false;
        controller.notifyShiftAnimationFinish();
    }

    @Override
    public void onFinishNewTileGenerationAnimation() {
        logger.info("onFinishNewTileGenerationAnimation");
        animating = false;
    }

    @Override
    public void notifyNewTilesGenerated() {
        logger.info("notifyNewTilesGenerated");
        Field field = model.getField();

        Timeline timeline = Timeline.createParallel();

        for (int i = 0; i < Config.FIELD_ROWS; i++) {
            for (int j = 0; j < Config.FIELD_COLUMNS; j++) {
                TileView view = fieldView.getTileViews()[i][j];
                view.setTile(field.getTiles()[i][j]);

                int offset = field.getTiles()[i][j].getSlideInOffset();
                if (offset > 0) {

                    animatingTiles++;
                    fieldView.setAnimating(true);
                    animating = true;

                    Vector2 slideInVector = new Vector2(1, 0);
                    slideInVector.setAngleRad(MathUtils.PI / 3.0f);
                    slideInVector.setLength(offset * fieldView.getTileWidth() / 2.0f);
                    timeline.push(Tween.from(view.getCenter(), Vector2Accessor.POS_XY, 500)
                            .target(view.getCenter().x + slideInVector.x,
                                    view.getCenter().y + slideInVector.y)
                            .ease(Quad.IN));
                }
            }
            fieldView.getFillerTileViews()[i].setTile(field.getFillerTiles()[i]);
        }

        if (animating) {
            timeline.setCallback(new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    //Animation finished
                    animating = false;
                    fieldView.setAnimating(false);

                    //Notify the listener that the animation stopped
                    onFinishNewTileGenerationAnimation();
                }
            }).start(tweenManager);
        }

        fieldView.animateNewTileGeneration();
    }

    @Override
    public void onUserInputFinish() {
        logger.info("onUserInputFinish");
        controller.notifyUserInputFinish();
    }

    @Override
    public void setSlideOffset(Vector2 offset) {
        fieldView.setSlideVector(offset);
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    public void setModel(GameModel model) {
        this.model = model;
    }
}