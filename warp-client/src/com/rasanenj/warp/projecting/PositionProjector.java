package com.rasanenj.warp.projecting;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.actors.ClientShip;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.systems.ShipSteering;

/**
 * @author gilead
 */
public class PositionProjector {
    final int projectionIntervalMs, projectionPointsAmount;

    public PositionProjector(float updatesInSecond, long projectionTimeMs) {
        projectionIntervalMs = (int) (1000f / updatesInSecond);
        projectionPointsAmount = (int) ((float) projectionTimeMs / (float) projectionIntervalMs);
        this.simulationShip.setVisible(false);
    }

    private ShipStats dummyStats = new ShipStats(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1, ShipStats.Shiptype.FRIGATE);
    private ClientShip simulationShip = new ClientShip(-1, null, dummyStats);

    ShipSteering.SteeringResult steeringResult = new ShipSteering.SteeringResult();
    private final Vector2 position = new Vector2(), change = new Vector2();

    public void project(ClientShip ship) {
        if (ship.hasSteeringTarget()) {
            projectBySteering(ship);
        }
        else {
            projectByVelocity(ship);
        }
    }

    private void projectByVelocity(ClientShip ship) {
        ship.getCenterPos(position);
        change.set(ship.getVelocity());
        change.scl(projectionIntervalMs/1000f);
        Array<PositionProjection> projections = ship.getProjectedPositions();
        float angle = ship.getRotation();

        for(int i=0; i < projectionPointsAmount; i++) {
            position.add(change);
            projections.get(i).set(projectionIntervalMs * i, position, angle, ship.getVelocity());
            // TODO: if ships acceleration would be known, it should be used here to change the velocity
        }
    }

    private void projectBySteering(ClientShip ship) {
        // simulate the steering by running it with a crude physics simulation
        int projectionIndex = 0;
        float timeSimulated = 0f;
        final float PROJECTION_STEP_LENGTH = projectionIntervalMs / 1000f;
        float lastProjection = 0f, lastSteering = 0f;

        // todo: instead of copying, just use the same stats?
        simulationShip.copySimulationStats(ship);
        Array<PositionProjection> projections = ship.getProjectedPositions();

        // TODO: here we make the assumption that the earlier steering
        // has just been done, when in fact it could've been done at any time
        // thus maybe we shouldn't start the lastSteering from 0?

        while (projectionIndex < projectionPointsAmount) {
            // what happens next: the next steering or the next projection?
            float nextSteering = lastSteering + ShipSteering.STEP_LENGTH;
            float nextProjection = lastProjection + PROJECTION_STEP_LENGTH;
            float smaller = Math.min(nextProjection, nextSteering);
            float dTime = smaller - timeSimulated;

            // do simulation
            change.set(simulationShip.getVelocity());
            change.scl(dTime);
            float angularChange = simulationShip.getAngularVelocity() * dTime;
            simulationShip.setX(simulationShip.getX() + change.x);
            simulationShip.setY(simulationShip.getY() + change.y);
            simulationShip.setRotation(simulationShip.getRotation() + angularChange);
            timeSimulated += dTime;
            if (nextSteering < nextProjection) {
                ShipSteering.steer(simulationShip, steeringResult); // do steering

                // turn impulse into velocity change
                steeringResult.linearImpulse.scl(1f / simulationShip.getStats().getMass());
                steeringResult.angularImpulse /= simulationShip.getStats().getInertia();

                // apply effects of steering
                simulationShip.getVelocity().add(steeringResult.linearImpulse);
                simulationShip.setAngularVelocity(simulationShip.getAngularVelocity() + steeringResult.angularImpulse);

                simulationShip.getVelocity().clamp(0, simulationShip.getStats().getMaxLinearVelocity());

                lastSteering = timeSimulated;
            }
            else {
                // do projection
                simulationShip.getCenterPos(position);
                projections.get(projectionIndex).set(
                        projectionIntervalMs * projectionIndex, position,
                        simulationShip.getRotation(), simulationShip.getVelocity());
                projectionIndex++;
                lastProjection = timeSimulated;
            }
        }
    }
}
