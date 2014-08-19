package com.rasanenj.warp.entities;

import com.badlogic.gdx.utils.Array;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class OrbitVelocities {
    public static class VelocityLimit {
        public float dst2;
        public float velocity;

        public VelocityLimit(float dst2, float velocity) {
            this.dst2 = dst2;
            this.velocity = velocity;
        }

        @Override
        public String toString() {
            return dst2 + "->" + velocity;
        }
    }

    private final Array<VelocityLimit> limits = new Array<VelocityLimit>(true, 10);

    public float getVelocity(float orbitDst2) {
        if (limits.size == 0) {
            throw new RuntimeException("No limits defined");
        }
        if (limits.size == 1) {
            return limits.get(0).velocity;
        }

        if (orbitDst2 <= limits.first().dst2) {
            return limits.first().velocity;
        }

        VelocityLimit last = limits.get(limits.size-1);
        if (orbitDst2 >= last.dst2) {
            return last.velocity;
        }

        // figured out so far:
        // - there's at least two items in array
        // - given orbit distance lies between them

        VelocityLimit lower = null, higher = null;
        for (int i =0; i < limits.size -1; i++) {
            VelocityLimit l = limits.get(i);
            if (orbitDst2 > l.dst2) {
                lower = l;
                higher = limits.get(i+1);
            }
        }
        // given orbit is between these two limits
        // scale the velocity linearly
        float range = higher.dst2 - lower.dst2;
        float over = orbitDst2 - lower.dst2;
        float percentage = over / range;

        float result = lower.velocity * percentage + higher.velocity * (1f - percentage);

        // log(orbitDst2 + " is between " + lower.dst2 + "(" + lower.velocity                + ") and " + higher.dst2 + "(" + lower.velocity + ") -> " + result);

        return result;
    }

    public Array<VelocityLimit> getLimits() {
        return limits;
    }

    @Override
    public String toString() {
        String result = "";
        for (VelocityLimit l : limits) {
            result += l + " ";
        }
        return result;
    }
}
