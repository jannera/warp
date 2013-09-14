package com.rasanenj.warp.messaging;

import com.rasanenj.warp.entities.Ship;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class CreateShipMessage extends EntityMessage {
    public CreateShipMessage(Ship ship) {
        super(ship.getId());
    }

    public CreateShipMessage(ByteBuffer msg) {
        super(msg.getLong());
    }

    @Override
    public MessageType getType() {
        return MessageType.CREATE_SHIP;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Long.SIZE/8);
        b.putLong(id);
        return b.array();
    }
}
