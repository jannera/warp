package com.rasanenj.warp;

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
    private ArrayList<Player> players = new ArrayList<Player>(); // TODO: make it concurrent (because players can join at any time)
    // TODO: also make it ServerPlayers
    private ArrayList<ServerShip> ships = new ArrayList<ServerShip>(); // TODO: this too


    static private final float FPS = 25;
    static private final long FRAME_LENGTH = (long) (1 / FPS * 1000f);

    private long lastTime, nextTime;

    public BattleLoop(MessageDelegator delegator, WSServer wsServer) {
        this.battleServer = new BattleServer(this, wsServer, delegator);
        nextTime = System.currentTimeMillis();
        lastTime = System.currentTimeMillis();
    }

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
        battleServer.update();
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
}
