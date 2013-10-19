package com.rasanenj.warp.messaging;

import com.rasanenj.warp.entities.ShipStats;

import java.nio.ByteBuffer;

/**
 * @author Janne Rasanen
 */
public class ShipStatsMessage extends Message {
    private final ShipStats stats;
    private final float acceleration;

    public ShipStatsMessage(ShipStats stats, float acceleration) {
        this.stats = stats;
        this.acceleration = acceleration;
    }

    public ShipStatsMessage(ByteBuffer b) {
        this.stats = new ShipStats(b);
        this.acceleration = b.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.SHIP_STATS;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(ShipStats.getLengthInBytes() + Float.SIZE/8);
        stats.encode(b);
        b.putFloat(acceleration);
        return b.array();
    }

    public ShipStats getStats() {
        return stats;
    }

    public float getAcceleration() {
        return acceleration;
    }
}
