package com.rasanenj.warp.messaging;

import org.junit.Test;

import java.nio.ByteBuffer;

import static junit.framework.Assert.assertEquals;

/**
 * @author gilead
 */
public class MessageTest {
    @Test
    public void testEstimateLatency() throws Exception {
        // client is receiving message from server
        long diffFromClientToServer = 123; // client_time = servertime + diffFromClientToServer

        long lastMessageLatency = 10;
        long thisMessageLatency = 40;

        long lastMessageSent = 300; // in client time
        long lastMessageReceived = lastMessageSent + lastMessageLatency; // this is still in client time, will be transformed to server time soon

        long thisMessageSent = lastMessageReceived + 2351; // this is still in client time, will be transformed to server time soon
        long thisMessageReceived = thisMessageSent + thisMessageLatency;

        // these two need to be in server time
        lastMessageReceived -= diffFromClientToServer;
        thisMessageSent -= diffFromClientToServer;

        JoinChatMessage message = new JoinChatMessage("name");
        message.getLastMessageSenderReceivedStats().setSent(lastMessageSent);
        message.getLastMessageSenderReceivedStats().setReceived(lastMessageReceived);
        message.getThisStats().setSent(thisMessageSent);

        byte[] arr = message.encode();
        ByteBuffer b = ByteBuffer.wrap(arr);
        JoinChatMessage decoded = (JoinChatMessage) MessageFactory.decode(b);
        assertEquals("name", decoded.getMsg());
        decoded.getThisStats().setReceived(thisMessageReceived);

        assertEquals((lastMessageLatency + thisMessageLatency)/2, decoded.estimateLatency());
    }
}
