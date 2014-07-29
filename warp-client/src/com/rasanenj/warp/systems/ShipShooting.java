package com.rasanenj.warp.systems;

import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.projecting.PositionProjector;
import com.rasanenj.warp.ai.ShipShootingAI;
import com.rasanenj.warp.actors.ClientShip;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.messaging.ShootRequestMessage;
import com.rasanenj.warp.tasks.IntervalTask;

/**
 * @author Janne Rasanen
 */
public class ShipShooting extends IntervalTask {
    private static final float UPDATES_IN_SECOND = 4f;

    public static final int PROJECTION_TIME_MS = 3000,
            PROJECTION_INTERVAL_MS = (int) (1000f / UPDATES_IN_SECOND),
            PROJECTION_POINTS_AMOUNT = (int) ((float) PROJECTION_TIME_MS / (float) PROJECTION_INTERVAL_MS);

    private final Array<ClientShip> ships;
    private final ServerConnection conn;
    private final ShipShootingAI shootingAI;
    private final PositionProjector projector;

    private long myId = -1;

    public ShipShooting(ShipShootingAI shootingAI, Array<ClientShip> ships, ServerConnection conn) {
        super(UPDATES_IN_SECOND);
        this.shootingAI = shootingAI;
        this.ships = ships;
        this.conn = conn;
        this.projector = new PositionProjector(UPDATES_IN_SECOND, PROJECTION_TIME_MS);
    }

    @Override
    protected void run() {
        if (!canAtLeastOneShipFire()) {
            // return; // update projections for all ships only if at least one of them can fire
            // TODO: enable this once you're confident position prediction works
        }

        for (ClientShip ship : ships) {
            projector.project(ship);
        }

        for (ClientShip ship : ships) {
            if (ship.getOwner().getId() != myId) {
                continue; // only command owned ships
            }
            if (!ship.canFire()) {
                continue;
            }

            ClientShip possibleTarget = shootingAI.getFiringTarget(ship);
            if (possibleTarget != null) {
                conn.send(new ShootRequestMessage(ship.getId(), possibleTarget.getId()));
                ship.setLastFiringTime(System.currentTimeMillis());
            }
        }
    }

    private boolean canAtLeastOneShipFire() {
        for (ClientShip ship : ships) {
            if (ship.getOwner().getId() != myId) {
                continue; // only command owned ships
            }
            if (!ship.canFire()) {
                continue;
            }
            return true;
        }
        return false;
    }







    public void setMyId(long myId) {
        this.myId = myId;
    }
}
