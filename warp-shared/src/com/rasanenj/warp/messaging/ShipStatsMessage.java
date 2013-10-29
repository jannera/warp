package com.rasanenj.warp.messaging;

import com.rasanenj.warp.entities.ShipStats;

import java.nio.ByteBuffer;

/**
 * @author Janne Rasanen
 */
public class ShipStatsMessage extends Message {
    private final ShipStats stats;

    public ShipStatsMessage(ShipStats stats) {
        this.stats = stats;
    }

    public ShipStatsMessage(ByteBuffer b) {
        this.stats = new ShipStats(b);
    }

    @Override
    public MessageType getType() {
        return MessageType.SHIP_STATS;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(ShipStats.getLengthInBytes());
        stats.encode(b);
        return b.array();
    }

    public ShipStats getStats() {
        return stats;
    }
}
