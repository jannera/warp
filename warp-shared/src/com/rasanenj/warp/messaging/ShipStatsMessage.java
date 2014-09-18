package com.rasanenj.warp.messaging;

import com.rasanenj.warp.entities.ShipStats;

import java.nio.ByteBuffer;

/**
 * @author Janne Rasanen
 */
public class ShipStatsMessage extends Message {
    private final ShipStats stats;
    private final long ownerId; // this is here because running NPCs in browsers messes up the connection finding in servers
    private final float y, x;
    private final int amount;

    public ShipStatsMessage(ShipStats stats, long ownerId, float x, float y, int amount) {
        this.stats = stats;
        this.ownerId = ownerId;
        this.x = x;
        this.y = y;
        this.amount = amount;
    }

    public ShipStatsMessage(ByteBuffer b) {
        this.stats = new ShipStats(b);
        this.ownerId = b.getLong();
        this.x = b.getFloat();
        this.y = b.getFloat();
        this.amount = b.getInt();
    }

    @Override
    public MessageType getType() {
        return MessageType.SHIP_STATS;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(ShipStats.getLengthInBytes() + Long.SIZE/8 + Float.SIZE/8 * 2 + Integer.SIZE/8);
        stats.encode(b);
        b.putLong(ownerId).putFloat(x).putFloat(y).putInt(amount);
        return b.array();
    }

    public ShipStats getStats() {
        return stats;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public float getY() {
        return y;
    }

    public float getX() {
        return x;
    }

    public int getAmount() {
        return amount;
    }
}
