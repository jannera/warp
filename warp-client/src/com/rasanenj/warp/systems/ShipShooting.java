package com.rasanenj.warp.systems;

import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.messaging.ShootRequestMessage;
import com.rasanenj.warp.tasks.IntervalTask;

import java.util.ArrayList;

/**
 * @author Janne Rasanen
 */
public class ShipShooting extends IntervalTask {

    private static final int UPDATES_IN_SECOND = 4;
    private final ArrayList<ClientShip> ships;
    private final ServerConnection conn;

    public ShipShooting(ArrayList<ClientShip> ships, ServerConnection conn) {
        super(UPDATES_IN_SECOND);
        this.ships = ships;
        this.conn = conn;
    }

    @Override
    protected void run() {
        for (ClientShip ship : ships) {
            ClientShip target = ship.getFiringTarget();
            if (target == null) {
                continue;
            }
            if (!ship.canFire()) {
                continue;
            }

            conn.send(new ShootRequestMessage(ship.getId(), target.getId()));
            ship.setLastFiringTime(System.currentTimeMillis());
        }
    }
}
