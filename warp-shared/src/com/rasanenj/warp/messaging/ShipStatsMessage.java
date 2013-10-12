package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author Janne Rasanen
 */
public class ShipStatsMessage extends Message {
    final float maxSpeed, acceleration, turnSpeed, maxHealth;

    public ShipStatsMessage(float maxSpeed, float acceleration, float turnSpeed, float maxHealth) {
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.turnSpeed = turnSpeed;
        this.maxHealth = maxHealth;
    }

    public ShipStatsMessage(ByteBuffer b) {
        maxSpeed = b.getFloat();
        acceleration = b.getFloat();
        turnSpeed = b.getFloat();
        maxHealth = b.getFloat();

    }

    @Override
    public MessageType getType() {
        return MessageType.SHIP_STATS;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Float.SIZE/8 * 6);
        b.putFloat(maxSpeed).putFloat(acceleration).putFloat(turnSpeed).putFloat(maxHealth);
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
}
