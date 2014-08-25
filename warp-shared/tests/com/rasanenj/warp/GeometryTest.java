package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author gilead
 */
public class GeometryTest {
    @Before
    public void setUp() throws Exception {
        posA = new Vector2();
        posB = new Vector2();
        velA = new Vector2();
        velB = new Vector2();
    }

    Vector2 posA, posB, velA, velB;

    @Test
    public void testGetTransverseSpeedZeros() throws Exception {
        // should not crash with zeros
        float t = Geometry.getTransverseSpeed(posA, posB, velA, velB);
        assertTrue(Float.isNaN(t));
    }

    private static final float delta = 0.001f;

    @Test
    public void testGetTransverseSpeed_velocity_perpendicular() throws Exception {
        posB.set(1f, 0);
        velB.set(0f, 3f);

        float t = Geometry.getTransverseSpeed(posA, posB, velA, velB);
        assertEquals(3f, t, delta);
    }

    @Test
    public void testGetTransverseSpeed_velocity_directly_away() throws Exception {
        posB.set(1f, 0);
        velB.set(1f, 0);

        float t = Geometry.getTransverseSpeed(posA, posB, velA, velB);
        assertEquals(0, t, delta);
    }

    @Test
    public void testGetTransverseSpeed_velocity_() throws Exception {
        posB.set(1f, 1f);
        velB.set(-1f, 2f);

        float t = Geometry.getTransverseSpeed(posA, posB, velA, velB);
        assertEquals(2.1213204f, t, delta);
    }
}
