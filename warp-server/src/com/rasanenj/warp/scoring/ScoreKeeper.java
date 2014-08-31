package com.rasanenj.warp.scoring;

import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.BattleLoop;
import com.rasanenj.warp.ServerPlayer;
import com.rasanenj.warp.messaging.Player;
import com.rasanenj.warp.messaging.ScoreGatheringPointMessage;
import com.rasanenj.warp.messaging.ScoreUpdateMessage;
import com.rasanenj.warp.tasks.IntervalTask;

/**
 * @author gilead
 */
public class ScoreKeeper extends IntervalTask {
    private final Array<ScoreGatheringPoint> scoreGatheringPoints = new Array<ScoreGatheringPoint>(false, 0);
    private final ScoreUpdateMessage msg = new ScoreUpdateMessage(0, 0);

    private final BattleLoop battleLoop;
    public static final float UPDATES_IN_SECOND = 1f;

    public ScoreKeeper(BattleLoop battleLoop) {
        super(UPDATES_IN_SECOND);
        this.battleLoop = battleLoop;
    }

    public void addScoreGatheringPoint(float x, float y) {
        scoreGatheringPoints.add(new ScoreGatheringPoint(x, y));
    }

    public void initPlayer(ServerPlayer serverPlayer) {
        for (ScoreGatheringPoint p : scoreGatheringPoints) {
            serverPlayer.send(new ScoreGatheringPointMessage(p.getPosition().x, p.getPosition().y));
        }
        serverPlayer.setScore(0f);
    }

    @Override
    protected void run() {
        if (battleLoop.isPhysicsPaused()) {
            return;
        }

        run(1f / UPDATES_IN_SECOND);
    }

    private void run(float seconds) {
        // update scores
        for (int i=0; i < scoreGatheringPoints.size; i++) {
            scoreGatheringPoints.get(i).updateScores(battleLoop.getPlayers(), battleLoop.getShips(), seconds);
        }

        Array<Player> players = battleLoop.getPlayers();

        for (int i=0; i < players.size; i++) {
            Player p = players.get(i);
            msg.update(p);
            battleLoop.sendToAll(msg);
        }
    }

    public void roundEnd(int secondsLeft) {

        run(secondsLeft);
    }
}
