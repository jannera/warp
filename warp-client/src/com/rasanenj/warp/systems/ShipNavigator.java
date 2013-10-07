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
 */
public class ShipNavigator extends Task {
    private static final float MESSAGES_IN_SECOND = 60f;
    private static final float STEP_LENGTH = 1f / MESSAGES_IN_SECOND;

    private final Vector2 pos = new Vector2();
    private final Vector2 tgt = new Vector2();
    private final Vector2 projectionTemp = new Vector2();
    private final Vector2 finalPos = new Vector2();

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

            // === FIGURE OUT THE LINEAR IMPULSE ===
            // From Steering Behaviors:
            // desired_velocity = normalize (position - target) * max_speed
            // steering = desired_velocity - velocity
            ship.getCenterPos(pos);

            tgt.sub(pos);
            pos.set(tgt);
            // pos.nor();
            // pos.scl(ship.getMaxSpeed());
            pos.sub(ship.getVelocity());
            pos.scl(ship.getMass());

            ship.setImpulseOriginal(pos);
            float scaleTo = Float.MAX_VALUE;

            // limit the force by the maximum force vectors of the Ship
            // the wanted force is projected for every four limiting vectors each ship has
            String out = pos + " -> ";
            ship.getMaxForceForward(projectionTemp);
            float limitLength = projectionTemp.len();
            out += projectionTemp + " -> ";
            float len = pos.dot(projectionTemp) / projectionTemp.len2();
            projectionTemp.scl(len);
            out += projectionTemp;
            float projectionLength = projectionTemp.len();
            if (projectionLength > limitLength) {
                if (limitLength < scaleTo) {
                    scaleTo = limitLength;
                }
            }

            finalPos.set(projectionTemp);
            /*
            ship.getMaxForceBackward(projectionTemp);
            projLen = projectionTemp.len();
            out += " back: " + projectionTemp + " -> ";
            len = pos.dot(projectionTemp) / projectionTemp.len2();
            projectionTemp.scl(len);
            projectionTemp.limit(projLen);
            out += " projected: " + projectionTemp + " -> ";
            finalPos.add(projectionTemp);
            out += projectionTemp;
            log(out);

            ship.getMaxForceRight(projectionTemp);
            projLen = projectionTemp.len();
            len = pos.dot(projectionTemp) / projectionTemp.len2();
            projectionTemp.scl(len);
            projectionTemp.limit(projLen);
            finalPos.add(projectionTemp);
            */
            ship.getMaxForceLeft(projectionTemp);
            limitLength = projectionTemp.len();
            len = pos.dot(projectionTemp) / projectionTemp.len2();
            projectionTemp.scl(len);
            // projectionTemp.limit(projLen);
            finalPos.add(projectionTemp);
            projectionLength = projectionTemp.len();
            if (projectionLength > limitLength) {
                if (limitLength < scaleTo) {
                    scaleTo = limitLength;
                }
            }
            if (scaleTo != Float.MAX_VALUE) {
                finalPos.limit(scaleTo);
            }

            pos.set(finalPos);






            // pos.set(0, 0);

            ship.setImpulse(pos);

            // === FIGURE OUT THE ANGULAR IMPULSE ===
            float currAngle = Geometry.ensurePositiveDeg(ship.getRotation() + ship.getAngularVelocity() * STEP_LENGTH);
            float tgtAngle = Geometry.ensurePositiveDeg(tgt.angle());

            float angleDiff = Geometry.ensurePositiveDeg(currAngle - tgtAngle);
            if (angleDiff > 180) {
                angleDiff -= 360;
            }

            float change;

            float maxAccelerationInTimestep = ship.maxAngularAcceleration() * STEP_LENGTH;

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

            // log(" currVel " + ship.getAngularVelocity() + " change: " + change);

            AccelerationMessage msg = new AccelerationMessage(ship.getId(), change, pos.x, pos.y);

            // log(pos.x +"," + pos.y);

            connection.send(msg);
        }
    }
}
