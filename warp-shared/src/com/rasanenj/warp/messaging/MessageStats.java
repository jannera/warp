package com.rasanenj.warp.messaging;

/**
 * @author gilead
 */
public class MessageStats {
    public static final long UNKNOWN = -1;
    private long sent = UNKNOWN, // .. when was it sent (in senders time)
            received = UNKNOWN;  // .. when was it received (in receivers time)

    public MessageStats() {

    }

    public void copyFrom(MessageStats from) {
        setSent(from.getSent());
        setReceived(from.getReceived());
    }

    public long getSent() {
        return sent;
    }

    public void setSent(long sent) {
        this.sent = sent;
    }

    public long getReceived() {
        return received;
    }

    public void setReceived(long received) {
        this.received = received;
    }
}
