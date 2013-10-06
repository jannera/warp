package com.rasanenj.warp.systems;

import com.badlogic.gdx.math.MathUtils;
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
public class ShipNavigator extends Task {
    private static final float MESSAGES_IN_SECOND = 5f;
    private static final float STEP_LENGTH = 1f / MESSAGES_IN_SECOND;

    private final Vector2 pos = new Vector2();
    private final Vector2 tgt = new Vector2();

    private final Collection<ClientShip> ships;
    private final ServerConnection connection;
    public float singlePulse;
    public boolean stop = false;

    public ShipNavigator(Collection<ClientShip> ships, ServerConnection conn) {
        super(MESSAGES_IN_SECOND);
        this.ships = ships;
        this.connection = conn;
    }

    @Override
    protected void run() {
        for (ClientShip ship : ships) {
            tgt.set(ship.getTargetPos());
            if (!ship.hasTargetPos()) {
                continue;
            }

            // From Steering Behaviors:
            // desired_velocity = normalize (position - target) * max_speed
            // steering = desired_velocity - velocity
            ship.getCenterPos(pos);

            tgt.sub(pos);
            pos.set(tgt);
            pos.nor();
            pos.scl(ship.getMaxSpeed());
            pos.sub(ship.getVelocity());
            pos.scl(ship.getMass());
            // TODO: limit the force by the maximum force vectors of the Ship

            // pos.set(0, 0);

            ship.setImpulse(pos);

            float currAngle = Geometry.ensurePositiveDeg(ship.getRotation() + ship.getAngularVelocity() * STEP_LENGTH);
            float tgtAngle = Geometry.ensurePositiveDeg(tgt.angle());

            float angleDiff = Geometry.ensurePositiveDeg(currAngle - tgtAngle);
            if (angleDiff > 180) {
                angleDiff -= 360;
            }


            float change;

            float maxAccelerationInTimestep = ship.maxAngularAcceleration() * STEP_LENGTH;

            float minimumBreakingDistance = 5f;

            if (Math.abs(angleDiff) > minimumBreakingDistance) {
                ship.setStoppingImpulseSent(false);
                float desiredVelocity = ship.getMaxAngularVelocity();
                if (angleDiff > 0) {
                    desiredVelocity *= -1f;
                }
                change = ship.getInertia() * (desiredVelocity - ship.getAngularVelocity());
                change = MathUtils.clamp(change, -maxAccelerationInTimestep, maxAccelerationInTimestep);
            }
            else {
                if (ship.isStoppingImpulseSent()) {
                    continue;
                }
                change = -1f * ship.getInertia() * ship.getAngularVelocity();
                // TODO: this breaking should be done in chunks, in the max size of the maxAccelerationInTimestep
                ship.setStoppingImpulseSent(true);
            }

            /*
            // Below is an attempt at doing the braking correctly in parts.. does not work atm.
            // Also estimates minimumBrakingDistance way too high

            float maxAcceleration = ship.maxAngularAcceleration();
            float maxAccelerationInTimestep = ship.maxAngularAcceleration() * STEP_LENGTH;
            float maxVelocity = ship.getMaxAngularVelocity();
            float timeToFullStop = maxVelocity / maxAcceleration;
            float minimumBreakingDistance = maxVelocity * timeToFullStop
                    - 1/2f * maxAcceleration * timeToFullStop * timeToFullStop
                     + STEP_LENGTH * maxVelocity
                    ;

            log (minimumBreakingDistance + " vs " + Math.abs(angleDiff));

            // minimumBreakingDistance = 5f;

            // log (minimumBreakingDistance + " vs " + Math.abs(angleDiff));

            if (Math.abs(angleDiff) > minimumBreakingDistance) {
                ship.setTurningState(TurningState.FULL_SPEED);
                float desiredVelocity = ship.getMaxAngularVelocity();
                if (angleDiff > 0) {
                    desiredVelocity *= -1f;
                }
                change = ship.getInertia() * (desiredVelocity - ship.getAngularVelocity());
            }
            else {
                if (ship.getTurningState() == TurningState.DONE_BRAKING) {
                    continue;
                }
                if (ship.getTurningState() == TurningState.FULL_SPEED) {
                    ship.setBrakingLeft(-1f * ship.getInertia() * ship.getAngularVelocity());
                    ship.setTurningState(TurningState.BRAKING);
                }

                change = ship.getBrakingLeft();
                if (Math.abs(change) < maxAccelerationInTimestep) {
                    ship.setTurningState(TurningState.DONE_BRAKING);
                }
                change = MathUtils.clamp(change, -maxAccelerationInTimestep, maxAccelerationInTimestep);
                ship.setBrakingLeft(ship.getBrakingLeft() - change);
            }

            change = MathUtils.clamp(change, -maxAccelerationInTimestep, maxAccelerationInTimestep);
             */
            log(" currVel " + ship.getAngularVelocity() + " change: " + change);

            AccelerationMessage msg = new AccelerationMessage(ship.getId(), change, pos.x, pos.y);

            connection.send(msg);
        }
    }
}
