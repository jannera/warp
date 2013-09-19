package com.rasanenj.warp.messaging;

import com.badlogic.gdx.physics.box2d.Body;
import com.rasanenj.warp.entities.Ship;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class ShipPhysicsMessage extends EntityMessage {
    private float x,y, velX, velY, angle, angularVelocity;

    public ShipPhysicsMessage(long id, Body body) {
        super(id);
        this.x = body.getPosition().x;
        this.y = body.getPosition().y;
        this.velX = body.getLinearVelocity().x;
        this.velY = body.getLinearVelocity().y;
        this.angle = body.getAngle();
        this.angularVelocity = body.getAngularVelocity();
    }

    public ShipPhysicsMessage(ByteBuffer b) {
        super(b.getLong());
        this.x = b.getFloat();
        this.y = b.getFloat();
        this.velX = b.getFloat();
        this.velY = b.getFloat();
        this.angle = b.getFloat();
        this.angularVelocity = b.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.UPDATE_SHIP_PHYSICS;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Float.SIZE/8 * 6 + Long.SIZE/8);
        b.putLong(id).putFloat(x).putFloat(y).putFloat(velX).putFloat(velY).putFloat(angle)
        .putFloat(angularVelocity);
        return b.array();
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

    public float getAngle() {
        return angle;
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }
}
