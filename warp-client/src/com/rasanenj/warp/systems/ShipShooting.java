package com.rasanenj.warp.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.PositionProjection;
import com.rasanenj.warp.ai.ShipShootingAI;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.messaging.ShootRequestMessage;
import com.rasanenj.warp.tasks.IntervalTask;

import java.util.ArrayList;

import static com.rasanenj.warp.Log.log;

/**
 * @author Janne Rasanen
 */
public class ShipShooting extends IntervalTask {
    public static final int PROJECTION_TIME_MS = 3000,
            PROJECTION_INTERVAL_MS = 1000,
            PROJECTION_POINTS_AMOUNT = PROJECTION_TIME_MS / PROJECTION_INTERVAL_MS;

    private static final int UPDATES_IN_SECOND = 1;
    private final ArrayList<ClientShip> ships;
    private final ServerConnection conn;
    private final String name;

    private long myId = -1;

    public ShipShooting(ArrayList<ClientShip> ships, ServerConnection conn, String name) {
        super(UPDATES_IN_SECOND);
        this.ships = ships;
        this.conn = conn;
        this.name = name;
    }

    private final Vector2 position = new Vector2(), change = new Vector2();

    @Override
    protected void run() {
        // update projections for all ships
        for (ClientShip ship : ships) {
            if (ship.hasSteeringTarget()) {
                projectBySteering(ship);
            }
            else {
                projectByVelocity(ship);
            }
        }

        for (ClientShip ship : ships) {
            if (ship.getOwner().getId() != myId) {
                continue; // only command owned ships
            }
            ClientShip target = ship.getFiringTarget();
            if (target == null) {
                continue;
            }
            if (!ship.canFire()) {
                continue;
            }

            log(name);
            ClientShip possibleTarget = ShipShootingAI.getFiringTarget(ship);
            if (possibleTarget != null) {
                conn.send(new ShootRequestMessage(ship.getId(), possibleTarget.getId()));
                ship.setLastFiringTime(System.currentTimeMillis());
            }
            else {
                log("decided to wait");
            }
        }
    }

    private void projectByVelocity(ClientShip ship) {
        position.set(ship.getX(), ship.getY());
        change.set(ship.getVelocity());
        change.scl(PROJECTION_INTERVAL_MS/1000);
        Array<PositionProjection> projections = ship.getProjectedPositions();
        float angle = ship.getRotation();
        String output = "projected ";
        for(int i=0; i < PROJECTION_POINTS_AMOUNT; i++) {
            position.add(change);
            projections.get(i).set(PROJECTION_INTERVAL_MS * i, position, angle);
            output += position + " ";
        }
        // log(output);
    }

    private void projectBySteering(ClientShip ship) {
        projectByVelocity(ship); // TODO
    }

    public void setMyId(long myId) {
        this.myId = myId;
    }
}
