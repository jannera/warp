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


}
