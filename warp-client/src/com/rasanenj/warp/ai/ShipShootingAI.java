package com.rasanenj.warp.ai;

import com.rasanenj.warp.entities.ClientShip;

/**
 * @author Janne Rasanen
 */
public interface ShipShootingAI {
    public abstract ClientShip getFiringTarget(ClientShip shooter);
}
