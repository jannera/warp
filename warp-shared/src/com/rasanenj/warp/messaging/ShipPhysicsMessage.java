package com.rasanenj.warp.messaging;

import com.rasanenj.warp.entities.Ship;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class ShipPhysicsMessage extends EntityMessage {
    private float x,y,accX,accY;

    public ShipPhysicsMessage(Ship ship) {
        super(ship.getId());
        this.x = ship.getX();
        this.y = ship.getY();
        this.accX = ship.getAccX();
        this.accY = ship.getAccY();
    }

    public ShipPhysicsMessage(ByteBuffer b) {
        super(b.getLong());
        this.x = b.getFloat();
        this.y = b.getFloat();
        this.accX = b.getFloat();
        this.accY = b.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.UPDATE_SHIP_PHYSICS;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Float.SIZE/8 * 4 + Long.SIZE/8);
        b.putLong(id).putFloat(x).putFloat(y).putFloat(accX).putFloat(accY);
        return b.array();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
