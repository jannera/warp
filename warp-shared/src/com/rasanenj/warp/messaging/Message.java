package com.rasanenj.warp.messaging;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

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
        SHIP_DESTRUCTION, JOIN_BATTLE, JOIN_CHAT, CREATE_SCORE_GATHERING_POINT,
        SCORE_UPDATE, GAME_STATE_CHANGE
    }

    // concerning this message
    private MessageStats thisStats = new MessageStats();

    // concerning the last message the sender received
    private MessageStats lastMessageSenderReceivedStats = new MessageStats();

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
            e.printStackTrace();
        }
        return null;
    }

    abstract public byte[] encode();

    protected ByteBuffer create(int capacity) {
        ByteBuffer b = ByteBuffer.allocate(capacity + Integer.SIZE/8 + Long.SIZE/8 * 3);
        b.order(ByteOrder.BIG_ENDIAN);
        b.putInt(getType().ordinal());
        b.putLong(thisStats.getSent());
        b.putLong(lastMessageSenderReceivedStats.getReceived());
        b.putLong(lastMessageSenderReceivedStats.getSent());
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

    public static long readTimestamp(ByteBuffer msg) {
        try {
            return msg.getLong();
        }
        catch (IndexOutOfBoundsException e) {
            log(Level.SEVERE, "IndexOutOfBoundsException when trying to read type from a message");
            return -1;
        }
    }

    public static void putFloats(ByteBuffer b, float ... floats) {
        for(int i = 0; i < floats.length; i++) {
            b.putFloat(floats[i]);
        }
    }

    public static int getBytesForVectors(Vector2[] vectors) {
        // one for integer that tells the length, one for each x-y-pair
        return Integer.SIZE/8 + Float.SIZE/8 * vectors.length * 2;
    }

    public static void putVectors(ByteBuffer b, Vector2[] vectors) {
        b.putInt(vectors.length);
        for(int i=0; i < vectors.length; i++) {
            b.putFloat(vectors[i].x);
            b.putFloat(vectors[i].y);
        }
    }

    public static Vector2[] getVectors(ByteBuffer b) {
        int length = b.getInt();
        Vector2[] result = new Vector2[length];
        for (int i=0; i < length; i++) {
            result[i] = new Vector2(b.getFloat(), b.getFloat());
        }
        return result;
    }

    public MessageStats getThisStats() {
        return thisStats;
    }

    public MessageStats getLastMessageSenderReceivedStats() {
        return lastMessageSenderReceivedStats;
    }

    // in milliseconds, rounded up
    public long estimateLatency() {
        return MathUtils.ceil(
                (lastMessageSenderReceivedStats.getReceived()
                        - lastMessageSenderReceivedStats.getSent()
                        + thisStats.getReceived()
                        - thisStats.getSent()) / 2f);
    }

    public static boolean getBoolean(ByteBuffer b) {
        return b.getShort() != 0;
    }

    public static void putBoolean(ByteBuffer b, boolean value) {
        short s = 0;
        if (value) {
            s = 1;
        }
        b.putShort (s);
    }
}
