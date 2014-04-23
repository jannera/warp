package com.rasanenj.warp.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.PositionProjection;
import com.rasanenj.warp.ai.ShipShootingAI;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.messaging.ShootRequestMessage;
import com.rasanenj.warp.tasks.IntervalTask;

import java.util.ArrayList;

import static com.rasanenj.warp.Log.log;

/**
 * @author Janne Rasanen
 */
public class ShipShooting extends IntervalTask {
    private static final float UPDATES_IN_SECOND = 4f;

    public static final int PROJECTION_TIME_MS = 3000,
            PROJECTION_INTERVAL_MS = (int) (1000f / UPDATES_IN_SECOND),
            PROJECTION_POINTS_AMOUNT = (int) ((float) PROJECTION_TIME_MS / (float) PROJECTION_INTERVAL_MS);

    private final ArrayList<ClientShip> ships;
    private final ServerConnection conn;
    private final ShipShootingAI shootingAI;

    private long myId = -1;

    public ShipShooting(ShipShootingAI shootingAI, ArrayList<ClientShip> ships, ServerConnection conn) {
        super(UPDATES_IN_SECOND);
        this.shootingAI = shootingAI;
        this.ships = ships;
        this.conn = conn;
        this.simulationShip.setVisible(false);
    }

    private final Vector2 position = new Vector2(), change = new Vector2();

    @Override
    protected void run() {
        if (!canAtLeastOneShipFire()) {
            // return; // update projections for all ships only if at least one of them can fire
            // TODO: enable this once you're confident position prediction works
        }

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
            ClientShip target = ship.getFiringTarget();
            if (target == null) {
                continue;
            }
            if (!ship.canFire()) {
                continue;
            }
            return true;
        }
        return false;
    }

    private void projectByVelocity(ClientShip ship) {
        ship.getCenterPos(position);
        change.set(ship.getVelocity());
        change.scl(PROJECTION_INTERVAL_MS/1000f);
        Array<PositionProjection> projections = ship.getProjectedPositions();
        float angle = ship.getRotation();

        for(int i=0; i < PROJECTION_POINTS_AMOUNT; i++) {
            position.add(change);
            projections.get(i).set(PROJECTION_INTERVAL_MS * i, position, angle, ship.getVelocity());
            // TODO: if ships acceleration would be known, it should be used here to change the velocity
        }
    }

    ShipStats dummyStats = new ShipStats(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1, ShipStats.Shiptype.FRIGATE);
    private ClientShip simulationShip = new ClientShip(-1, null, dummyStats);

    ShipSteering.SteeringResult steeringResult = new ShipSteering.SteeringResult();

    private void projectBySteering(ClientShip ship) {
        // simulate the steering by running it with a crude physics simulation
        int projectionIndex = 0;
        float timeSimulated = 0f;
        final float PROJECTION_STEP_LENGTH = PROJECTION_INTERVAL_MS / 1000f;
        float lastProjection = 0f, lastSteering = 0f;

        simulationShip.copySimulationStats(ship);
        Array<PositionProjection> projections = ship.getProjectedPositions();

        // TODO: here we make the assumption that the earlier steering
        // has just been done, when in fact it could've been done at any time
        // thus maybe we shouldn't start the lastSteering from 0?

        while (projectionIndex < PROJECTION_POINTS_AMOUNT) {
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
                        PROJECTION_INTERVAL_MS * projectionIndex, position,
                        simulationShip.getRotation(), simulationShip.getVelocity());
                projectionIndex++;
                lastProjection = timeSimulated;
            }
        }
    }

    public void setMyId(long myId) {
        this.myId = myId;
    }
}
