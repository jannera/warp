package com.rasanenj.warp.messaging;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public abstract class Message {
    public enum MessageType {
        JOIN_SERVER, CHAT_MSG, DISCONNECT, START_BATTLE, CREATE_SHIP,
        UPDATE_SHIP_PHYSICS, SET_ACCELERATION, SHIP_STATS, SHOOT_REQUEST, SHOOT_DAMAGE,
        SHIP_DESTRUCTION
    }

    public abstract MessageType getType();

    private final static String CHARSET = "UTF-8";
    public static byte[] encode(String s) {
        try {
            return s.getBytes(CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decode(byte[] b) {
        try {
            return new String(b, CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    abstract public byte[] encode();

    protected ByteBuffer create(int capacity) {
        ByteBuffer b = ByteBuffer.allocate(capacity + Integer.SIZE/8);
        b.order(ByteOrder.BIG_ENDIAN);
        b.putInt(getType().ordinal());
        return b;
    }

    public static MessageType readType(ByteBuffer msg) {
        int first;
        try {
            first = msg.getInt();
        }
        catch (IndexOutOfBoundsException e) {
            log(Level.SEVERE, "IndexOutOfBoundsException when trying to read type from a message");
            return null;
        }

        for (MessageType t : MessageType.values()) {
            if (first == t.ordinal()) {
                return t;
            }
        }
        log(Level.SEVERE, "Unknown type when trying to read type from a message: " + first);
        return null;
    }

}
