package com.rasanenj.warp;

import com.rasanenj.warp.entities.ServerShip;
import com.rasanenj.warp.messaging.GameStateChangeMessage;
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

    private static final int PAUSE_LENGTH_SECONDS = 15;

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
        }
        else if (state == KOTHState.JUST_STARTED ||
                state == KOTHState.WAITING_FOR_PLAYERS) {
            timeout = Long.MAX_VALUE/2;
        }
        else {
            timeout = nextStateChange - System.currentTimeMillis();
        }
        deployHandler.add(message, timeout);
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
            pauseGame(0);
            System.out.println("KOTH: Waiting for players to join");
        }
        else if (state != KOTHState.WAITING_FOR_PLAYERS
                && playerAmount == 0) {
            // game was running but we ran out of players
            resetGame();
            state = KOTHState.JUST_STARTED;
        }
        else if (state == KOTHState.WAITING_FOR_PLAYERS) {
            if (playerAmount >= 2) {
                state = KOTHState.BETWEEN_ROUNDS;
                nextStateChange = System.currentTimeMillis() + PAUSE_LENGTH_SECONDS * 1000;
                deployHandler.flushAllAt(nextStateChange);
                pauseGame(PAUSE_LENGTH_SECONDS);
                System.out.println("KOTH: Started " + PAUSE_LENGTH_SECONDS + " sec countdown to begin the game");
            }
        }
        else if (state == KOTHState.BETWEEN_ROUNDS && timeNow > nextStateChange) {
            state = KOTHState.RUNNING_ROUND;
            continueGame(matchLength);
            currentRound++;
            nextStateChange = timeNow + matchLength * 1000;
            System.out.println("KOTH: Started round " + currentRound + "/" + rounds);
        }
        else if (state == KOTHState.RUNNING_ROUND && timeNow > nextStateChange) {
            roundEnded(timeNow);
        }
        else if (state == KOTHState.RUNNING_ROUND) {
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
                    System.out.println("KOTH: Ending round because only one player left with " + secondsLeft + " seconds on the clock");
                    scoreKeeper.roundEnd(secondsLeft);
                    roundEnded(timeNow);
                }
            }
        }
    }

    private void roundEnded(long timeNow) {
        System.out.println("KOTH: Ended round " + currentRound);
        if (currentRound >= rounds) {
            // tell all players to end the match
            battleLoop.disconnectEveryone();
            resetGame();
        }
        else {
            state = KOTHState.BETWEEN_ROUNDS;
            // tell all players to go to pause mode
            nextStateChange = timeNow + PAUSE_LENGTH_SECONDS * 1000;
            pauseGame(PAUSE_LENGTH_SECONDS);
            System.out.println("KOTH: Paused for " + PAUSE_LENGTH_SECONDS + " secs between rounds");
        }
    }

    private void resetGame() {
        System.out.println("KOTH: Ended the game");
        pauseGame(0);
        state = KOTHState.JUST_STARTED;
        currentRound = 0;
    }

    private void pauseGame(int length) {
        battleLoop.setPhysicsPaused(true);
        battleLoop.sendToAll(new GameStateChangeMessage(GameState.PAUSED, length));
    }

    private void continueGame(int length) {
        battleLoop.setPhysicsPaused(false);
        battleLoop.sendToAll(new GameStateChangeMessage(GameState.RUNNING, length));
    }
}
