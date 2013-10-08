package com.rasanenj.warp.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.rasanenj.warp.Geometry;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.entities.ClientShip.TurningState;
import com.rasanenj.warp.messaging.AccelerationMessage;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.tasks.Task;

import java.util.Collection;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 *
 * TODO: ATM the update rate of this Task is the multiplier on the forces used.
 * Should be fixed somehow.
 */
public class ShipSteering extends Task {
    private static final float MESSAGES_IN_SECOND = 60f;
    private static final float STEP_LENGTH = 1f / MESSAGES_IN_SECOND;

    private final Vector2 pos = new Vector2();
    private final Vector2 tgt = new Vector2();
    private final Vector2 projectionTemp = new Vector2();

    private final Vector2 vecP = new Vector2(), vecR = new Vector2(), vecQ = new Vector2(), vecS = new Vector2();

    private final Collection<ClientShip> ships;
    private final ServerConnection connection;
    private final Vector2 corners[] = new Vector2[4];

    public ShipSteering(Collection<ClientShip> ships, ServerConnection conn) {
        super(MESSAGES_IN_SECOND);
        this.ships = ships;
        this.connection = conn;
        for (int i=0; i < 4; i++) {
            corners[i] = new Vector2();
        }
    }

    @Override
    protected void run() {
        for (ClientShip ship : ships) {
            tgt.set(ship.getTargetPos());
            if (!ship.hasTargetPos()) {
                continue;
            }

            // === FIGURE OUT THE LINEAR IMPULSE ===
            // From Steering Behaviors:
            // desired_velocity = normalize (position - target) * max_speed
            // steering = desired_velocity - velocity
            ship.getCenterPos(pos);

            // figure out the force that would be ideal to use in this situation
            tgt.sub(pos);
            pos.set(tgt);
            pos.sub(ship.getVelocity());
            pos.scl(ship.getMass());

            ship.setImpulseIdeal(pos);

            // limit the ideal force by maximum forces a ship can put out
            // this is figured out by
            ship.getCenterPos(vecP);
            vecR.set(pos);

            ship.getForceLimitCorners(corners);
            for (int i=0; i < 4; i++) {
                // first we make a vector from i'th corner to i+1'th corner
                int next = i+1;
                if (next == 4) {
                    next = 0; // last vector is from first point to the last point
                }
                vecQ.set(corners[i]);
                vecS.set(corners[next]);
                vecS.sub(corners[i]);

                if (Geometry.getIntersectionPoint(vecQ, vecS, vecP, vecR, projectionTemp)) {
                    // since the sides of a rectangle only touch each other at the corners,
                    // intersection with just one vector is fine
                    ship.getCenterPos(pos);
                    projectionTemp.sub(pos);
                    pos.set(projectionTemp);
                    break;
                }
            }

            ship.setImpulse(pos);

            // === FIGURE OUT THE ANGULAR IMPULSE ===
            float currAngle = Geometry.ensurePositiveDeg(ship.getRotation() + ship.getAngularVelocity() * STEP_LENGTH);
            float tgtAngle = Geometry.ensurePositiveDeg(tgt.angle());

            float angleDiff = Geometry.ensurePositiveDeg(currAngle - tgtAngle);
            if (angleDiff > 180) {
                angleDiff -= 360;
            }

            float change;

            float maxAccelerationInTimestep = ship.getMaxAngularAcceleration() * STEP_LENGTH;

            float minimumBreakingDistance = 0f;
            for (float velocity = ship.getMaxAngularVelocity() - maxAccelerationInTimestep;
                 velocity > 0;
                 velocity -= maxAccelerationInTimestep) {
                minimumBreakingDistance += STEP_LENGTH * velocity
                        - 1/2f * maxAccelerationInTimestep * STEP_LENGTH * STEP_LENGTH;
            }

            // log(minimumBreakingDistance + " vs " + angleDiff);

            minimumBreakingDistance /= 2.5f; // TODO: minimum breaking distance is still too high, thus it's lowered a bit here

            if (Math.abs(angleDiff) > minimumBreakingDistance) {
                ship.setTurningState(TurningState.FULL_SPEED);
                float desiredVelocity = ship.getMaxAngularVelocity();
                if (angleDiff > 0) {
                    desiredVelocity *= -1f;
                }
                change = ship.getInertia() * (desiredVelocity - ship.getAngularVelocity());
                change = MathUtils.clamp(change, -maxAccelerationInTimestep, maxAccelerationInTimestep);
            }
            else {
                if (ship.getTurningState() == TurningState.DONE_BRAKING) {
                    change = 0;
                }
                else {
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
            }

            change = MathUtils.clamp(change, -maxAccelerationInTimestep, maxAccelerationInTimestep);

            // pos.scl(STEP_LENGTH);
            AccelerationMessage msg = new AccelerationMessage(ship.getId(), change, pos.x, pos.y);

            connection.send(msg);
        }
    }
}
