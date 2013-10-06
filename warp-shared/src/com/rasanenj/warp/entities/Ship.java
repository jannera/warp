package com.rasanenj.warp.entities;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public abstract class Ship extends Entity {
    protected float x,y, velX, velY, width, height;

    public Ship(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Ship(long id) {
        super(id);
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setVelX(float velX) {
        this.velX = velX;
    }

    public void setVelY(float velY) {
        this.velY = velY;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getVelX() {
        return velX;
    }

    public float getVelY() {
        return velY;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getAngle() {
        return 0;
    }

    public abstract float getMass();
}
