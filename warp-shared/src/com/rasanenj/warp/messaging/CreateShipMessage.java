package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
// TODO: this should include correct location as well
public class CreateShipMessage extends EntityMessage {
    float width, height, mass, inertia;

    public CreateShipMessage(long id, float width, float height, float mass, float inertia) {
        super(id);
        this.width = width;
        this.height = height;
        this.mass = mass;
        this.inertia = inertia;
    }

    public CreateShipMessage(ByteBuffer msg) {
        super(msg.getLong());
        this.width = msg.getFloat();
        this.height = msg.getFloat();
        this.mass = msg.getFloat();
        this.inertia = msg.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.CREATE_SHIP;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Long.SIZE/8 + 4* Long.SIZE/8);
        b.putLong(id).putFloat(width).putFloat(height).putFloat(mass).putFloat(inertia);
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

    public float getInertia() {
        return inertia;
    }
}
