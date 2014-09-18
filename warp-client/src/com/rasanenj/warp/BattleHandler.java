package com.rasanenj.warp;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.ai.ShipShootingAIDecisionTree;
import com.rasanenj.warp.actors.ClientShip;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.messaging.*;
import com.rasanenj.warp.projecting.MaxOrbitVelocityCalculator;
import com.rasanenj.warp.screens.BattleScreen;
import com.rasanenj.warp.screens.LobbyScreen;
import com.rasanenj.warp.systems.ShipShooting;
import com.rasanenj.warp.systems.ShipSteering;
import com.rasanenj.warp.tasks.*;
import com.rasanenj.warp.ui.fleetbuilding.FleetBuildWindow;

import java.util.*;
import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

/**
 * Handles UI and messaging to and from server.
 *
 * @author gilead
 */
public class BattleHandler {
    private final BattleMessageConsumer consumer;
    private final ShipClickListener shipClickListener;
    private final TaskHandler taskHandler;
    private final Array<Player> players = new Array<Player>(true, 1);
    private final ShipHover hoverOverShipListener;
    private final ShipShooting shipShooting;
    private final ShipTextUpdater shipTextUpdater;
    private final ManualSteeringTask manualSteeringTask;
    private final LobbyScreen lobbyScreen;
    private final OrbitUIHandler orbitUIHandler;
    private final MaxOrbitVelocityCalculator orbitVelocityCalc;

    private long myId = -1;
    private Array<NPCPlayer> npcPlayers = new Array<NPCPlayer>(false, 0);
    private final ShipSelection selection = new ActiveShipSelection();
    private MouseState mouseState = MouseState.DEFAULT;
    private final Array<Array<ClientShip>> shipQuickGroups = new Array<Array<ClientShip>>(true, 9);
    private final Array<Array<ClientShip>> enemyGroups = new Array<Array<ClientShip>>(true, 9);

    private final Statistics statistics = new Statistics();
    private final AverageDpsCalculator dpsCalculator;
    private GameState state = GameState.PAUSED;

    public BattleHandler(BattleScreen screen, ServerConnection conn, LobbyScreen lobbyScreen) {
        conn.register(new ConnectionListener());
        this.screen = screen;
        this.conn = conn;
        this.shipClickListener = new ShipClickListener();
        screen.getStage().addListener(new StageListener());
        shipSteering = new ShipSteering(ships, conn);
        ShipShootingAIDecisionTree shootingAI = new ShipShootingAIDecisionTree(ships);
        shipShooting = new ShipShooting(shootingAI, ships, conn);
        this.consumer = new BattleMessageConsumer(conn.getDelegator());
        this.taskHandler = new TaskHandler();
        this.hoverOverShipListener = new ShipHover();
        this.shipTextUpdater = new ShipTextUpdater(ships, selection, true, true);
        taskHandler.addToTaskList(shipTextUpdater);
        this.manualSteeringTask = new ManualSteeringTask(selection, screen);
        taskHandler.addToTaskList(manualSteeringTask);
        this.lobbyScreen = lobbyScreen;
        this.orbitUIHandler = new OrbitUIHandler(taskHandler, screen.getCam(), ships, selection);
        screen.setOrbitUIHandler(orbitUIHandler);
        for (int i=0; i < 9; i++) {
            shipQuickGroups.add(new Array<ClientShip>(false, 0));
        }
        dpsCalculator = new AverageDpsCalculator(statistics, 10000);
        orbitVelocityCalc = new MaxOrbitVelocityCalculator(15000, 16, screen);

        FleetBuildWindow deployWindow = screen.getDeployWindow();
        deployWindow.getStartFight().addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startDeployMode();
            }
        });
    }

    public GameState getState() {
        return state;
    }

    private enum MouseState {
        DEFAULT, DIRECTION, GO_TO, ORBIT_CW, ORBIT_CCW, DEPLOY;

        public boolean isOrbit() {
            return this == ORBIT_CCW || this == ORBIT_CW;
        }
    }

    public void createNPC() {
        this.npcPlayers.add(new NPCPlayer(ClientUtility.getHost(), BuildStats.totalCost));
    }

    private class BattleMessageConsumer extends MessageConsumer {
        public BattleMessageConsumer(MessageDelegator delegator) {
            super(delegator);
        }

        private boolean firstPosSet = false;

        @Override
        public void consume(Player player, Message msg) {
            if (msg.getType() == Message.MessageType.UPDATE_SHIP_PHYSICS) {
                ShipPhysicsMessage shipPhysicsMessage = (ShipPhysicsMessage) msg;
                ClientShip ship = getShip(shipPhysicsMessage.getId());
                if (ship == null) {
                    log("Couldn't find a ship with id " + shipPhysicsMessage.getId());
                }
                else {
                    long updateTime = shipPhysicsMessage.getThisStats().getReceived()
                            - shipPhysicsMessage.estimateLatency();
                    ship.setLastServerPosition(shipPhysicsMessage.getX(), shipPhysicsMessage.getY(), shipPhysicsMessage.getAngle());
                    if (shipPhysicsMessage.isTeleport()) {
                        ship.setPosition(shipPhysicsMessage.getX(), shipPhysicsMessage.getY());
                        ship.setRotation(shipPhysicsMessage.getAngle());
                        log("teleported");
                    }
                    ship.setVelocity(shipPhysicsMessage.getVelX(), shipPhysicsMessage.getVelY(), shipPhysicsMessage.getAngularVelocity(), updateTime);
                    ship.setVertices(shipPhysicsMessage.getVertices());
                    ship.setUpdateTime(updateTime);
                    if (!firstPosSet && ship.getOwner().getId() == myId) {
                        ship.updatePos(updateTime);
                        ship.getCenterPos(tmp);
                        screen.setCameraPos(tmp.x, tmp.y);
                        firstPosSet = true;
                    }
                }
            }
            else if (msg.getType() == Message.MessageType.CREATE_SHIP) {
                CreateShipMessage message = (CreateShipMessage) msg;
                Player owningPlayer = getPlayer(message.getOwnerId());

                ShipStats stats = message.getStats();
                ClientShip ship = new ClientShip(message.getId(), owningPlayer, stats);
                // todo: instead of calculating orbit velocities for every ship,
                // todo: we should only calculate once per unique ship stats
                orbitVelocityCalc.calcOrbitingVelocities(ship);
                ship.clearAllSteering();
                ship.initProjections(ShipShooting.PROJECTION_POINTS_AMOUNT);
                ships.add(ship);
                screen.getStage().addActor(ship);
                ship.getClickRegionImage().addListener(shipClickListener);
                ship.getClickRegionImage().addListener(hoverOverShipListener);
                ship.setZIndex(ZOrder.ship.ordinal());


                if (owningPlayer == null) {
                    log(Level.SEVERE, "Couldn't find player with id: " + message.getOwnerId());
                }
                else {
                    ship.setSelected(false);
                }

                // if these are my ships, put ships with the same stats into groups
                if (message.getOwnerId() == myId) {
                    for(Array<ClientShip> group : shipQuickGroups) {
                        if (group.size == 0 ||
                            group.get(0).getStats().equals(ship.getStats())) {
                            // if found an empty group or a group that has similar ships
                            group.add(ship);
                            break;
                        }
                    }
                }
                else {
                    boolean foundGroup = false;
                    for(Array<ClientShip> group : enemyGroups) {
                        if(group.size == 0 ||
                           group.get(0).getStats().equals(ship.getStats()) &&
                           group.get(0).getOwner().getId() == ship.getOwner().getId()) {
                            group.add(ship);
                            foundGroup = true;
                            break;
                        }
                    }
                    if (!foundGroup) {
                        Array<ClientShip> group = new Array<ClientShip>(false, 1);
                        group.add(ship);
                        enemyGroups.add(group);
                    }
                }

                // instead of calling this, we could just tell ShipShootingAIDecisionTree to add this ship to all relevant trees
                dirtyAllShootingDecisionTrees();
            }

            else if (msg.getType() == Message.MessageType.JOIN_BATTLE) {
                JoinBattleMessage message = (JoinBattleMessage) msg;
                if (message.getPlayerId() != -1) {
                    // terrible hack in order to use JoinServerMessages in both battle and chat
                    Player p = new Player(message.getPlayerName(), message.getPlayerId(), message.getColorIndex());
                    log("joined in battle:" + p);
                    if (myId == -1) {
                        myId = message.getPlayerId();
                        manualSteeringTask.setMyId(myId);
                        shipShooting.setMyId(myId);
                        dpsCalculator.setPlayerId(myId);
                    }
                    players.add(p);
                }
            }

            else if (msg.getType() == Message.MessageType.SHOOT_DAMAGE) {
                ShootDamageMessage message = (ShootDamageMessage) msg;
                ClientShip target = getShip(message.getTarget());
                ClientShip shooter = getShip(message.getId());
                if (target == null) {
                    log("Couldn't find target with id " + message.getTarget());
                }
                else {
                    target.reduceHealth(message.getDamage());
                    screen.addLaserBeam(shooter, target, message.getDamage() > 0);
                    screen.addDamageText(target, message.getDamage(), message.getChance());
                    // screen.addDamageProjectile(target, message.getDamage(), shooter.getX(), shooter.getY());
                    statistics.storeDamage(shooter.getId(), target.getId(), shooter.getOwner().getId(),
                            target.getOwner().getId(), System.currentTimeMillis(), message.getDamage());
                }
            }

            else if (msg.getType() == Message.MessageType.SHIP_DESTRUCTION) {
                ShipDestructionMessage message = (ShipDestructionMessage) msg;
                long id = message.getId();

                ClientShip removedShip = removeShip(id);
                if (removedShip == null)
                {
                    log(Level.SEVERE, "Couldn't remove ship with id " + id);
                }
                else {
                    screen.getStage().getRoot().removeActor(removedShip);
                }

                // instead of this we could just tell ShipShootingAIDecisionTree to remove the ship from all relevant decision trees
                dirtyAllShootingDecisionTrees();

                // raise all targeting orders if possible
                TargetValue removedShipTargetValue = removedShip.getTargetValue();
                if (!removedShipTargetValue.equals(TargetValue.others) &&
                    !removedShipTargetValue.equals(TargetValue.tertiary)) {
                    // check if this was the last enemy ship with this target value
                    boolean found = false;
                    for (ClientShip s : ships) {
                        if (s.getOwner().getId() != myId && s.getTargetValue().equals(removedShipTargetValue)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // raise all existing lower target values
                        final float existingValue = removedShipTargetValue.getValueModifier();
                        for (ClientShip s : ships) {
                            if (s.getOwner().getId() == myId) {
                                continue;
                            }
                            TargetValue t = s.getTargetValue();
                            if (t.equals(TargetValue.others)) {
                                continue;
                            }
                            if (t.getValueModifier() > existingValue) {
                                continue;
                            }

                            s.setTargetValue(t.raiseByOne());
                        }
                    }

                }
            }
            else if (msg.getType() == Message.MessageType.CREATE_SCORE_GATHERING_POINT) {
                ScoreGatheringPointMessage message = (ScoreGatheringPointMessage) msg;
                screen.addScoreGatheringPoint(message.getX(), message.getY());
                if (!firstPosSet) {
                    screen.setCameraPos(message.getX(), message.getY());
                    firstPosSet = true;
                }
            }
            else if (msg.getType() == Message.MessageType.SCORE_UPDATE) {
                ScoreUpdateMessage message = (ScoreUpdateMessage) msg;
                getPlayer(message.getPlayerId()).setScore(message.getScore());
            }
            else if (msg.getType() == Message.MessageType.GAME_STATE_CHANGE) {
                GameStateChangeMessage message = (GameStateChangeMessage) msg;
                GameState newState = message.getNewState();
                state = newState;
                screen.setCountdown(message.getLengthInSec());
            }
        }

        @Override
        public Collection<Message.MessageType> getMessageTypes() {
            return Arrays.asList(Message.MessageType.UPDATE_SHIP_PHYSICS,
                    Message.MessageType.CREATE_SHIP,
                    Message.MessageType.JOIN_BATTLE,
                    Message.MessageType.SHOOT_DAMAGE,
                    Message.MessageType.SHIP_DESTRUCTION,
                    Message.MessageType.CREATE_SCORE_GATHERING_POINT,
                    Message.MessageType.SCORE_UPDATE,
                    Message.MessageType.GAME_STATE_CHANGE);
        }
    }

    private void dirtyAllShootingDecisionTrees() {
        for (ClientShip s : ships) {
            s.setDecisionTreeDirty(true);
        }
    }

    private class ShipHover extends InputListener {
        @Override
        public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
            ClientShip ship = ClientShip.getShip(event.getTarget());

            // log("cursor entered " + ship);
            screen.setHoveringTarget(ship);
        }

        @Override
        public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
            ClientShip ship = ClientShip.getShip(event.getTarget());
            // log("cursor exited " + ship);
            screen.setHoveringTarget(null);
        }
    }
    
    private class ShipClickListener extends InputListener {

        @Override
        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            return true;
        }

        @Override
        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
            ClientShip clientShip = ClientShip.getShip(event.getTarget());

            // log("clicked " + clientShip + " @ (" + x + ", " + y + ")");

            // clicked a ship, so select it.. if mouse state is correct
            if (mouseState != MouseState.DEFAULT) {
                return;
            }

            selection.clear();

            if (ctrlDown) {
                // if the ctrl is down, select the whole group of ships
                if (clientShip.getOwner().getId() == myId) {
                    selectWholeGroup(clientShip, shipQuickGroups);
                }
                else {
                    selectWholeGroup(clientShip, enemyGroups);
                }
            }
            else {
                selection.add(clientShip);
            }

            event.handle();
        }

        private void selectWholeGroup(ClientShip ship, Array<Array<ClientShip>> groups) {
            for(Array<ClientShip> group : groups) {
                if (group.contains(ship, true)) {
                    selection.set(group);
                    break;
                }
            }
        }
    }

    private enum DragState {
        NOT_STARTED, STARTING_PANNING, PANNING,
        STARTING_MULTISELECTING, MULTISELECTING
    }

    private static final int LEFT_MOUSE = 0, RIGHT_MOUSE = 1;

    private boolean ctrlDown = false;

    private class StageListener extends InputListener {
        DragState dragState;
        private final Camera cam;

        private final Vector2 startPoint = new Vector2();
        private final Vector3 screenCoordinates = new Vector3(),
                tmp3_1 = new Vector3(),
                tmp3_2 = new Vector3();

        public StageListener() {
            startPoint.set(screen.getStage().getCamera().position.x, screen.getStage().getCamera().position.y);
            cam = screen.getStage().getCamera();
        }

        // returns the amount of world units that corresponds to one screen unit
        private float getLengthModifier(Vector3 tmp) {
            tmp.set(0, 0, 0);
            cam.unproject(tmp);
            float x1 = tmp.x;
            tmp.set(1, 0, 0);
            cam.unproject(tmp);
            float x2 = tmp.x;

            return x2 - x1;
        }

        public void touchDragged (InputEvent event, float x, float y, int pointer) {
            if (dragState == DragState.NOT_STARTED) {
                return;
            }

            if (dragState == DragState.STARTING_PANNING) {
                dragState = DragState.PANNING;
            }
            else if (dragState == DragState.STARTING_MULTISELECTING) {
                dragState = DragState.MULTISELECTING;
            }

            if (dragState == DragState.PANNING) {
                tmp3_1.set(x, y, 0);
                cam.project(tmp3_1);

                tmp3_2.set(tmp3_1);
                tmp3_2.sub(screenCoordinates); // now it contains diff between last position and this position, in pixels
                if (tmp3_2.x != 0 || tmp3_2.y != 0) {
                    tmp3_2.scl(getLengthModifier(screenCoordinates)); // now it contains the same distance in units
                    screen.translateCamera(-tmp3_2.x, -tmp3_2.y);
                    screenCoordinates.set(tmp3_1);
                }
            }
            else if (dragState == DragState.MULTISELECTING) {
                screen.setSelectionRectangleEnd(x, y);
            }
        }
        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            if (event.getTarget() instanceof ClientShip) {
                return false;
            }
            if (event.getButton() == RIGHT_MOUSE) {
                dragState = DragState.STARTING_PANNING;
                screenCoordinates.set(x, y, 0);
                cam.project(screenCoordinates);
            }
            else if (event.getButton() == LEFT_MOUSE && mouseState == MouseState.DEFAULT) {
                dragState = DragState.STARTING_MULTISELECTING;
                startPoint.set(x, y);
                screen.setSelectionRectangleStart(x, y);
                screen.setSelectionRectangleEnd(x, y);
                screen.setSelectionRectangleActive(true);
            }
            return true;
        }

        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
            if (dragState == DragState.PANNING) {
                event.cancel();
                dragState = DragState.NOT_STARTED;
            }
            else if (dragState == DragState.MULTISELECTING) {
                dragState = DragState.NOT_STARTED;
                screen.setSelectionRectangleActive(false);

                selection.clear();
                float startY, startX, width, height;
                if (startPoint.x < x) {
                    startX = startPoint.x;
                    width = x - startX;
                }
                else {
                    startX = x;
                    width = startPoint.x - startX;
                }

                if (startPoint.y < y) {
                    startY = startPoint.y;
                    height = y - startY;
                }
                else {
                    startY = y;
                    height = startPoint.y - startY;
                }
                Rectangle selectRect = new Rectangle(startX, startY, width, height);
                Rectangle shipRect = new Rectangle();
                for (ClientShip s : ships) {
                    s.getBoundingBox(shipRect);
                    // log(shipRect + " vs " + selectRect);
                    if (!selectRect.overlaps(shipRect)) {
                        continue;
                    }
                    selection.add(s);
                }
            }
            else if (mouseState == MouseState.GO_TO) {
                for (ClientShip s : selection) {
                    s.clearAllSteering();
                    s.setTargetPos(x, y);
                    event.stop();
                }
                changeMouseState(MouseState.DEFAULT);
            }
            else if (mouseState == MouseState.DIRECTION) {
                changeMouseState(MouseState.DEFAULT);
            }
            else if (mouseState.isOrbit()) {
                if (orbitUIHandler.getState() == OrbitUIHandler.State.SELECTING_TARGET) {
                    orbitUIHandler.setState(OrbitUIHandler.State.SELECTING_RADIUS);
                }
                else {
                    orbitUIHandler.setOrbit(mouseState == MouseState.ORBIT_CW);
                    changeMouseState(MouseState.DEFAULT);
                }
            }
            else if (mouseState == MouseState.DEPLOY) {
                FleetBuildWindow deployWindow = screen.getDeployWindow();
                deployWindow.deploy(conn, getMyId(), x, y);
                changeMouseState(MouseState.DEFAULT);
            }
        }

        @Override
        public boolean scrolled(InputEvent event, float x, float y, int amount) {
            screen.zoom(0.1f * amount, x, y);
            return true;
        }

        public boolean keyDown(InputEvent event, int keycode) {
            if (event.getKeyCode() == Input.Keys.CONTROL_LEFT ||
                event.getKeyCode() == Input.Keys.CONTROL_RIGHT) {
                ctrlDown = true;
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.A && ctrlDown) {
                selection.clear();
                for (ClientShip s : ships) {
                    if (s.getOwner().getId() == myId) {
                        selection.add(s);
                    }
                }

                return true;
            }
            else if (event.getKeyCode() == Input.Keys.N) {
                createNPC();
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.L) {
                screen.cycleOptimalRendering();
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.D) {
                if (selection.containsAnyOwnedBy(myId) || mouseState == MouseState.DIRECTION)
                {
                    flipMouseState(MouseState.DIRECTION);
                }
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.G) {
                if (selection.containsAnyOwnedBy(myId) || mouseState == MouseState.GO_TO)
                {
                    flipMouseState(MouseState.GO_TO);
                }
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.O) {
                if (selection.containsAnyOwnedBy(myId) || mouseState == MouseState.ORBIT_CW)
                {
                    flipMouseState(MouseState.ORBIT_CW);
                }
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.P) {
                if (selection.containsAnyOwnedBy(myId) || mouseState == MouseState.ORBIT_CCW)
                {
                    flipMouseState(MouseState.ORBIT_CCW);
                }

                return true;
            }
            else if (event.getKeyCode() == Input.Keys.Z) {
                screen.zoom(0.1f, screen.getCam().position.x, screen.getCam().position.y);
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.X) {
                screen.zoom(-0.1f, screen.getCam().position.x, screen.getCam().position.y);
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.T) {
                for (ClientShip s : selection) {
                    if (screen.isPlotting(s)) {
                        continue;
                    }
                    PathPlotterTask task = new PathPlotterTask(s);
                    screen.addPlotter(task);
                    taskHandler.addToTaskList(task);
                }
                return true;
            }
            else if (event.getKeyCode() >= Input.Keys.NUM_1 &&
                     event.getKeyCode() <= Input.Keys.NUM_9) {
                int index = event.getKeyCode() - Input.Keys.NUM_1;
                selection.set(shipQuickGroups.get(index));
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.Y) {
                ClientShip shooter = ships.get(0);
                Random rng = new Random();
                shooter.getCenterPos(tmp);
                float targetX = tmp.x + (rng.nextFloat() - 0.5f) * 40f;
                float targetY = tmp.y + (rng.nextFloat() - 0.5f) * 40f;

                screen.addLaserBeam(tmp.x, tmp.y, targetX, targetY, Assets.getLaserColor(shooter.getOwner()), rng.nextBoolean());
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.U) {
                ClientShip shooter = ships.get(0);
                Random rng = new Random();

                int tgtIndex = 1 + rng.nextInt(ships.size - 1);
                ClientShip target = ships.get(tgtIndex);
                screen.addLaserBeam(shooter, target, rng.nextBoolean());

                return true;
            }
            else if (event.getKeyCode() == Input.Keys.M && ctrlDown) {
                leaveGame();
                screen.getGame().setScreen(WarpGame.ScreenType.LOBBY);
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.M) {
                for (ClientShip s : ships) {
                    s.setTargetValue(TargetValue.others);
                }
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.COMMA) {
                setTargetValues(TargetValue.primary);
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.PERIOD) {
                setTargetValues(TargetValue.secondary);
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.MINUS) {
                setTargetValues(TargetValue.tertiary);
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.SPACE) {
                Window w = screen.getDeployWindow().getWindow();
                w.setVisible(!w.isVisible());
                return true;
            }
            else {
                log("Unknown input received: " + event.getKeyCode());
                return false;
            }
        }

        public boolean keyUp(InputEvent event, int keycode) {
            if (event.getKeyCode() == Input.Keys.CONTROL_LEFT ||
                event.getKeyCode() == Input.Keys.CONTROL_RIGHT) {
                ctrlDown = false;
                return true;
            }
            else {
                return false;
            }
        }
    }

    private void setTargetValues(TargetValue t) {
        clearTargetValues(t);
        for (ClientShip s : selection) {
            s.setTargetValue(t);
        }
    }

    private void clearTargetValues(TargetValue t) {
        for (ClientShip s : ships) {
            if (s.getTargetValue() == t) {
                s.setTargetValue(TargetValue.others);
            }
        }
    }

    private void leaveGame() {
        conn.send(new DisconnectMessage(getPlayer(myId))); // despite the misleading name, DisconnectMessage just actually removes you from the current game
        screen.getStage().getActors().removeAll(ships, true);
        ships.clear();
    }

    private final ServerConnection conn;
    private final BattleScreen screen;
    private final ShipSteering shipSteering;

    private final Array<ClientShip> ships = new Array<ClientShip>(false, 16);

    private ClientShip getShip(long id) {
        for (ClientShip ship : ships) {
            if (ship.getId() == id) {
                return ship;
            }
        }
        return null;
    }

    private ClientShip removeShip(long id) {
        ClientShip tgt = null;
        for (ClientShip ship : ships) {
            if (ship.getId() == id) {
                tgt = ship;
                break;
            }
        }
        if (tgt != null) {
            ships.removeValue(tgt, true);
            selection.remove(tgt);
            return tgt;
        }
        return null;
    }

    public void update(float delta) {
        consumer.consumeStoredMessages();
        updateNPCs();
        if (state == GameState.RUNNING) {
            shipSteering.update();
            shipShooting.update();
            taskHandler.update(delta);
        }
        updateCamPosition();
        dpsCalculator.update();
    }

    private void updateCamPosition() {
        Vector2 diff = selection.getPosDiff();
        screen.translateCamera(diff.x, diff.y);
    }

    private void updateNPCs() {
        for (NPCPlayer npc : npcPlayers) {
            npc.update();
        }
    }

    private final Vector2 tmp = new Vector2(), tmp2 = new Vector2();

    public Array<ClientShip> getShips() {
        return ships;
    }

    public Player getPlayer(long id) {
        for (Player p : players) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public Iterable<ClientShip> getSelectedShips() {
        return selection;
    }

    private class ConnectionListener implements ServerConnection.OpenCloseListener {
        @Override
        public void onOpen() {
            if (WarpGame.START_SCREEN == WarpGame.ScreenType.BATTLE) {
                conn.send(new JoinServerMessage("", -1));
                lobbyScreen.loadEarlierBuild();
                lobbyScreen.startTestFlight();
            }
        }

        @Override
        public void onClose() {
            // TODO: maybe tell the screen the connection was lost?
        }
    }

    public long getMyId() {
        return myId;
    }

    private void flipMouseState(MouseState newState) {
        if (mouseState == newState) {
            changeMouseState(MouseState.DEFAULT);
        }
        else {
            changeMouseState(newState);
        }
    }

    private void changeMouseState(MouseState newState) {
        // deactivate the old state
        if (mouseState == MouseState.DIRECTION) {
            manualSteeringTask.disable();
        }
        else if (mouseState.isOrbit()) {
            orbitUIHandler.setState(OrbitUIHandler.State.DISABLED);
        }

        changeMouseCursor(newState);

        // activate the new state
        if (newState == MouseState.DIRECTION) {
            manualSteeringTask.activate();
        }
        else if (newState.isOrbit()) {
            orbitUIHandler.setState(OrbitUIHandler.State.SELECTING_TARGET);
        }

        mouseState = newState;
    }

    private void changeMouseCursor(MouseState newState) {
        if (newState == MouseState.GO_TO) {
            ClientUtility.getCanvas().setClassName("goToCursor");
        }
        else if (newState == MouseState.DIRECTION) {
            ClientUtility.getCanvas().setClassName("directionCursor");
        }
        else if (newState == MouseState.ORBIT_CW) {
            ClientUtility.getCanvas().setClassName("orbitCwCursor");
        }
        else if (newState == MouseState.ORBIT_CCW) {
            ClientUtility.getCanvas().setClassName("orbitCcwCursor");
        }
        else if (newState == MouseState.DEFAULT) {
            ClientUtility.getCanvas().setClassName("defaultCursor");
        }
        else if (newState == MouseState.DEPLOY) {
            ClientUtility.getCanvas().setClassName("goToCursor"); // TODO create own class for deploy
        }
    }

    public Array<Player> getPlayers() {
        return players;
    }

    private void startDeployMode() {
        screen.getDeployWindow().getWindow().setVisible(false);
        changeMouseState(MouseState.DEPLOY);
    }
}
