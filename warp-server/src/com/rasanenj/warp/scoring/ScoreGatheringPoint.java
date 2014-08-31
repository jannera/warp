package com.rasanenj.warp.scoring;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.entities.ServerShip;
import com.rasanenj.warp.messaging.Player;

import java.util.HashMap;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ScoreGatheringPoint {
    private final Vector2 position = new Vector2();

    public ScoreGatheringPoint(float x, float y) {
        position.set(x, y);
    }

    public Vector2 getPosition() {
        return position;
    }

    public final HashMap<Long, Float> scores = new HashMap<Long, Float>();

    public final float SCORE_PER_TICK = 1f;

    public void updateScores(Array<Player> players, Array<ServerShip> ships, float scoreMultiplier) {
        for(Player p : players) {
            scores.put(p.getId(), 0f);
        }
        // give out points according to what ships are nearby the gathering point

        // first store weights of every ship
        float totalWeight = 0f;
        for (ServerShip ship : ships) {
            float dst = ship.getBody().getWorldCenter().dst(position);
            dst = Math.max(3, dst);
            float weight = 10f / (float) Math.pow(dst, 0.6f);
            // todo multiply with the cost of the ship
            totalWeight += weight;
            long ownerId = ship.getPlayer().getId();
            Float currentWeight = scores.get(ownerId);
            scores.put(ownerId, currentWeight + weight);
        }

        if (totalWeight == 0f) {
            return;
        }

        for (Player p : players) {
            float score = scores.get(p.getId()) / totalWeight * SCORE_PER_TICK * scoreMultiplier;
            p.setScore(p.getScore() + score);
        }
    }
}
