package com.rasanenj.warp;

import com.rasanenj.warp.entities.ServerShip;
import com.rasanenj.warp.messaging.GameStateChangeMessage;
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
    private KOTHState state;

    private int currentRound = 0;
    private long nextStateChange;

    private static final int PAUSE_LENGTH_SECONDS = 5;

    public KOTHManager(BattleLoop battleLoop, ScoreKeeper scoreKeeper, int rounds, int matchLength) {
        super(UPDATES_IN_SECOND);
        this.battleLoop = battleLoop;
        this.rounds = rounds;
        this.matchLength = matchLength;
        this.state = KOTHState.JUST_STARTED;
        this.scoreKeeper = scoreKeeper;
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

        if (state == KOTHState.JUST_STARTED) {
            state = KOTHState.WAITING_FOR_PLAYERS;
            pauseGame(0);
            System.out.println("KOTH: Waiting for players to join");
        }
        else if (state == KOTHState.WAITING_FOR_PLAYERS) {
            if (battleLoop.getPlayers().size >= 2) {
                state = KOTHState.BETWEEN_ROUNDS;
                nextStateChange = System.currentTimeMillis() + PAUSE_LENGTH_SECONDS * 1000;
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
                int secondsLeft = (int) ((nextStateChange - timeNow) / 1000);
                System.out.println("KOTH: Ending round because only one player left with " + secondsLeft + " seconds on the clock");
                scoreKeeper.roundEnd(secondsLeft);
                roundEnded(timeNow);
            }
        }
    }

    private void roundEnded(long timeNow) {
        System.out.println("KOTH: Ended round " + currentRound);
        if (currentRound >= rounds) {
            // tell all players to end the match
            pauseGame(0);
            System.out.println("KOTH: Ended the game");
            battleLoop.disconnectEveryone();
            state = KOTHState.JUST_STARTED;
            currentRound = 0;
        }
        else {
            state = KOTHState.BETWEEN_ROUNDS;
            // tell all players to go to pause mode
            nextStateChange = timeNow + PAUSE_LENGTH_SECONDS * 1000;
            pauseGame(PAUSE_LENGTH_SECONDS);
            System.out.println("KOTH: Paused for " + PAUSE_LENGTH_SECONDS + " secs between rounds");
        }
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
