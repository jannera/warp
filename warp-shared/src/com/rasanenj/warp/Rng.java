package com.rasanenj.warp;

import java.util.Random;

/**
 * @author gilead
 */
public class Rng {
    private static Random rng = new Random();

    public static float getRandomFloatBetween(float a, float b) {
        return a + rng.nextFloat() * (b - a);
    }
}
