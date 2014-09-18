package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.entities.ServerShip;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.messaging.CreateShipMessage;
import com.rasanenj.warp.messaging.ShipPhysicsMessage;
import com.rasanenj.warp.messaging.ShipStatsMessage;
import com.rasanenj.warp.tasks.IntervalTask;

import static com.rasanenj.warp.Log.log;
import static com.rasanenj.warp.Rng.getRandomFloatBetween;

/**
 * @author gilead
 */
public class DeployHandler extends IntervalTask {
    public static final float UPDATES_IN_SECOND = 3f;
    private final BattleLoop battleLoop;
    private final Vector2 pos = new Vector2();
    private final World world;

    public DeployHandler(BattleLoop battleLoop, World world) {
        super(UPDATES_IN_SECOND);
        this.battleLoop = battleLoop;
        this.world = world;
    }

    @Override
    protected void run() {
        long timeNow = System.currentTimeMillis();
        int firstFound = -1;
        if (deployments.size > 0) {
            // log(deployments.size + " deployments waiting");
        }
        // finds the first that has been in queue long enough, and since it's a sorted array,
        for (int i=0; i < deployments.size; i++) {
            Deployment d = deployments.get(i);
            if (d.deployTime < timeNow) {
                firstFound = i;
                while (i < deployments.size) {
                    d = deployments.get(i);
                    log("deploying " + d.deployTime);
                    deploy(d.msg);
                    i++;
                }
            }
        }
        if (firstFound != -1) {
            deployments.truncate(firstFound);
        }
    }

    public void add(ShipStatsMessage msg, long timeoutMS) {
        log("adding " + System.currentTimeMillis() + timeoutMS);
        deployments.add(new Deployment(msg, System.currentTimeMillis() + timeoutMS));
        deployments.sort();
    }

    private void deploy(ShipStatsMessage message) {
        ServerPlayer serverPlayer = (ServerPlayer) battleLoop.getPlayer(message.getOwnerId());

        ShipStats stats = message.getStats();
        float singleShipRadius = Math.max(stats.getWidth(), stats.getHeight());
        float fullFleetMaxRadius = (float) Math.ceil(Math.sqrt(message.getAmount()));

        // add a new ship based on the stats

        for (int i=0; i < message.getAmount(); i++) {
            pos.set(message.getX() + getRandomFloatBetween(-fullFleetMaxRadius, fullFleetMaxRadius),
                    message.getY() + getRandomFloatBetween(-fullFleetMaxRadius, fullFleetMaxRadius));
            ServerShip ship = new ServerShip(world, pos.x, pos.y, 0,
                    serverPlayer, stats);
            battleLoop.addShip(ship);
            // notify everyone about the new ship
            battleLoop.sendToAll(new CreateShipMessage(ship.getId(), ship.getPlayer().getId(), ship.getStats()));
            final float lerp1 = battleLoop.getRelativePhysicsTimeLeft();
            final float lerp2 = 1f - lerp1;
            ship.getInterpolatedPosition(pos, lerp1, lerp2);
            float angle = ship.getInterpolatedAngle(lerp1, lerp2);
            battleLoop.sendToAll(new ShipPhysicsMessage(ship.getId(), pos, angle, ship.getBody(), true));
        }
    }

    private Array<Deployment> deployments = new Array<Deployment> (true, 16);

    public boolean anyShipsWaitingDeployment() {
        return deployments.size != 0;
    }

    public void flushAllAt(long time) {
        for (Deployment d : deployments) {
            d.deployTime = time;
        }
    }

    private class Deployment implements Comparable <Deployment> {
        public final ShipStatsMessage msg;
        public long deployTime;

        private Deployment(ShipStatsMessage msg, long deployTime) {
            this.msg = msg;
            this.deployTime = deployTime;
        }

        @Override
        public int compareTo(Deployment o) {
            return (int) (o.deployTime - this.deployTime );
        }
    }
}
