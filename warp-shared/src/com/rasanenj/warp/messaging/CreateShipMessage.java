package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
// TODO: this should include correct location as well
public class CreateShipMessage extends EntityMessage {
    private final float maxLinearForceRight, maxLinearForceForward, maxLinearForceBackward, maxLinearForceLeft;
    float width, height, mass, inertia;

    public CreateShipMessage(long id, float width, float height, float mass, float inertia,
                             float maxLinearForceForward, float maxLinearForceBackward,
                             float maxLinearForceLeft, float maxLinearForceRight) {
        super(id);
        this.width = width;
        this.height = height;
        this.mass = mass;
        this.inertia = inertia;
        this.maxLinearForceForward = maxLinearForceForward;
        this.maxLinearForceBackward = maxLinearForceBackward;
        this.maxLinearForceLeft = maxLinearForceLeft;
        this.maxLinearForceRight = maxLinearForceRight;
    }

    public CreateShipMessage(ByteBuffer msg) {
        super(msg.getLong());
        this.width = msg.getFloat();
        this.height = msg.getFloat();
        this.mass = msg.getFloat();
        this.inertia = msg.getFloat();
        this.maxLinearForceForward = msg.getFloat();
        this.maxLinearForceBackward = msg.getFloat();
        this.maxLinearForceLeft = msg.getFloat();
        this.maxLinearForceRight = msg.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.CREATE_SHIP;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Long.SIZE/8 + 8* Long.SIZE/8);
        b.putLong(id).putFloat(width).putFloat(height).putFloat(mass).putFloat(inertia);
        b.putFloat(maxLinearForceForward).putFloat(maxLinearForceBackward).putFloat(maxLinearForceLeft).putFloat(maxLinearForceRight);
        return b.array();
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getMass() {
        return mass;
    }

    public float getInertia() {
        return inertia;
    }
}
