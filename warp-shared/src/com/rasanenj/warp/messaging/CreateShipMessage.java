package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
// TODO: this should include correct location as well
public class CreateShipMessage extends EntityMessage {
    private final float maxLinearForceRight, maxLinearForceForward, maxLinearForceBackward, maxLinearForceLeft;
    private final float maxAngularVelocity, maxVelocity, maxHealth;
    float width, height, mass, inertia;

    public CreateShipMessage(long id, float width, float height, float mass, float inertia,
                             float maxLinearForceForward, float maxLinearForceBackward,
                             float maxLinearForceLeft, float maxLinearForceRight,
                             float maxHealth, float maxVelocity, float maxAngularVelocity) {
        super(id);
        this.width = width;
        this.height = height;
        this.mass = mass;
        this.inertia = inertia;
        this.maxLinearForceForward = maxLinearForceForward;
        this.maxLinearForceBackward = maxLinearForceBackward;
        this.maxLinearForceLeft = maxLinearForceLeft;
        this.maxLinearForceRight = maxLinearForceRight;
        this.maxHealth = maxHealth;
        this.maxVelocity = maxVelocity;
        this.maxAngularVelocity = maxAngularVelocity;
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
        this.maxHealth = msg.getFloat();
        this.maxVelocity = msg.getFloat();
        this.maxAngularVelocity = msg.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.CREATE_SHIP;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Long.SIZE/8 + 11 * Long.SIZE/8);
        b.putLong(id).putFloat(width).putFloat(height).putFloat(mass).putFloat(inertia);
        b.putFloat(maxLinearForceForward).putFloat(maxLinearForceBackward).putFloat(maxLinearForceLeft).putFloat(maxLinearForceRight);
        b.putFloat(maxHealth).putFloat(maxVelocity).putFloat(maxAngularVelocity);
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

    public float getMaxLinearForceRight() {
        return maxLinearForceRight;
    }

    public float getMaxLinearForceForward() {
        return maxLinearForceForward;
    }

    public float getMaxLinearForceBackward() {
        return maxLinearForceBackward;
    }

    public float getMaxLinearForceLeft() {
        return maxLinearForceLeft;
    }

    public float getMaxAngularVelocity() {
        return maxAngularVelocity;
    }

    public float getMaxVelocity() {
        return maxVelocity;
    }

    public float getMaxHealth() {
        return maxHealth;
    }
}
