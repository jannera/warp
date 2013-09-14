package com.rasanenj.warp.entities;

/**
 * @author gilead
 */
public class Ship extends Entity {
    protected float x,y, accX, accY;

    public Ship(float x, float y) {
        this.x = x;
        this.y = y;
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

    public void setAccX(float accX) {
        this.accX = accX;
    }

    public void setAccY(float accY) {
        this.accY = accY;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getAccX() {
        return accX;
    }

    public float getAccY() {
        return accY;
    }
}
