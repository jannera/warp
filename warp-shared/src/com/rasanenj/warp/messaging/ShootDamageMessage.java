package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author Janne Rasanen
 */
public class ShootDamageMessage extends EntityMessage {

    private final long target;
    private final float damage;

    public ShootDamageMessage(long shooter, long target, float damage) {
        super(shooter);
        this.target = target;
        this.damage = damage;
    }

    public ShootDamageMessage(ByteBuffer b) {
        super(b.getLong());
        this.target = b.getLong();
        this.damage = b.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.SHOOT_DAMAGE;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(2 * Long.SIZE/8 + Float.SIZE/8);
        b.putLong(id).putLong(target).putFloat(damage);
        return b.array();
    }

    public long getTarget() {
        return target;
    }

    public float getDamage() {
        return damage;
    }
}
