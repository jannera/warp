package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;

/**
 * @author gilead
 */
public class PositionProjection {
    private long timestamp;
    private final Vector2 position = new Vector2();
    private float angle; // in degrees
    // TODO: add velocity

    public void set(long timestamp, Vector2 position, float angle)
    {
        this.timestamp = timestamp;
        this.position.set(position);
        this.angle = angle;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getAngle() {
        return angle;
    }
}
