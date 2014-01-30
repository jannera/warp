package com.rasanenj.warp;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.messaging.*;
import com.rasanenj.warp.screens.BattleScreen;
import com.rasanenj.warp.screens.LobbyScreen;
import com.rasanenj.warp.systems.ShipShooting;
import com.rasanenj.warp.systems.ShipSteering;
import com.rasanenj.warp.tasks.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
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
    private final MoveCameraTask moveCameraTask;
    private static final Color[] playerColors = {new Color(0.5f, 0, 0, 1), new Color(0, 0.5f, 0, 1), new Color(0, 0, 0.5f, 1), new Color(0, 0.5f, 0.5f, 1)};
    private static final Color[] hiliteColors = {new Color(1f, 0, 0, 1), new Color(0, 1, 0, 1), new Color(0,0,1,1), new Color(0, 1, 1, 1)};
    private final Array<Player> players = new Array<Player>(true, 1);
    private final ShipHover hoverOverShipListener;
    private final ShipShooting shipShooting;
    private final ShipTextUpdater shipTextUpdater;
    private final ManualSteeringTask manualSteeringTask;
    private final LobbyScreen lobbyScreen;
    private final OrbitUIHandler orbitUIHandler;

    private long myId = -1;
    private final FleetStatsFetcher statsFetcher;
    private Array<NPCPlayer> npcPlayers = new Array<NPCPlayer>(false, 0);
    private final ShipSelection selection = new ShipSelection();
    private MouseState mouseState = MouseState.DEFAULT;

    public BattleHandler(BattleScreen screen, ServerConnection conn, LobbyScreen lobbyScreen) {
        conn.register(new ConnectionListener());
        this.statsFetcher = new FleetStatsFetcher();
        this.screen = screen;
        this.conn = conn;
        this.shipClickListener = new ShipClickListener();
        screen.getStage().addListener(new StageListener());
        shipSteering = new ShipSteering(ships, conn);
        shipShooting = new ShipShooting(ships, conn);
        this.consumer = new BattleMessageConsumer(conn.getDelegator());
        this.taskHandler = new TaskHandler();
        this.moveCameraTask = new MoveCameraTask(screen);
        this.hoverOverShipListener = new ShipHover();
        taskHandler.addToTaskList(moveCameraTask);
        this.shipTextUpdater = new ShipTextUpdater(ships, selection, true, true);
        taskHandler.addToTaskList(shipTextUpdater);
        this.manualSteeringTask = new ManualSteeringTask(selection, screen);
        taskHandler.addToTaskList(manualSteeringTask);
        this.lobbyScreen = lobbyScreen;
        this.orbitUIHandler = new OrbitUIHandler(taskHandler, screen.getCam(), ships, selection);
        screen.setOrbitUIHandler(orbitUIHandler);
    }

    private enum MouseState {
        DEFAULT, DIRECTION, GO_TO, ORBIT_CW, ORBIT_CCW;

        public boolean isOrbit() {
            return this == ORBIT_CCW || this == ORBIT_CW;
        }
    }

    public void createNPC() {
        this.npcPlayers.add(new NPCPlayer(Utility.getHost()));
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
                    long updateTime = shipPhysicsMessage.getTimestamp();
                    ship.setPosition(shipPhysicsMessage.getX(), shipPhysicsMessage.getY());
                    ship.setRotation(shipPhysicsMessage.getAngle());
                    ship.setVelocity(shipPhysicsMessage.getVelX(), shipPhysicsMessage.getVelY(), shipPhysicsMessage.getAngularVelocity(), updateTime);
                    ship.setVertices(shipPhysicsMessage.getVertices());
                    ship.setUpdateTime(updateTime);
                    if (!firstPosSet && ship.getOwner().getId() == myId) {
                        ship.getCenterPos(tmp);
                        screen.setCameraPos(tmp.x, tmp.y);
                        firstPosSet = true;
                    }
                }
            }
            else if (msg.getType() == Message.MessageType.CREATE_SHIP) {
                CreateShipMessage message = (CreateShipMessage) msg;
                Player owningPlayer = getPlayer(message.getOwnerId());

                ClientShip ship = new ClientShip(message.getId(), owningPlayer, message.getStats());
                ships.add(ship);
                screen.getStage().addActor(ship);
                ship.getClickRegionImage().addListener(shipClickListener);
                ship.getClickRegionImage().addListener(hoverOverShipListener);
                ship.setZIndex(ZOrder.ship.ordinal());


                if (owningPlayer == null) {
                    log(Level.SEVERE, "Couldn't find player with id: " + message.getOwnerId());
                }
                else {
                    Color c = selection.getBasicColor(owningPlayer);
                    ship.getImage().setColor(c);
                }
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
                    screen.addDamageProjectile(target, message.getDamage(), shooter.getX(), shooter.getY());
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

                // tell all shooting ships to stop shooting
                for (ClientShip ship : ships) {
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

    private class ShipHover extends InputListener {
        @Override
        public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
            ClientShip ship = ClientShip.getShip(event.getTarget());

            log("cursor entered " + ship);
            if (ship.getOwner().getId() == myId) {
                return;
            }

            screen.setHoveringTarget(ship);
        }

        @Override
        public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
            ClientShip ship = ClientShip.getShip(event.getTarget());
            log("cursor exited " + ship);
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

            log("clicked " + clientShip + " @ (" + x + ", " + y + ")");

            if (clientShip.getOwner().getId() == myId) {
                // clicked friendly ship, so select it.. if mouse state is correct
                if (mouseState == MouseState.DEFAULT) {
                    selection.clear();
                    selection.add(clientShip);
                }
            }
            else {
                // clicked non-friendly ship, so set it target for all friendly selected ships
                for (ClientShip s : selection) {
                    if (s.getOwner().getId() == myId) {
                        s.setFiringTarget(clientShip);
                    }
                }
            }
            event.handle();
        }
    }

    private enum DragState {
        NOT_STARTED, STARTING_PANNING, PANNING,
        STARTING_MULTISELECTING, MULTISELECTING
    }

    private static final int LEFT_MOUSE = 0, RIGHT_MOUSE = 1;

    private class StageListener extends InputListener {
        DragState dragState;

        private final Vector2 startPoint = new Vector2();

        public StageListener() {
            startPoint.set(screen.getStage().getCamera().position.x, screen.getStage().getCamera().position.y);
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
                float dx = startPoint.x - x;
                float dy = startPoint.y - y;
                if (dx != 0 && dy != 0) {
                    moveCameraTask.setTarget(dx, dy);
                }
                log(x + ", " + y + " -> " + dx + ", " + dy);
                // screen.translateCamera(dx, dy);
                startPoint.set(x, y);
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
                startPoint.set(x, y);
            }
            else if (event.getButton() == LEFT_MOUSE) {
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
                    log(shipRect + " vs " + selectRect);
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
        }

        @Override
        public boolean scrolled(InputEvent event, float x, float y, int amount) {
            screen.zoom(0.1f * amount, x, y);
            return true;
        }

        private boolean ctrlDown = false;

        public boolean keyDown(InputEvent event, int keycode) {
            if (event.getKeyCode() == Input.Keys.CONTROL_LEFT ||
                event.getKeyCode() == Input.Keys.CONTROL_RIGHT) {
                ctrlDown = true;
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.A && ctrlDown) {
                selection.selectAllMyShips();
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
                flipMouseState(MouseState.DIRECTION);
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.G) {
                flipMouseState(MouseState.GO_TO);
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.O) {
                flipMouseState(MouseState.ORBIT_CW);
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.P) {
                flipMouseState(MouseState.ORBIT_CCW);
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
            else if (event.getKeyCode() == Input.Keys.Q) {
                selection.setDesiredRelativeVelocity(0.0f);
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.W) {
                selection.setDesiredRelativeVelocity(0.33f);
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.E) {
                selection.setDesiredRelativeVelocity(0.67f);
                return true;
            }
            else if (event.getKeyCode() == Input.Keys.R) {
                selection.setDesiredRelativeVelocity(1.0f);
                return true;
            }
            else {
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

    private final ServerConnection conn;
    private final BattleScreen screen;
    private final ShipSteering shipSteering;

    private final ArrayList<ClientShip> ships = new ArrayList<ClientShip>();

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
            ships.remove(tgt);
            selection.remove(tgt);
            return tgt;
        }
        return null;
    }

    public void update(float delta) {
        consumer.consumeStoredMessages();
        shipSteering.update();
        shipShooting.update();
        taskHandler.update(delta);
        updateNPCs();
        updateCamPosition();
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

    final Vector2 tmp = new Vector2(), tmp2 = new Vector2();

    public ArrayList<ClientShip> getShips() {
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

    public class ShipSelection implements Iterable<ClientShip> {
        private Array<ClientShip> selectedShips = new Array<ClientShip>(false, 16);
        private final Vector2 lastWeightedPos = new Vector2(Float.NaN, Float.NaN),
        posDiff = new Vector2(0, 0), tmp = new Vector2(0, 0);

        public Color getHiliteColor(Player player) {
            return hiliteColors[player.getColorIndex()];
        }

        public Color getBasicColor(Player player) {
            return playerColors[player.getColorIndex()];
        }

        public void selectAllMyShips() {
            selectedShips.clear();
            for (ClientShip s : ships) {
                if (s.getOwner().getId() == myId) {
                    add(s);
                }
            }
        }

        public void clear() {
            if (selectedShips.size > 0) {
                for (ClientShip s : selectedShips) {
                    s.getImage().setColor(getBasicColor(s.getOwner()));
                }
                selectedShips.clear();
            }
        }

        public void add(ClientShip ship) {
            ship.getImage().setColor(getHiliteColor(ship.getOwner()));
            selectedShips.add(ship);
            calcWeightedPos(lastWeightedPos);
        }

        public void remove(ClientShip ship) {
            selectedShips.removeValue(ship, true);
            calcWeightedPos(lastWeightedPos);
        }

        @Override
        public Iterator<ClientShip> iterator() {
            return selectedShips.iterator();
        }

        private void calcWeightedPos(Vector2 tgt) {
            if (selectedShips.size == 0) {
                tgt.set(Float.NaN, Float.NaN);
                return;
            }

            tgt.set(0, 0);
            for (ClientShip ship : this) {
                tgt.add(ship.getX(), ship.getY());
            }
            tgt.scl(1f / selectedShips.size);
        }

        /**
         * Calculates the change in weighted position compared to last frame
         * and stores the current position.
         */
        public Vector2 getPosDiff() {
            if (selectedShips.size == 0) {
                return posDiff.set(0, 0);
            }

            calcWeightedPos(posDiff);
            tmp.set(posDiff);
            posDiff.sub(lastWeightedPos);
            lastWeightedPos.set(tmp);
            return posDiff;
        }

        public boolean contains(ClientShip ship) {
            return selectedShips.contains(ship, true);
        }

        public void setDesiredRelativeVelocity(float velocity) {
            for (ClientShip s : this) {
                s.setDesiredRelativeVelocity(velocity);
            }
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
            Utility.getCanvas().setClassName("goToCursor");
        }
        else if (newState == MouseState.DIRECTION) {
            Utility.getCanvas().setClassName("directionCursor");
        }
        else if (newState == MouseState.ORBIT_CW) {
            Utility.getCanvas().setClassName("orbitCwCursor");
        }
        else if (newState == MouseState.ORBIT_CCW) {
            Utility.getCanvas().setClassName("orbitCcwCursor");
        }
        else if (newState == MouseState.DEFAULT) {
            Utility.getCanvas().setClassName("defaultCursor");
        }
    }
}
