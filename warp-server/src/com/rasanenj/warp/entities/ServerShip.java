package com.rasanenj.warp.entities;

import com.rasanenj.warp.ServerPlayer;

/**
 * @author gilead
 */
public class ServerShip extends Ship {
    private ServerPlayer player;

    public ServerShip(float x, float y, ServerPlayer player) {
        super(x, y);
        this.player = player;
    }
}
