package com.rasanenj.warp;

import com.rasanenj.warp.entities.ServerShip;

import java.util.Random;

/**
 * @author Janne Rasanen
 */
public class DamageModeler {
    private Random random;
    public DamageModeler() {
        random = new Random();
    }

    public float getDamage(ServerShip shooter, ServerShip target) {
        return random.nextFloat() * 10f;
    }
}
