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
     * The posA and posB parameters are modified, velocities are not.
     */
    public static float getTransverseSpeed(Vector2 posA, Vector2 posB, Vector2 velA, Vector2 velB) {
        Vector2 plane = new Vector2();
        plane.set(posA);
        plane.sub(posB);
        posA.set(velA);
        posB.set(velB);
        project(posA, plane);
        project(posB, plane);
        posA.sub(posB);
        return posA.len();
    }

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
}
