package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.rasanenj.warp.entities.ServerShip;
import com.rasanenj.warp.messaging.MessageDelegator;
import com.rasanenj.warp.messaging.Player;

import java.util.ArrayList;

/**
 * @author gilead
 *
 * Uses static frame length
 */
public class BattleLoop implements Runnable {
    private final BattleServer battleServer;
    private final World world;
    private ArrayList<Player> players = new ArrayList<Player>(); // TODO: make it concurrent (because players can join at any time)
    // TODO: also make it ServerPlayers
    private ArrayList<ServerShip> ships = new ArrayList<ServerShip>(); // TODO: this too


    static private final float FPS = 25;
    static private final long FRAME_LENGTH = (long) (1 / FPS * 1000f);

    private final static int MIN_FPS = 10;
    private final static float TIME_STEP = 1f / FPS;
    private final static float MAX_STEPS = 1f + FPS / MIN_FPS;
    private final static float MAX_TIME_PER_FRAME = TIME_STEP * MAX_STEPS;
    // The suggested iteration count for Box2D is 8 for velocity and 3 for
    // position.
    private final static int VELOCITY_ITERS = 8;
    private final static int POSITION_ITERS = 3;

    private long lastTime, nextTime;

    public BattleLoop(MessageDelegator delegator, WSServer wsServer) {
        GdxNativesLoader.load();
        this.world = new World(new Vector2(0,0), true);
        this.battleServer = new BattleServer(this, wsServer, delegator, world);
        nextTime = System.currentTimeMillis();
        lastTime = System.currentTimeMillis();
    }

    public float physicsTimeLeft = 0f;

    @Override
    public void run() {
        while (true) {
            if (System.currentTimeMillis() > lastTime + FRAME_LENGTH) {
                long currTime = System.currentTimeMillis();
                float delta = (currTime - lastTime) / 1000f; // in seconds
                update(delta);
                lastTime = currTime;
            }
            else {

                try {
                    Thread.sleep(FRAME_LENGTH / 3);
                } catch (InterruptedException e) {
                    // do nothing
                }

            }
        }

    }

    private void update(float delta) {

        physicsTimeLeft += delta;
        if (physicsTimeLeft > MAX_TIME_PER_FRAME) {
            physicsTimeLeft = MAX_TIME_PER_FRAME;
        }

        while (physicsTimeLeft >= TIME_STEP) {
            storeOldShipPositions();
            world.step(TIME_STEP, VELOCITY_ITERS, POSITION_ITERS);
            physicsTimeLeft -= TIME_STEP;
            //TaskHandler.update(TIME_STEP);
        }

        battleServer.update();
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

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<ServerShip> getShips() {
        return ships;
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void removeAllShips(Player player) {
        ServerShip foundShip = null;
        // TODO: make this actually remove all ships, not just one
        for (ServerShip ship : ships) {
            if (ship.getPlayer() == player) {
                foundShip = ship;
                break;
            }
        }
        if (foundShip != null) {
            ships.remove(foundShip);
        }
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
}
