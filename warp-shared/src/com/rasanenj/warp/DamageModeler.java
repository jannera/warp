package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;
import com.rasanenj.warp.entities.ShipStats;

import java.util.Random;

import static com.rasanenj.warp.Log.log;

/**
 * @author Janne Rasanen
 */
public class DamageModeler {
    private Random rng;
    public DamageModeler() {
        rng = new Random();
    }

    private static final Vector2 tmp = new Vector2();
    public float getDamage(float hitChance, ShipStats shooterStats) {
        float hit = rng.nextFloat();
        float damage = 0;
        if (hit < hitChance) {
            float quality = 0.5f + hit;
            damage = shooterStats.getWeaponDamage() * quality;
        }
        return damage;
    }

    public static float getChance(Vector2 shooterPos, Vector2 targetPos, Vector2 shooterLinVel,
                           Vector2 targetLinVel, ShipStats shooterStats, ShipStats targetStats) {
        tmp.set(shooterPos);
        tmp.sub(targetPos);
        float range = tmp.len();
        float transverseSpeed = Geometry.getTransverseSpeed(shooterPos,
                targetPos, shooterLinVel, targetLinVel);
        float tgtSigRes = targetStats.getSignatureResolution();
        float tracking = shooterStats.getWeaponTracking();
        float wpnSigRadius = shooterStats.getWeaponSignatureRadius();
        float optimal = shooterStats.getWeaponOptimal();
        float falloff = shooterStats.getWeaponFalloff();

        // log("transverse: " + transverseSpeed + " vs " + tracking);
        // log("sigRes: " + tgtSigRes + " vs " + wpnSigRadius);
        // log("range: " + range + " vs " + optimal + " + " + falloff);

        float a = (transverseSpeed * wpnSigRadius) / (tracking * range * tgtSigRes);
        a *= a;
        float b = Math.max(0, range - optimal) / falloff;
        b *= b;
        float blob = a + b;
        float hitChance = (float) Math.pow(0.5, blob);
        // log(a + " + " + b + " = " + blob + " -> " + hitChance + " chance");
        return hitChance;
    }

    public static float getExpectedDamage(Vector2 shooterPos, Vector2 targetPos, Vector2 shooterLinVel,
                                          Vector2 targetLinVel, ShipStats shooterStats, ShipStats targetStats) {

        float hitChance = getChance(shooterPos, targetPos, shooterLinVel,
                targetLinVel, shooterStats, targetStats);
        return getExpectedDamage(hitChance, shooterStats);
    }

    public static float getExpectedDamage(float hitChance, ShipStats shooterStats) {
        return shooterStats.getWeaponDamage() * hitChance;
    }
}
