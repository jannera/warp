package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;

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

    /**
     * Checks if a line segment running from q to q+s intersects with
     * another line segment running from p to p+r.
     *
     * Messes with the contents of the q parameter.
     *
     * Copied directly from
     * http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
     * (Gareth Rees's answer, the one with vector graphics)
     *
     * @return true iff crossing point exists
     */
    public static boolean getIntersectionPoint(
            Vector2 q, Vector2 s, Vector2 p, Vector2 r, Vector2 result) {
        float crossRS = r.crs(s); // r x s
        if (crossRS == 0) {
            return false;
        }
        q.sub(p);
        float crossForT = q.crs(s); // (q - p) x s
        float crossForU = q.crs(r); // (q - p) x r

        float t = crossForT / crossRS; // (q - p) x s / (r x s)
        float u = crossForU / crossRS; // (q - p) x r / (r x s)

        if (t >= 0 && t <= 1 &&
            u >= 0 && u <= 1) {
            result.set(p.x + t * r.x, p.y + t * r.y);
            return true;
        }
        return false;
    }


    /**
     * Calculates transverse speed of ships in certain positions with certain
     * velocities.
     */
    public static float getTransverseSpeed(Vector2 posA, Vector2 posB, Vector2 velA, Vector2 velB) {
        /**
         * from http://en.wikipedia.org/wiki/Velocity#Polar_coordinates:
         *
         * The magnitude of the transverse velocity is that of the cross product
         * of the unit vector in the direction of the displacement and the
         * velocity vector.
         *
         * When v = velocity, r = displacement, then
         *
         * radial_velocity = len(v x r) / len(r)
         *
         * Where x means cross product.
         */
        v.set(velA);
        v.sub(velB);

        r.set(posA);
        r.sub(posB);

        float rLen = r.len();
        if (rLen == 0) {
            return Float.NaN;
        }

        return Math.abs(v.crs(r)) / r.len();
    }

    static final Vector2 v = new Vector2(), r = new Vector2();

    /**
     * Projects a on b, and returns a.
     */
    public static Vector2 project(Vector2 a, Vector2 b) {
        float adotb = a.dot(b);
        float bdotb = b.dot(b);
        a.set(b);
        a.scl(adotb / bdotb);
        return a;
    }

    public static float getAngularSpeed(float velocity, float radius) {
        return velocity / radius;
    }
}
