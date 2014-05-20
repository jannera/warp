package com.rasanenj.warp.ai;

import com.rasanenj.warp.actors.ClientShip;

/**
 * @author Janne Rasanen
 */
public class ShipShootingAISimple implements ShipShootingAI {
    @Override
    public ClientShip getFiringTarget(ClientShip shooter) {
        return shooter.getFiringTarget();
    }
}
