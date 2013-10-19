package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author Janne Rasanen
 */
public class ShipStatsMessage extends Message {
    private final float maxSpeed, acceleration, turnSpeed, maxHealth, maxAngularAcceleration;
    private final float signatureResolution;
    private final float weaponTracking, weaponSignatureRadius, weaponOptimal, weaponFalloff;

    public ShipStatsMessage(float maxSpeed, float acceleration, float turnSpeed, float maxHealth,
                            float maxAngularAcceleration, float signatureResolution,
                            float weaponTracking, float weaponSignatureRadius,
                            float weaponOptimal, float weaponFalloff) {
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.turnSpeed = turnSpeed;
        this.maxHealth = maxHealth;
        this.maxAngularAcceleration = maxAngularAcceleration;
        this.weaponTracking = weaponTracking;
        this.signatureResolution = signatureResolution;
        this.weaponSignatureRadius = weaponSignatureRadius;
        this.weaponOptimal = weaponOptimal;
        this.weaponFalloff = weaponFalloff;
    }

    public ShipStatsMessage(ByteBuffer b) {
        maxSpeed = b.getFloat();
        acceleration = b.getFloat();
        turnSpeed = b.getFloat();
        maxHealth = b.getFloat();
        maxAngularAcceleration = b.getFloat();
        this.weaponTracking = b.getFloat();
        this.signatureResolution = b.getFloat();
        this.weaponSignatureRadius = b.getFloat();
        this.weaponOptimal = b.getFloat();
        this.weaponFalloff = b.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.SHIP_STATS;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Float.SIZE/8 * 10);
        b.putFloat(maxSpeed).putFloat(acceleration).putFloat(turnSpeed).putFloat(maxHealth)
        .putFloat(maxAngularAcceleration);
        putFloats(b, weaponTracking, signatureResolution, weaponSignatureRadius, weaponOptimal, weaponFalloff);
        return b.array();
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public float getTurnSpeed() {
        return turnSpeed;
    }

    public float getMaxHealth() {
        return maxHealth;
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
