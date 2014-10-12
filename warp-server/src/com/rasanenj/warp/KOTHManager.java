package com.rasanenj.warp;

import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.entities.ServerShip;
import com.rasanenj.warp.messaging.GameStateChangeMessage;
import com.rasanenj.warp.messaging.ServerUpdateMessage;
import com.rasanenj.warp.messaging.Player;
import com.rasanenj.warp.messaging.ResourceUpdateMessage;
import com.rasanenj.warp.messaging.ShipStatsMessage;
import com.rasanenj.warp.scoring.ScoreKeeper;
import com.rasanenj.warp.tasks.IntervalTask;

/**
 * @author gilead
 */
public class KOTHManager extends IntervalTask {
    private static final float UPDATES_IN_SECOND = 1f;
    private final BattleLoop battleLoop;

    private final int matchLength;
    private final int rounds;
    private final ScoreKeeper scoreKeeper;
    private final DeployHandler deployHandler;
    private KOTHState state;

    private int currentRound = 0;
    private long nextStateChange;

    private static final int PAUSE_LENGTH_SECONDS = 10;

    private static final float RESOURCE_LIMIT = 1500;

    public KOTHManager(BattleLoop battleLoop, ScoreKeeper scoreKeeper, DeployHandler deployHandler, int rounds, int matchLength) {
        super(UPDATES_IN_SECOND);
        this.battleLoop = battleLoop;
        this.rounds = rounds;
        this.matchLength = matchLength;
        this.state = KOTHState.JUST_STARTED;
        this.scoreKeeper = scoreKeeper;
        this.deployHandler = deployHandler;
    }

    public void newShip(ShipStatsMessage message) {
        long timeout;
        if (state == KOTHState.RUNNING_ROUND) {
            timeout = (long) (1000 * Constants.DEPLOY_TIME_SECONDS);
            deployHandler.add(message, timeout);
        }
        else if (state == KOTHState.JUST_STARTED ||
                state == KOTHState.WAITING_FOR_PLAYERS) {
            deployHandler.addWithoutTimeout(message);

        }
        else {
            timeout = nextStateChange - System.currentTimeMillis();
            deployHandler.add(message, timeout);
        }

    }

    private enum KOTHState {
        JUST_STARTED,
        WAITING_FOR_PLAYERS,
        RUNNING_ROUND,
        BETWEEN_ROUNDS
    }

    @Override
    protected void run() {
        long timeNow = System.currentTimeMillis();

        int playerAmount = battleLoop.getPlayers().size;

        if (state == KOTHState.JUST_STARTED) {
            state = KOTHState.WAITING_FOR_PLAYERS;
            pauseMatch(0);
            logMessage("KOTH: Waiting for players to join");
        }
        else if (state != KOTHState.WAITING_FOR_PLAYERS
                && playerAmount == 0) {
            // match was running but we ran out of players
            resetMatch();
            state = KOTHState.JUST_STARTED;
        }
        else if (state == KOTHState.WAITING_FOR_PLAYERS) {
            if (playerAmount >= 2) {
                startMatch();
            }
        }
        else if (state == KOTHState.BETWEEN_ROUNDS && timeNow > nextStateChange) {
            state = KOTHState.RUNNING_ROUND;
            continueMatch(matchLength);
            currentRound++;
            nextStateChange = timeNow + matchLength * 1000;
            logMessage("KOTH: Started round " + currentRound + "/" + rounds);
        }
        else if (state == KOTHState.RUNNING_ROUND && timeNow > nextStateChange) {
            roundEnded(timeNow);
        }
        else if (state == KOTHState.RUNNING_ROUND) {
            // checks if there are only ships owned by single player left
            long firstOwner = -1;
            boolean onlyOneOwner = true;
            for (ServerShip s : battleLoop.getShips()) {
                long owner = s.getPlayer().getId();
                if (firstOwner == -1) {
                    firstOwner = owner;
                }
                else if (firstOwner != owner) {
                    onlyOneOwner = false;
                    break;
                }
            }
            if (onlyOneOwner) {
                if (!deployHandler.anyShipsWaitingDeployment()) {
                    int secondsLeft = (int) ((nextStateChange - timeNow) / 1000);
                    logMessage("KOTH: Ending round because only one player left with " + secondsLeft + " seconds on the clock");
                    scoreKeeper.roundEnd(secondsLeft);
                    roundEnded(timeNow);
                }
            }
        }
    }

    private void startMatch() {
        state = KOTHState.BETWEEN_ROUNDS;
        nextStateChange = System.currentTimeMillis() + PAUSE_LENGTH_SECONDS * 1000;
        deployHandler.flushAllAt(nextStateChange);
        pauseMatch(PAUSE_LENGTH_SECONDS);
        updateResourcePoints();
        logMessage("KOTH: Started deploy phase for " + PAUSE_LENGTH_SECONDS + " seconds");
    }

    private void updateResourcePoints() {
        Array<Player> players = battleLoop.getPlayers();
        for (int i = 0; i < players.size; i++) {
            Player p = players.get(i);
            updateResourcePoints(p);
        }
    }

    private void updateResourcePoints(Player p) {
        float resources = RESOURCE_LIMIT;
        Array<ServerShip> ships = battleLoop.getShipsOwnedByPlayer(p);
        for (int i=0; i < ships.size; i++) {
            ServerShip s = ships.get(i);
            System.out.println("reducing resources with " + s.getStats().getCost());
            resources -= s.getStats().getCost();
        }
        p.setResourcePointsAvailable(resources);
        battleLoop.sendToAll(new ResourceUpdateMessage(p.getId(), resources));
    }

    private void roundEnded(long timeNow) {
        logMessage("KOTH: Ended round " + currentRound);
        if (currentRound >= rounds) {
            matchEnded();
        }
        else {
            state = KOTHState.BETWEEN_ROUNDS;
            // tell all players to go to pause mode
            nextStateChange = timeNow + PAUSE_LENGTH_SECONDS * 1000;
            pauseMatch(PAUSE_LENGTH_SECONDS);
            updateResourcePoints();
            // TODO: heal all remaining ships to full health!
        logMessage("KOTH: Started deploy phase for " + PAUSE_LENGTH_SECONDS + " seconds");
        }
    }

    private void matchEnded() {
        // tell all players to end the match
        pauseMatch(0);
        battleLoop.disconnectEveryone();
        resetMatch();
    }

    private void resetMatch() {
        logMessage("KOTH: Ended the match");
        pauseMatch(0);
        state = KOTHState.JUST_STARTED;
        currentRound = 0;
    }

    private void pauseMatch(int length) {
        battleLoop.setPhysicsPaused(true);
        battleLoop.sendToAll(new GameStateChangeMessage(GameState.PAUSED, length));
    }

    private void continueMatch(int length) {
        battleLoop.setPhysicsPaused(false);
        battleLoop.sendToAll(new GameStateChangeMessage(GameState.RUNNING, length));
    }

    private void logMessage(String s) {
        battleLoop.sendToAll(new ServerUpdateMessage(s));
        System.out.println(s);
    }
}
