package com.rasanenj.warp.messaging;

import com.rasanenj.warp.entities.Ship;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
// TODO: this should include correct location as well
public class CreateShipMessage extends EntityMessage {
    float width, height, mass;

    public CreateShipMessage(Ship ship) {
        super(ship.getId());
        this.width = ship.getWidth();
        this.height = ship.getHeight();
        this.mass = ship.getMass();
    }

    public CreateShipMessage(ByteBuffer msg) {
        super(msg.getLong());
        this.width = msg.getFloat();
        this.height = msg.getFloat();
        this.mass = msg.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.CREATE_SHIP;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Long.SIZE/8 + 3* Long.SIZE/8);
        b.putLong(id).putFloat(width).putFloat(height).putFloat(mass);
        return b.array();
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getMass() {
        return mass;
    }
}
