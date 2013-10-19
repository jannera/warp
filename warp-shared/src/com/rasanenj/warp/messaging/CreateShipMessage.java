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
    private final long ownerId;
    private final float maxAngularAcceleration;
    float width, height, mass, inertia;
    private final float signatureResolution;
    private final float weaponTracking, weaponSignatureRadius, weaponOptimal, weaponFalloff;

    public CreateShipMessage(long id, long ownerId, float width, float height, float mass, float inertia,
                             float maxLinearForceForward, float maxLinearForceBackward,
                             float maxLinearForceLeft, float maxLinearForceRight,
                             float maxHealth, float maxVelocity, float maxAngularVelocity,
                             float maxAngularAcceleration, float signatureResolution,
                             float weaponTracking, float weaponSignatureRadius,
                             float weaponOptimal, float weaponFalloff) {
        super(id);
        this.ownerId = ownerId;
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
        this.maxAngularAcceleration = maxAngularAcceleration;
        this.weaponTracking = weaponTracking;
        this.signatureResolution = signatureResolution;
        this.weaponSignatureRadius = weaponSignatureRadius;
        this.weaponOptimal = weaponOptimal;
        this.weaponFalloff = weaponFalloff;
    }

    public CreateShipMessage(ByteBuffer msg) {
        super(msg.getLong());
        this.ownerId = msg.getLong();
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
        this.maxAngularAcceleration = msg.getFloat();
        this.weaponTracking = msg.getFloat();
        this.signatureResolution = msg.getFloat();
        this.weaponSignatureRadius = msg.getFloat();
        this.weaponOptimal = msg.getFloat();
        this.weaponFalloff = msg.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.CREATE_SHIP;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(2 * Long.SIZE/8 + 17 * Float.SIZE/8);
        // log("owner id in message " + ownerId);
        b.putLong(id).putLong(ownerId).putFloat(width).putFloat(height).putFloat(mass).putFloat(inertia);
        b.putFloat(maxLinearForceForward).putFloat(maxLinearForceBackward).putFloat(maxLinearForceLeft).putFloat(maxLinearForceRight);
        b.putFloat(maxHealth).putFloat(maxVelocity).putFloat(maxAngularVelocity).putFloat(maxAngularAcceleration);
        putFloats(b, weaponTracking, signatureResolution, weaponSignatureRadius, weaponOptimal, weaponFalloff);
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

    public long getOwnerId() {
        return ownerId;
    }

    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    public float getSignatureResolution() {
        return signatureResolution;
    }

    public float getWeaponTracking() {
        return weaponTracking;
    }

    public float getWeaponSignatureRadius() {
        return weaponSignatureRadius;
    }

    public float getWeaponOptimal() {
        return weaponOptimal;
    }

    public float getWeaponFalloff() {
        return weaponFalloff;
    }
}
