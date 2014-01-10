package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author gilead
 */
public class JoinServerMessageTest {
    @org.junit.Test
    public void testEncode() throws Exception {
        JoinServerMessage msg = new JoinServerMessage("name", 1);
        byte[] arr = msg.encode();
        ByteBuffer b = ByteBuffer.wrap(arr);
        Message.MessageType type = Message.readType(b);
        assertEquals(Message.MessageType.JOIN_SERVER, type);
    }

    @org.junit.Test
    public void bytebufferTest() throws Exception {
        byte[] arr = new String("gilead").getBytes();
        ByteBuffer b = ByteBuffer.allocate(Integer.SIZE/8 + arr.length);
        b.putInt(1);
        b.put(arr);

        b = ByteBuffer.wrap(b.array());

        int x = b.getInt();
        assertTrue(x == 1);

        byte[] newArr = new byte[b.remaining()];
        b.get(newArr);
        String name = new String(newArr);
        assertEquals("gilead", name);


    }

    @org.junit.Test
    public void testDecode() throws Exception {
        JoinServerMessage msg = new JoinServerMessage("name", 1);
        byte[] arr = msg.encode();
        ByteBuffer b = ByteBuffer.wrap(arr);
        Message.MessageType type = Message.readType(b);
        assertEquals(Message.MessageType.JOIN_SERVER, type);
        msg = new JoinServerMessage(b);
        assertEquals("name", msg.playerName);
    }
}
