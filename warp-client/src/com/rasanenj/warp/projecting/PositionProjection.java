package com.rasanenj.warp.projecting;

import com.badlogic.gdx.math.Vector2;

/**
 * @author gilead
 */
public class PositionProjection {
    private long timestamp;
    private final Vector2 position = new Vector2();
    private float angle; // in degrees
    private Vector2 velocity = new Vector2();

    public void set(long timestamp, Vector2 position, float angle, Vector2 velocity)
    {
        this.timestamp = timestamp;
        this.position.set(position);
        this.angle = angle;
        this.velocity.set(velocity);
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

    public Vector2 getVelocity() {
        return velocity;
    }
}
