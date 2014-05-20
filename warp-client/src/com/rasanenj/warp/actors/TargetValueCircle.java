package com.rasanenj.warp.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * @author Janne Rasanen
 */
public class TargetValueCircle extends Image {
    private static final float CIRCLE_WIDTH = 0.25f;

    private static final Vector2 tmp = new Vector2();
    @Override
    public Actor hit(float x, float y, boolean touchable) {
        if (super.hit(x,y, touchable) != null) {
            float radius = getWidth()/2f;
            tmp.set(getX() + radius, getY() + radius);
            float dst = tmp.dst(x, y);
            if (dst < radius && dst > radius * CIRCLE_WIDTH) {
                return this;
            }
        }
        return null;
    }

    // TODO: add listener
    // TODO: add a link to the clientship
    // TODO: create one for every ClientShip
}
