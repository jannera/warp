package com.rasanenj.warp.systems;

import com.badlogic.gdx.math.Vector2;
import com.rasanenj.warp.Geometry;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.messaging.AccelerationMessage;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.tasks.Task;

import java.util.Collection;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ShipDriver extends Task {
    private static final float MESSAGES_IN_SECOND = 4f;
    private static final float STEP_LENGTH = 1f / MESSAGES_IN_SECOND;

    private final Vector2 pos = new Vector2();
    private final Vector2 currVelocity = new Vector2();

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

            float currAngle = Geometry.ensurePositiveDeg(ship.getRotation());
            float tgtAngle = Geometry.ensurePositiveDeg(tgt.angle());

            float angleDiff = Geometry.ensurePositiveDeg(currAngle - tgtAngle);
            if (angleDiff > 180) {
                angleDiff -= 360;
            }

            // figure out desired speed and direction
            float dst2 = tgt.len2();

            float desiredSpeed = 0;

            if (Math.abs(angleDiff) < 15 && dst2 > 30) {
                desiredSpeed = ClientShip.MAX_SPEED;
            }

            log("current angle " + currAngle + " target angle " + tgtAngle
            + " diff " + angleDiff + " desired speed " + desiredSpeed);

            currVelocity.set(ship.getVelocity());

            float currSpeed = currVelocity.len();
            float impulseLength = (desiredSpeed - currSpeed) * ship.getMass();
            // TODO: limit the max impulse somehow
            // TODO: only try to slow down if it is possible, due to ship's current heading
            if (desiredSpeed == 0f) {
                currVelocity.nor();
                currVelocity.scl(impulseLength);
            }
            else {
                ship.getHeadingVector(currVelocity, ship.getRotation(), impulseLength);
            }

            //float angular = -1f * angleDiff * ship.getMass();
            float angular = -2 * ship.getMass() * (angleDiff/STEP_LENGTH - ship.getAngularVelocity());

            log("angular " + angular);

            // based on desired velocity and current velocity, work out impulse to send to engines
            AccelerationMessage msg = new AccelerationMessage(ship.getId(), angular, 0, 0);
            // AccelerationMessage msg = new AccelerationMessage(ship.getId(), angular, currVelocity.x, currVelocity.y);
            connection.send(msg);
        }
    }
}
