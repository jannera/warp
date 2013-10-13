package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author Janne Rasanen
 */
public class ShipDestructionMessage extends EntityMessage {
    public ShipDestructionMessage(long id) {
        super(id);
    }

    public ShipDestructionMessage(ByteBuffer b) {
        super(b.getLong());
    }

    @Override
    public MessageType getType() {
        return MessageType.SHIP_DESTRUCTION;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Long.SIZE/8);
        b.putLong(id);
        return b.array();
    }
}
