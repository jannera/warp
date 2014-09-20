package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.ai.ShipShootingAISimple;
import com.rasanenj.warp.actors.ClientShip;
import com.rasanenj.warp.messaging.Player;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.messaging.*;
import com.rasanenj.warp.systems.ShipShooting;
import com.rasanenj.warp.systems.ShipSteering;
import com.rasanenj.warp.ui.fleetbuilding.ShipBuildWindow;

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
    private GameState state = GameState.PAUSED;

    public class MyShipInfo {
        public ClientShip targetShip = null;
        public long timeToSelectNextTarget = 0;
    }

    private final long MIN_SHOOTING_TIME = 2000, MAX_SHOOTING_TIME = 12000; // in ms

    private final Array<ClientShip> allShips = new Array<ClientShip>(false, 16);
    private final Array<ClientShip> myShips = new Array<ClientShip>(false, 16);
    private final Array<ClientShip> enemyShips = new Array<ClientShip>(false, 16);
    private final Random rng = new Random();

    private final Map<Long, MyShipInfo> infos = new HashMap<Long, MyShipInfo>();

    public NPCPlayer(String host, float maxCost) {
        maxFleetCost = maxCost;
        this.delegator = new MessageDelegator();
        this.consumer = new Consumer(delegator);
        this.conn = new ServerConnection(host, delegator);
        steering = new ShipSteering(myShips, conn);
        ShipShootingAISimple shootingAI = new ShipShootingAISimple(infos);
        shooting = new ShipShooting(shootingAI, allShips, conn);
        conn.register(new ConnectionListener());
        conn.open();
    }

    public void update() {
        consumer.consumeStoredMessages();
        if (state == GameState.RUNNING) {
            chooseTargets();
            updateSteeringTarget();
            shooting.update();
            steering.update();
        }
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
        if (enemyShips.size == 0) {
            return;
        }
        for(ClientShip ship : myShips) {
            MyShipInfo info = infos.get(ship.getId());
            if (info.timeToSelectNextTarget > timenow && info.targetShip != null) {
                continue;
            }
            int i = rng.nextInt(enemyShips.size);
            ClientShip target = enemyShips.get(i);
            // TODO: perhaps pick the target based on the current hitpoints of the enemy ships?
            // TODO: perhaps pick the same target for all of the ships
            info.targetShip = target;
            info.timeToSelectNextTarget = (long) (timenow + MIN_SHOOTING_TIME + rng.nextFloat() * (MAX_SHOOTING_TIME - MIN_SHOOTING_TIME));
        }
    }

    private final float maxFleetCost;

    private class ConnectionListener implements ServerConnection.OpenCloseListener {

        @Override
        public void onOpen() {
            conn.send(new JoinServerMessage("npc", -1));
            conn.send(new JoinBattleMessage("npc", -1, -1));
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
                    log("[npc] joined in battle:" + p);
                    if (myId == -1) {
                        myId = message.getPlayerId();
                        shooting.setMyId(myId);
                    }
                    players.add(p);

                    createRandomFleet();
                }
            }
            else if (msg.getType() == Message.MessageType.CREATE_SHIP) {
                CreateShipMessage message = (CreateShipMessage) msg;
                Player owningPlayer = getPlayer(message.getOwnerId());

                ClientShip ship = new ClientShip(message.getId(), owningPlayer, message.getStats());
                ship.initProjections(ShipShooting.PROJECTION_POINTS_AMOUNT);
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

                for(Map.Entry<Long, MyShipInfo> e : infos.entrySet()) {
                    if (e.getValue().targetShip == removedShip) {
                        e.getValue().targetShip = null;
                    }
                }
            }
            else if (msg.getType() == Message.MessageType.GAME_STATE_CHANGE) {
                GameStateChangeMessage message = (GameStateChangeMessage) msg;
                state = message.getNewState();
            }
            else if (msg.getType() == Message.MessageType.DEPLOY_WARNING) {
                // aggressive NPCs aren't scared of no threat!
            }
        }

        @Override
        public Collection<Message.MessageType> getMessageTypes() {
            return Arrays.asList(Message.MessageType.UPDATE_SHIP_PHYSICS,
                    Message.MessageType.CREATE_SHIP,
                    Message.MessageType.JOIN_BATTLE,
                    Message.MessageType.SHOOT_DAMAGE,
                    Message.MessageType.SHIP_DESTRUCTION,
                    Message.MessageType.SCORE_UPDATE,
                    Message.MessageType.CREATE_SCORE_GATHERING_POINT,
                    Message.MessageType.GAME_STATE_CHANGE,
                    Message.MessageType.DEPLOY_WARNING);
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

    private static ClientShip removeShip(Array<ClientShip> ships, long id) {
        ClientShip tgt = null;
        for (ClientShip ship : ships) {
            if (ship.getId() == id) {
                tgt = ship;
                break;
            }
        }
        if (tgt != null) {
            ships.removeValue(tgt, true);
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

    private static ClientShip getShip(Array<ClientShip> ships, long id) {
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

    private void createRandomFleet() {
        log("creating random fleet");
        float totalCost;
        int fitAmount;
        ShipStats[] stats;
        int[] shipAmounts;
        do {
            fitAmount = rng.nextInt(4) + 1; // 1 -4 fits
            float[] maxCosts = new float[fitAmount];
            float costLeft = maxFleetCost * 1.2f;
            for(int i=0; i < fitAmount - 1; i++) {
                float cost = rng.nextFloat() * costLeft;
                maxCosts[i] = cost;
                costLeft -= cost;
            }
            maxCosts[fitAmount - 1] = costLeft;

            stats = new ShipStats[fitAmount];
            shipAmounts = new int[fitAmount];
            ShipBuildWindow[] allTypes = ShipBuildWindow.createAllTypes();

            for(int i=0; i < fitAmount; i++) {
                stats[i] = generateRandomFit(maxCosts[i], allTypes);
                if (stats[i] != null) {
                    float cost = stats[i].getCost();
                    shipAmounts[i] = (int) Math.floor(maxCosts[i] / cost);
                }
            }

            // calculate total cost
            totalCost = 0;
            for(int i=0; i < fitAmount; i++) {
                if (stats[i] != null) {
                    totalCost += stats[i].getCost() * shipAmounts[i];
                }
            }
        }
        while (!(totalCost < maxFleetCost * 1.2f && totalCost > maxFleetCost * 0.8f));

        Log.log("Creating fleet of about " + maxFleetCost + " points");
        for(int i=0; i < fitAmount; i++) {
            if (stats[i] != null) {
                float singleCost = stats[i].getCost();
                Log.log(shipAmounts[i] + " x " + singleCost + " = " + shipAmounts[i] * singleCost);
            }
        }
        Log.log("Total cost was " + totalCost);

        for (int i = 0; i < fitAmount; i++) {
            ShipStats s = stats[i];
            if (s != null) {
                conn.send(new ShipStatsMessage(s, myId, 420, 400, shipAmounts[i]));
            }
        }
    }

    private ShipStats generateRandomFit(float maxCost, ShipBuildWindow[] allTypes) {
        allTypes[0].resetSliders();
        float minCost = allTypes[0].getStats().getCost();
        if (minCost > maxCost) {
            return null;
        }

        float cost = Float.POSITIVE_INFINITY;
        ShipBuildWindow selection = null;
        while (cost > maxCost) {
            int type = rng.nextInt(allTypes.length);
            selection = allTypes[type];
            selection.randomizeSliders();
            cost = selection.getTotalCost();
        }
        return selection.getStats();
    }
}
