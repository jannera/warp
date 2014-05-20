package com.rasanenj.warp.ai;

import com.rasanenj.warp.actors.ClientShip;

/**
 * @author Janne Rasanen
 */
public interface ShipShootingAI {
    public abstract ClientShip getFiringTarget(ClientShip shooter);
}
