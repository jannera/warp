package com.rasanenj.warp.messaging;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author gilead
 */
public abstract class Message {
    public enum MessageType {
        JOIN_SERVER, CHAT_MSG
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

    public static ByteBuffer create(MessageType type, int capacity) {
        ByteBuffer b = ByteBuffer.allocate(capacity + Integer.SIZE/8);
        b.order(ByteOrder.BIG_ENDIAN);
        b.putInt(type.ordinal());
        return b;
    }

    public static MessageType readType(ByteBuffer msg) {
        int first;
        try {
            first = msg.getInt();
        }
        catch (IndexOutOfBoundsException e) {
            System.out.println("IndexOutOfBoundsException when trying to read type from a message");
            return null;
        }

        for (MessageType t : MessageType.values()) {
            if (first == t.ordinal()) {
                return t;
            }
        }
        System.out.println("Unknown type when trying to read type from a message: " + first);
        return null;
    }

}
