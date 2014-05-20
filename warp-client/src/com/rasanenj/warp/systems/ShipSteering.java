package com.rasanenj.warp.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.rasanenj.warp.Geometry;
import com.rasanenj.warp.actors.ClientShip;
import com.rasanenj.warp.messaging.AccelerationMessage;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.tasks.IntervalTask;

import java.util.Collection;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ShipSteering extends IntervalTask {
    public static final float MESSAGES_IN_SECOND = 8f;
    public static final float STEP_LENGTH = 1f / MESSAGES_IN_SECOND;

    private static final Vector2 pos = new Vector2();
    private static final Vector2 tgt = new Vector2();
    private static final Vector2 projectionTemp = new Vector2();

    private static final Vector2 vecP = new Vector2(), vecR = new Vector2(), vecQ = new Vector2(), vecS = new Vector2();

    private final Collection<ClientShip> ships;
    private final ServerConnection connection;
    private static final Vector2 corners[] = new Vector2[4];

    public ShipSteering(Collection<ClientShip> ships, ServerConnection conn) {
        super(MESSAGES_IN_SECOND);
        this.ships = ships;
        this.connection = conn;
        for (int i=0; i < 4; i++) {
            corners[i] = new Vector2();
        }
    }

    public static void targetPointSteer(ClientShip ship, SteeringResult result) {
        tgt.set(ship.getTargetPos());

        // === FIGURE OUT THE LINEAR IMPULSE ===
        // From Steering Behaviors:
        // desired_velocity = normalize (position - target) * max_speed
        // steering = desired_velocity - velocity
        ship.getCenterPos(pos);

        // figure out the force that would be ideal to use in this situation
        tgt.sub(pos);
        pos.set(tgt);
        pos.sub(ship.getVelocity());
        pos.scl(ship.getStats().getMass());

        result.idealImpulse.set(pos);

        limitByForceLimits(ship, pos);

        // === FIGURE OUT THE ANGULAR IMPULSE ===
        result.angularImpulse = getAngularImpulse(ship, tgt.angle());
        result.linearImpulse.set(pos.x, pos.y);
    }

    private static float getAngularImpulse(ClientShip ship, float tgtAngle) {
        float currAngle = Geometry.ensurePositiveDeg(ship.getRotation() + ship.getAngularVelocity() * STEP_LENGTH);
        tgtAngle = Geometry.ensurePositiveDeg(tgtAngle);

        float angleDiff = Geometry.ensurePositiveDeg(currAngle - tgtAngle);
        if (angleDiff > 180) {
            angleDiff -= 360;
        }

        angleDiff *= MathUtils.degreesToRadians;

        float change;
        float minimumBreakingDistance;

        float maxAcceleration = ship.getStats().getMaxAngularAcceleration();
        float maxVelocity = ship.getStats().getMaxAngularVelocity();

        if (ship.getAngularVelocity() != 0) {
            float decelerationTime = ship.getAngularVelocity() / maxAcceleration; // atm this calculation doesn't take time steps (STEP_LENGTH) into account
            minimumBreakingDistance = decelerationTime * ship.getAngularVelocity() - maxAcceleration * decelerationTime * decelerationTime / 2f;
        }
        else {
            float decelerationTime = maxVelocity / maxAcceleration;
            minimumBreakingDistance = decelerationTime * maxVelocity - maxAcceleration * decelerationTime * decelerationTime / 2f;
        }
        if (minimumBreakingDistance > 0) {
            float rampedSpeed = -1f * maxVelocity * angleDiff / minimumBreakingDistance;

            float desiredVelocity = MathUtils.clamp(rampedSpeed, -maxVelocity, maxVelocity);

            change = ship.getStats().getInertia() * (desiredVelocity - ship.getAngularVelocity());

            float maxAccelerationInTimestep = maxAcceleration * STEP_LENGTH;
            maxAccelerationInTimestep *= ship.getStats().getInertia();

            change = MathUtils.clamp(change, -maxAccelerationInTimestep, maxAccelerationInTimestep);
        }
        else {
            change = 0f;
        }

        return change;
    }

    // limit the given force by maximum forces a ship can put out
    private static void limitByForceLimits(ClientShip ship, Vector2 force) {
        ship.getCenterPos(vecP);
        vecR.set(force);

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
                ship.getCenterPos(force);
                projectionTemp.sub(force);
                force.set(projectionTemp);
                break;
            }
        }
    }

    private final SteeringResult result = new SteeringResult();

    public static void steer(ClientShip ship, SteeringResult result) {
        if (ship.hasDirectionTarget()) {
            directionSteer(ship, result);
        }
        else if (ship.hasTargetPos()) {
            targetPointSteer(ship, result);
        }
        else if (ship.hasOrbitTarget()) {
            orbitSteer(ship, result);
        }
    }

    @Override
    protected void run() {
        for (ClientShip ship : ships) {
            steer(ship, result);
            ship.setImpulse(result.linearImpulse);
            ship.setImpulseIdeal(result.idealImpulse);
            AccelerationMessage msg = new AccelerationMessage(ship.getId(), result.angularImpulse,
                    result.linearImpulse.x, result.linearImpulse.y);

            connection.send(msg);
        }
    }

    private static void orbitSteer(ClientShip ship, SteeringResult result) {
        ClientShip orbitTarget = ship.getOrbitShip();
        ship.getCenterPos(pos);
        orbitTarget.getCenterPos(tgt);
        float dst2 = pos.dst2(tgt);

        float desiredDst2 = ship.getOrbitDst2();

        final float orbitMargin = 0.5f * desiredDst2;
        /**
         * if margin is too high, ship starts oscillating.. if too low,
         * orbit doesn't get maintained.
         *
         * TODO: Margin should probably be calculated from
         * - mass of the ship
         * - max propulsion force
         * - max velocity
         *
         * Also the orbiting ships stats should probably somehow affect it
         */

        // right at the desired distance, the target vector is the tangent vector
        pos.sub(tgt); // get the vector from the target to this ship
        pos.nor(); // normalize it
        float rotation = -90;
        if (!ship.isOrbitCW()) {
            rotation *= -1;
        }
        pos.rotate(rotation); // rotate it 90 degrees to be tangential

        // float angle = pos.angle(); // TODO: do the angle thing here and see how the ship flies at full speed sideways

        // if not at the desired distance, correct the orbit
        // at margin distance, fly straight towards/away
        // at half-margin distance, fly 45 degrees towards/away
        // etc..
        float diff = (dst2 - desiredDst2) / orbitMargin;
        diff = MathUtils.clamp(diff, -1f, 1f);

        // log("orbiting " + diff + " away from desired distance");

        // positive diff means ship needs to get further away, negative means it needs to get closer
        rotation = -90f * diff;
        if (!ship.isOrbitCW()) {
            rotation *= -1;
        }
        pos.rotate(rotation);

        float angle = pos.angle();

        pos.scl(ship.getDesiredVelocity());
        // now pos contains the desired velocity difference between this ship and the ship that is being orbited

        tgt.set(ship.getVelocity());
        tgt.sub(orbitTarget.getVelocity());

        pos.sub(tgt); // subtract the current velocity difference

        pos.scl(ship.getStats().getMass());

        result.idealImpulse.set(pos);
        limitByForceLimits(ship, pos);

        result.angularImpulse = getAngularImpulse(ship, angle); // turn the ship
        result.linearImpulse.set(pos);
    }

    public static void directionSteer(ClientShip ship, SteeringResult result) {
        tgt.set(100, 0);
        tgt.rotate(ship.getTargetDirection());
        result.idealImpulse.set(tgt);
        limitByForceLimits(ship, tgt);

        result.angularImpulse = getAngularImpulse(ship, tgt.angle());
        result.linearImpulse.set(tgt);
    }

    public static class SteeringResult {
        final Vector2 linearImpulse = new Vector2();
        final Vector2 idealImpulse = new Vector2();
        float angularImpulse;
    }
}
