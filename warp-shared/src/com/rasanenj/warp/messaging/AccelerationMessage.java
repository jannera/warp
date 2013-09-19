package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class AccelerationMessage extends EntityMessage {
    private float angular, x, y;

    public AccelerationMessage(long id, float angular, float x, float y) {
        super(id);
        this.angular = angular;
        this.x = x;
        this.y = y;
    }

    public AccelerationMessage(ByteBuffer b) {
        super (b.getLong());
        this.angular = b.getFloat();
        this.x = b.getFloat();
        this.y = b.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.SET_ACCELERATION;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Float.SIZE/8 * 3 + Long.SIZE/8);
        b.putLong(id).putFloat(angular).putFloat(x).putFloat(y);
        return b.array();
    }

    public float getAngular() {
        return angular;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
