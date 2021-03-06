package com.rasanenj.warp.messaging;

import com.rasanenj.warp.entities.ShipStats;

import java.nio.ByteBuffer;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
// TODO: this should include correct location as well
public class CreateShipMessage extends EntityMessage {
    private final long ownerId;

    private final ShipStats stats;

    public CreateShipMessage(long id, long ownerId, ShipStats stats) {
        super(id);
        this.ownerId = ownerId;
        this.stats = stats;
    }

    public CreateShipMessage(ByteBuffer msg) {
        super(msg.getLong());
        this.ownerId = msg.getLong();
        this.stats = new ShipStats(msg);
    }

    @Override
    public MessageType getType() {
        return MessageType.CREATE_SHIP;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(2 * Long.SIZE/8 + ShipStats.getLengthInBytes());
        // log("owner id in message " + ownerId);
        b.putLong(id).putLong(ownerId);
        stats.encode(b);
        return b.array();
    }

    public long getOwnerId() {
        return ownerId;
    }

    public ShipStats getStats() {
        return stats;
    }
}
