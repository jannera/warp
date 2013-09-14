package com.rasanenj.warp.systems;

import com.badlogic.gdx.math.Vector2;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.messaging.AccelerationMessage;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.tasks.Task;

import java.util.Collection;

/**
 * @author gilead
 */
public class ShipDriver extends Task {
    private static final float MESSAGES_IN_SECOND = 4f;

    private static final Vector2 pos = new Vector2();

    private final Collection<ClientShip> ships;
    private final ServerConnection connection;

    public ShipDriver(Collection<ClientShip> ships, ServerConnection conn) {
        super(MESSAGES_IN_SECOND);
        this.ships = ships;
        this.connection = conn;
    }

    @Override
    protected void run() {
        for (ClientShip ship : ships) {
            Vector2 tgt = ship.getTargetPos();
            if (Float.isNaN(tgt.x)) {
                continue;
            }

            ship.getCenterPos(pos);
            tgt.sub(pos);

            float angleDiff = ship.getRotation() - tgt.angle();
            float angular = ship.getMaxAngularForce();
            if (angleDiff < 0) {
                angular *= -1;
            }

            AccelerationMessage msg = new AccelerationMessage(ship.getId(), angular, 0, 0);
            connection.send(msg);
        }
    }
}
