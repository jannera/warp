package com.rasanenj.warp.entities;

import com.badlogic.gdx.graphics.Texture;
import com.rasanenj.warp.Assets;
import com.rasanenj.warp.messaging.Player;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author gilead
 */
public class ClientShipTest {
    float epsilon = 0.0001f;

    @Test
    public void testSetAngularVelocity() throws Exception {
        Assets.shipTexture = mock(Texture.class);
        Assets.moveTargetTexture = mock(Texture.class);
        Player player = new Player("name", 0);
        ShipStats stats = new ShipStats(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        ClientShip ship = new ClientShip(1, player, 1, 1, stats);
        ship.setVelocity(0, 0, 1, 1000);
        ship.setVelocity(0, 0, 2, 2000);
        assertEquals("Acceleration should've been set", 1f, ship.getAngularAcceleration(), epsilon);

        ship.setVelocity(0, 0, 123, 2000);
        ship.setVelocity(0, 0, 452, 2000);
        ship.setVelocity(0, 0, 212, 2000);
        assertEquals("Same message multiple times with same timestamp shouldn't change anything", 1f, ship.getAngularAcceleration(), epsilon);


    }
}
