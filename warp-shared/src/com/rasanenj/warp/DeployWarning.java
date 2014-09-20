package com.rasanenj.warp;

import com.rasanenj.warp.messaging.Player;

/**
 * @author Janne Rasanen
 */
public class DeployWarning {
    private float x, y,fleetSize;
    private long estimatedTime;
    private Player owner;

    public DeployWarning(float x, float y, float fleetSize, long estimatedTime, Player owner) {
        this.x = x;
        this.y = y;
        this.fleetSize = fleetSize;
        this.estimatedTime = estimatedTime;
        this.owner = owner;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getFleetSize() {
        return fleetSize;
    }

    public long getEstimatedTime() {
        return estimatedTime;
    }

    public Player getOwner() {
        return owner;
    }
}
