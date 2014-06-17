package com.rasanenj.warp;

import com.badlogic.gdx.utils.Array;

/**
 * @author gilead
 */
public class Statistics {
    public void storeDamage(long shootingShipId, long targetShipId, long shooterOwnerId,
                            long targetOwnerId, long timeMs, float damage) {
        damageStats.add(new DamageStat(shootingShipId, targetShipId, shooterOwnerId, targetOwnerId,
                timeMs, damage));
    }

    public float getAverageDps(long playerId, long timeMs) {
        long timeLimit = System.currentTimeMillis() - timeMs;

        float damage = 0;
        for (DamageStat stat : damageStats) {
            if (stat.timeMs >= timeLimit && stat.shooterOwnerId == playerId) {
                damage += stat.damage;
            }
        }
        return damage / (timeMs / 1000f);
    }

    private Array<DamageStat> damageStats = new Array<DamageStat>();

    private class DamageStat
    {
        long shootingShipId, targetShipId, shooterOwnerId, targetOwnerId, timeMs;
        float damage;

        private DamageStat(long shootingShipId, long targetShipId, long shooterOwnerId, long targetOwnerId, long timeMs, float damage) {
            this.shootingShipId = shootingShipId;
            this.targetShipId = targetShipId;
            this.shooterOwnerId = shooterOwnerId;
            this.targetOwnerId = targetOwnerId;
            this.timeMs = timeMs;
            this.damage = damage;
        }
    }
}
