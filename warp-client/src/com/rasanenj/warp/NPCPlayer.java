package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.ai.ShipShootingAISimple;
import com.rasanenj.warp.actors.ClientShip;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.messaging.*;
import com.rasanenj.warp.systems.ShipShooting;
import com.rasanenj.warp.systems.ShipSteering;

import java.util.*;
import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class NPCPlayer {
    private final MessageDelegator delegator;
    private final ServerConnection conn;
    private final Consumer consumer;
    private final ShipSteering steering;
    private final ShipShooting shooting;
    private long myId = -1;
    private final Array<Player> players = new Array<Player>(true, 1);

    private final class MyShipInfo {
        public ClientShip targetShip = null;
        public long timeToSelectNextTarget = 0;
    }

    private final long MIN_SHOOTING_TIME = 2000, MAX_SHOOTING_TIME = 12000; // in ms

    private final ArrayList<ClientShip> allShips = new ArrayList<ClientShip>();
    private final ArrayList<ClientShip> myShips = new ArrayList<ClientShip>();
    private final ArrayList<ClientShip> enemyShips = new ArrayList<ClientShip>();
    private final Random rng = new Random();

    private final Map<Long, MyShipInfo> infos = new HashMap<Long, MyShipInfo>();

    public NPCPlayer(String host) {
        this.delegator = new MessageDelegator();
        this.consumer = new Consumer(delegator);
        this.conn = new ServerConnection(host, delegator);
        steering = new ShipSteering(myShips, conn);
        ShipShootingAISimple shootingAI = new ShipShootingAISimple();
        shooting = new ShipShooting(shootingAI, allShips, conn);
        conn.register(new ConnectionListener());
        conn.open();
    }

    public void update() {
        consumer.consumeStoredMessages();
        chooseTargets();
        updateSteeringTarget();
        shooting.update();
        steering.update();
    }

    private final Vector2 tmp = new Vector2();

    private void updateSteeringTarget() {
        for(ClientShip ship : myShips) {
            MyShipInfo info = infos.get(ship.getId());
            ClientShip target = info.targetShip;
            if (target == null) {
                continue;
            }
            getOptimal(ship.getX(), ship.getY(), target.getX(), target.getY(),
                    ship.getStats().getWeaponOptimal(), tmp);
            ship.setTargetPos(tmp.x, tmp.y);
        }
    }

    /**
     * Calculates the point for shooter to be, little inside the optimal
     */
    private void getOptimal(float shooterX, float shooterY, float targetX, float targetY,
                            float optimal, Vector2 result) {
        result.set(shooterX, shooterY);
        result.sub(targetX, targetY);
        result.nor();
        result.scl(optimal * 0.9f);
        result.add(targetX, targetY);
    }

    private void chooseTargets() {
        // for every ship that doesn't have a target, picks one randomly
        long timenow = System.currentTimeMillis();
        if (enemyShips.isEmpty()) {
            return;
        }
        for(ClientShip ship : myShips) {
            MyShipInfo info = infos.get(ship.getId());
            if (info.timeToSelectNextTarget > timenow && info.targetShip != null) {
                continue;
            }
            int i = rng.nextInt(enemyShips.size());
            ClientShip target = enemyShips.get(i);
            // TODO: perhaps pick the target based on the current hitpoints of the enemy ships?
            // TODO: perhaps pick the same target for all of the ships
            info.targetShip = target;
            info.timeToSelectNextTarget = (long) (timenow + MIN_SHOOTING_TIME + rng.nextFloat() * (MAX_SHOOTING_TIME - MIN_SHOOTING_TIME));
            ship.setFiringTarget(target);
        }
    }

    private static int maxFleetCost = 80;

    private class ConnectionListener implements ServerConnection.OpenCloseListener {

        @Override
        public void onOpen() {
            conn.send(new JoinServerMessage("npc", -1));
            conn.send(new JoinBattleMessage("npc", -1, -1));
            Array<ShipStats> stats = FleetStatsFetcher.parse(Constants.NPC_INVENTORY);
            float pointsLeft = maxFleetCost;

            // calculate the lowest cost of a ship in the inventory
            float minCost = Float.MAX_VALUE;
            for (int i = 0; i < stats.size; i++) {
                float cost = stats.get(i).getCost();
                if (cost < minCost) {
                    minCost = cost;
                }
            }

            // randomly select ships into fleet until all points are spent
            Array<ShipStats> result = new Array<ShipStats>();
            while(pointsLeft >= minCost) {
                int i = rng.nextInt(stats.size);
                ShipStats s = stats.get(i);
                if (s.getCost() <= pointsLeft) {
                    result.add(s);
                    pointsLeft -= s.getCost();
                }
            }

            // tell server our selection
            for (ShipStats s : result) {
                conn.send(new ShipStatsMessage(s));
            }
        }

        @Override
        public void onClose() {
        }
    }

    private class Consumer extends MessageConsumer {

        public Consumer(MessageDelegator delegator) {
            super(delegator);
        }

        @Override
        public void consume(Player player, Message msg) {
            if (msg.getType() == Message.MessageType.UPDATE_SHIP_PHYSICS) {
                ShipPhysicsMessage shipPhysicsMessage = (ShipPhysicsMessage) msg;
                ClientShip ship = getShip(shipPhysicsMessage.getId());
                if (ship == null) {
                    log("Couldn't find a ship with id " + shipPhysicsMessage.getId());
                }
                else {
                    long updateTime = shipPhysicsMessage.getThisStats().getReceived() - shipPhysicsMessage.estimateLatency();
                    ship.setPosition(shipPhysicsMessage.getX(), shipPhysicsMessage.getY());
                    ship.setRotation(shipPhysicsMessage.getAngle());
                    ship.setVelocity(shipPhysicsMessage.getVelX(), shipPhysicsMessage.getVelY(), shipPhysicsMessage.getAngularVelocity(), updateTime);
                    ship.setUpdateTime(updateTime);
                }
            }
            else if (msg.getType() == Message.MessageType.JOIN_BATTLE) {
                // we should probably do all the initializing only after receiving JOIN_BATTLE
                JoinBattleMessage message = (JoinBattleMessage) msg;
                if (message.getPlayerId() != -1) {
                    // terrible hack in order to use JoinServerMessages in both battle and chat
                    Player p = new Player(message.getPlayerName(), message.getPlayerId(), message.getColorIndex());
                    log("joined in battle:" + p);
                    if (myId == -1) {
                        myId = message.getPlayerId();
                        shooting.setMyId(myId);
                    }
                    players.add(p);
                }
            }
            else if (msg.getType() == Message.MessageType.CREATE_SHIP) {
                CreateShipMessage message = (CreateShipMessage) msg;
                Player owningPlayer = getPlayer(message.getOwnerId());

                ClientShip ship = new ClientShip(message.getId(), owningPlayer, message.getStats());
                if (message.getOwnerId() == myId) {
                    myShips.add(ship);
                    infos.put(ship.getId(), new MyShipInfo());
                }
                else {
                    enemyShips.add(ship);
                }
                allShips.add(ship);
            }
            else if (msg.getType() == Message.MessageType.SHIP_DESTRUCTION) {
                ShipDestructionMessage message = (ShipDestructionMessage) msg;
                long id = message.getId();

                ClientShip removedShip = removeShip(id);
                if (removedShip == null)
                {
                    log(Level.SEVERE, "Couldn't remove ship with id " + id);
                }

                // tell all shooting ships to stop shooting
                for (ClientShip ship : myShips) {
                    if (ship.getFiringTarget() == removedShip) {
                        ship.setFiringTarget(null);
                    }
                }
            }
        }

        @Override
        public Collection<Message.MessageType> getMessageTypes() {
            return Arrays.asList(Message.MessageType.UPDATE_SHIP_PHYSICS,
                    Message.MessageType.CREATE_SHIP,
                    Message.MessageType.JOIN_BATTLE,
                    Message.MessageType.SHOOT_DAMAGE,
                    Message.MessageType.SHIP_DESTRUCTION);
        }
    }

    private ClientShip removeShip(long id) {
        removeShip(allShips, id);
        ClientShip s = removeShip(myShips, id);
        if (s != null) {
            infos.remove(s.getId());
            return s;
        }
        return removeShip(enemyShips, id);
    }

    private static ClientShip removeShip(ArrayList<ClientShip> ships, long id) {
        ClientShip tgt = null;
        for (ClientShip ship : ships) {
            if (ship.getId() == id) {
                tgt = ship;
                break;
            }
        }
        if (tgt != null) {
            ships.remove(tgt);
            return tgt;
        }
        return null;
    }

    private ClientShip getShip(long id) {
        ClientShip s = getShip(myShips, id);
        if (s != null) {
            return s;
        }
        return getShip(enemyShips, id);
    }

    private static ClientShip getShip(ArrayList<ClientShip> ships, long id) {
        for (ClientShip ship : ships) {
            if (ship.getId() == id) {
                return ship;
            }
        }
        return null;
    }

    public Player getPlayer(long id) {
        for (Player p : players) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }
}
