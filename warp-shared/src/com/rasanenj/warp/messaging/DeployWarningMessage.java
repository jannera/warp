package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author Janne Rasanen
 */
public class DeployWarningMessage extends Message {
    private final float fleetSize, x, y;
    private final int msUntil;
    private final long ownerId;

    public DeployWarningMessage(float fleetSize, float x, float y, int msUntil, long ownerId) {
        this.fleetSize = fleetSize;
        this.x = x;
        this.y = y;
        this.msUntil = msUntil;
        this.ownerId = ownerId;
    }

    public DeployWarningMessage(ByteBuffer b) {
        this.fleetSize = b.getFloat();
        this.x = b.getFloat();
        this.y = b.getFloat();
        this.msUntil = b.getInt();
        this.ownerId = b.getLong();
    }

    @Override
    public MessageType getType() {
        return MessageType.DEPLOY_WARNING;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Float.SIZE/8 * 3 + Integer.SIZE/8 + Long.SIZE/8);
        b.putFloat(fleetSize).putFloat(x).putFloat(y).putInt(msUntil).putLong(ownerId);
        return b.array();
    }

    public float getFleetSize() {
        return fleetSize;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getMsUntil() {
        return msUntil;
    }

    public long getOwnerId() {
        return ownerId;
    }
}
