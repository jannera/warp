package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author Janne Rasanen
 */
public class ShootRequestMessage extends EntityMessage {
    private final long target;

    public ShootRequestMessage(long shooter, long target) {
        super(shooter);
        this.target = target;
    }

    public ShootRequestMessage(ByteBuffer b) {
        super(b.getLong());
        this.target = b.getLong();
    }

    @Override
    public MessageType getType() {
        return MessageType.SHOOT_REQUEST;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(2 * Long.SIZE/8);
        b.putLong(id).putLong(target);
        return b.array();
    }

    public long getTarget() {
        return target;
    }
}
