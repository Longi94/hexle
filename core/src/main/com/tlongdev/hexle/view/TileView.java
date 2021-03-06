package com.tlongdev.hexle.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.tlongdev.hexle.model.enumeration.SlideDirection;
import com.tlongdev.hexle.model.Tile;
import com.tlongdev.hexle.shape.EquilateralTriangle;

/**
 * @author longi
 * @since 2016.04.10.
 */
public class TileView implements BaseView {

    private EquilateralTriangle triangle;

    private Tile tile;

    private Vector2 center;

    private Vector2 originCenter;

    private float fullWidth;

    public TileView() {
        triangle = new EquilateralTriangle();
        center = new Vector2();
        originCenter = new Vector2();
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {

        //Still needs to update position, even if it's blank
        switch (tile.getOrientation()) {
            case UP:
                //Triangle faces up /\
                triangle.setRotation(MathUtils.PI / 2);

                //Offset the triangle so the rows properly align, remove this and you'll know what
                //I mean
                triangle.setCenter(
                        center.x,
                        center.y - ((float) Math.sqrt(3) * fullWidth / 12.0f)
                );
                break;
            case DOWN:
                //Triangle faces down \/
                triangle.setRotation(-MathUtils.PI / 2);

                //Offset the triangle so the rows properly align, remove this and you'll know what
                //I mean
                triangle.setCenter(
                        center.x,
                        center.y + ((float) Math.sqrt(3) * fullWidth / 12.0f)
                );
                break;
        }

        //Don't render anything if it's a blank
        if (tile.isBlank()) {
            return;
        }

        //Set the color
        if (tile.getTileColor() != null) {
            switch (tile.getTileColor()) {
                case RED:
                    triangle.setColor(Color.RED);
                    break;
                case GREEN:
                    triangle.setColor(Color.GREEN);
                    break;
                case BLUE:
                    triangle.setColor(Color.BLUE);
                    break;
                case CYAN:
                    triangle.setColor(Color.CYAN);
                    break;
                case MAGENTA:
                    triangle.setColor(Color.MAGENTA);
                    break;
                case YELLOW:
                    triangle.setColor(Color.YELLOW);
                    break;
            }
        }

        triangle.render(shapeRenderer);
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public void setSide(float side) {
        triangle.setSide(side);
    }

    public void setCenter(float x, float y) {
        center.x = x;
        center.y = y;
    }

    public void setCenter(Vector2 center) {
        this.center.set(center);
    }

    public Vector2 getTriangleCenter() {
        return triangle.getCenter();
    }

    public Vector2 getCenter() {
        return center;
    }

    public boolean isAffectedBySlide(TileView selectedTile, SlideDirection slideDirection) {
        return selectedTile != null &&
                tile.isAffectedBySlide(selectedTile.getTile(), slideDirection);
    }

    public Tile getTile() {
        return tile;
    }

    public float getSide() {
        return triangle.getSide();
    }

    public void setFullWidth(float fullWidth) {
        this.fullWidth = fullWidth;
    }

    public Vector2 getOriginCenter() {
        return originCenter;
    }

    public void setOriginCenter(Vector2 originCenter) {
        this.originCenter = originCenter;
    }

    public void setOriginCenter(float x, float y) {
        originCenter.x = x;
        originCenter.y = y;
    }
}
