package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author Janne Rasanen
 */
public class ShipStatsMessage extends Message {
    final float maxSpeed, acceleration, turnSpeed, maxHealth, maxAngularAcceleration;

    public ShipStatsMessage(float maxSpeed, float acceleration, float turnSpeed, float maxHealth,
                            float maxAngularAcceleration) {
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.turnSpeed = turnSpeed;
        this.maxHealth = maxHealth;
        this.maxAngularAcceleration = maxAngularAcceleration;
    }

    public ShipStatsMessage(ByteBuffer b) {
        maxSpeed = b.getFloat();
        acceleration = b.getFloat();
        turnSpeed = b.getFloat();
        maxHealth = b.getFloat();
        maxAngularAcceleration = b.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.SHIP_STATS;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Float.SIZE/8 * 5);
        b.putFloat(maxSpeed).putFloat(acceleration).putFloat(turnSpeed).putFloat(maxHealth)
        .putFloat(maxAngularAcceleration);
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
}
