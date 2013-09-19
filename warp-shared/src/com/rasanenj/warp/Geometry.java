package com.rasanenj.warp;

/**
 * @author gilead
 */
public class Geometry {
    public static float ensurePositiveDeg(float angleDeg) {
        while (angleDeg < 0) {
            angleDeg += 360;
        }
        while (angleDeg >= 360) {
            angleDeg -= 360;
        }
        return angleDeg;
    }
}
