package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class AccelerationMessage extends Message {
    private final long id;
    private float angular, x, y;

    public AccelerationMessage(long id, float angular, float x, float y) {
        this.id = id;
        this.angular = angular;
        this.x = x;
        this.y = y;
    }

    public AccelerationMessage(ByteBuffer b) {
        this.id = b.getLong();
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
}
