package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author Janne Rasanen
 */
public class ResourceUpdateMessage extends EntityMessage {
    private final float newResourceAmount;

    public ResourceUpdateMessage(long playerId, float newResourceAmount) {
        super(playerId);
        this.newResourceAmount = newResourceAmount;
    }

    public ResourceUpdateMessage(ByteBuffer b) {
        super(b.getLong());
        this.newResourceAmount = b.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.RESOURCE_UPDATE;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Long.SIZE/8 + Float.SIZE/8);
        b.putLong(id).putFloat(newResourceAmount);
        return b.array();
    }

    public float getNewResourceAmount() {
        return newResourceAmount;
    }
}
