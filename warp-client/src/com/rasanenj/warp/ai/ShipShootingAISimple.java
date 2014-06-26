package com.rasanenj.warp.ai;

import com.rasanenj.warp.Log;
import com.rasanenj.warp.NPCPlayer;
import com.rasanenj.warp.actors.ClientShip;

import java.util.Map;
import java.util.logging.Level;

/**
 * @author Janne Rasanen
 */
public class ShipShootingAISimple implements ShipShootingAI {
    private final Map<Long, NPCPlayer.MyShipInfo> infos;

    @Override
    public ClientShip getFiringTarget(ClientShip shooter) {
        NPCPlayer.MyShipInfo info = infos.get(shooter.getId());
        if (info != null) {
            return info.targetShip;
        }
        else {
            Log.log(Level.SEVERE, "Couldn't find info for shooter " + shooter.getId());
            return null;
        }
    }

    public ShipShootingAISimple(Map<Long, NPCPlayer.MyShipInfo> infos) {
        this.infos = infos;
    }
}
