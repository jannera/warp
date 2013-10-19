package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;
import com.rasanenj.warp.entities.ServerShip;

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

    public float getDamage(ServerShip shooter, ServerShip target) {
        Vector2 pos = shooter.getBody().getWorldCenter();
        pos.sub(target.getBody().getWorldCenter());
        float range = pos.len();
        float transverseSpeed = Geometry.getTransverseSpeed(shooter.getBody().getWorldCenter(),
                target.getBody().getWorldCenter(), shooter.getBody().getLinearVelocity(),
                target.getBody().getLinearVelocity());
        float tgtSigRes = target.getSignatureResolution();
        float tracking = shooter.getWeaponTracking();
        float wpnSigRadius = shooter.getWeaponSignatureRadius();
        float optimal = shooter.getWeaponOptimal();
        float falloff = shooter.getWeaponFalloff();

        log("transverse: " + transverseSpeed + " vs " + tracking);
        log("sigRes: " + tgtSigRes + " vs " + wpnSigRadius);
        log("range: " + range + " vs " + optimal + " + " + falloff);

        float a = (transverseSpeed * tgtSigRes) / (tracking * range * wpnSigRadius);
        a *= a;
        float b = Math.max(0, range - optimal) / falloff;
        b *= b;
        float blob = a + b;
        float hitChance = (float) Math.pow(0.5, blob);
        log(a + " + " + b + " = " + blob + " -> " + hitChance + " chance");

        float hit = rng.nextFloat();
        float damage = 0;
        if (hit < hitChance) {
            float quality = 0.5f + hit;
            damage = 1f * quality;
            // TODO: fetch the base damage from the shooting ship
        }

        return damage;
    }
}
