package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.rasanenj.warp.entities.ServerShip;
import com.rasanenj.warp.messaging.Message;
import com.rasanenj.warp.messaging.MessageDelegator;
import com.rasanenj.warp.messaging.Player;
import com.rasanenj.warp.tasks.RunnableFPS;
import com.rasanenj.warp.tasks.TaskHandler;
import com.rasanenj.warp.tasks.VelocityPrinter;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 *
 * Uses static frame length
 */
public class BattleLoop extends RunnableFPS {
    private final BattleServer battleServer;
    private final World world;
    private final TaskHandler taskHandler;
    private Array<Player> players = new Array<Player>(); // TODO: make it ServerPlayers
    private Array<ServerShip> ships = new Array<ServerShip>();


    static private final float FPS = 120;

    private final static int MIN_FPS = 10;
    private final static float TIME_STEP = 1f / FPS;
    private final static float MAX_STEPS = 1f + FPS / MIN_FPS;
    private final static float MAX_TIME_PER_FRAME = TIME_STEP * MAX_STEPS;
    // The suggested iteration count for Box2D is 8 for velocity and 3 for
    // position.
    private final static int VELOCITY_ITERS = 8;
    private final static int POSITION_ITERS = 3;

    public BattleLoop(MessageDelegator delegator, WSServer wsServer) {
        GdxNativesLoader.load();
        this.world = new World(new Vector2(0,0), true);
        this.battleServer = new BattleServer(this, wsServer, delegator, world);
        this.taskHandler = new TaskHandler();
        taskHandler.addToTaskList(new VelocityPrinter(ships));
    }

    public float physicsTimeLeft = 0f;

    private boolean physicsPaused = false;

    @Override
    protected float getFPS() {
        return FPS;
    }

    protected void update(float delta) {
        if (!physicsPaused) {
            physicsTimeLeft += delta;
            if (physicsTimeLeft > MAX_TIME_PER_FRAME) {
                physicsTimeLeft = MAX_TIME_PER_FRAME;
            }

            while (physicsTimeLeft >= TIME_STEP) {
                storeOldShipPositions();
                world.step(TIME_STEP, VELOCITY_ITERS, POSITION_ITERS);
                slowDownShips();
                physicsTimeLeft -= TIME_STEP;
                taskHandler.update(TIME_STEP);
            }

            for (ServerShip ship : ships) {
                if (ship.getBody().getLinearVelocity().len() > 0) {
                    // log(ship.getId() + " speed " + ship.getBody().getLinearVelocity().len() + " vs " + ship.getMaxVelocity());
                }
            }
        }

        battleServer.update();
    }

    private final Vector2 vel = new Vector2();
    private void slowDownShips() {
        for (ServerShip ship : ships) {
            vel.set(ship.getBody().getLinearVelocity());
            float speed = vel.len();
            float limit = ship.getStats().getMaxLinearVelocity();

            if (speed > limit) {
                // ship is going over the speed limit, slow it down
                float deltaVelocity = speed - limit;
                float impulseLength = deltaVelocity * ship.getMass();
                String out = vel + " -> ";
                vel.nor();
                vel.scl(-impulseLength);
                // log(out + vel);
                ship.getBody().applyLinearImpulse(vel, ship.getBody().getWorldCenter(), true);
            }
        }
    }

    private void storeOldShipPositions() {
        for (ServerShip ship : ships) {
            ship.storeOldPosition();
        }
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void addShip(ServerShip ship) {
        ships.add(ship);
    }

    public Array<Player> getPlayers() {
        return players;
    }

    public Array<ServerShip> getShips() {
        return ships;
    }

    /**
     * Do NOT store reference to the returned list
     */
    public Array<ServerShip> getShipsOwnedByPlayer(Player owner) {
        removeList.clear();

        for (ServerShip ship : ships) {
            if (ship.getPlayer() == owner) {
                removeList.add(ship);
            }
        }
        return removeList;
    }

    public void removePlayer(Player player) {
        players.removeValue(player, true);
    }

    public void removeShip(long id) {
        ServerShip s = getShip(id);
        if (s != null) {
            ships.removeValue(s, true);
        }
    }

    private Array<ServerShip> removeList = new Array<ServerShip> ();

    public void removeAllShips(Array<ServerShip> toBeRemoved) {

        ships.removeAll(toBeRemoved, true);
    }

    public ServerShip getShip(long id) {
        for (ServerShip ship : ships) {
            if (ship.getId() == id) {
                return ship;
            }
        }
        return null;
    }

    public float getRelativePhysicsTimeLeft() {
        return physicsTimeLeft / TIME_STEP;
    }

    public void sendToAll(Message msg) {
        for (Player player : players) {
            ServerPlayer serverPlayer = (ServerPlayer) player; // TODO get rid of this once messages work better
            serverPlayer.send(msg);
        }
    }

    public void sendToAll(Array<Message> messages) {
        for (Player player : players) {
            ServerPlayer serverPlayer = (ServerPlayer) player; // TODO get rid of this once messages work better
            for (Message msg : messages) {
                serverPlayer.send(msg);
            }
        }
    }

    public void setPhysicsPaused(boolean physicsPaused) {
        this.physicsPaused = physicsPaused;
    }

    public boolean isPhysicsPaused() {
        return physicsPaused;
    }

    public void disconnectEveryone() {
        for (Player player : players) {
            ServerPlayer p = (ServerPlayer) player;
            p.getConn().close();
        }
        players.clear();
    }
}
