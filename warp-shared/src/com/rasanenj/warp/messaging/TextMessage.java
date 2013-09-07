package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public abstract class TextMessage extends Message {
    protected String msg;

    public TextMessage(String msg) {
        this.msg = msg;
    }

    public TextMessage(ByteBuffer b) {
        byte[] newArr = new byte[b.remaining()];
        b.get(newArr);
        msg = Message.decode(newArr);
    }

    @Override
    public byte[] encode() {
        byte[] nameInBytes = Message.encode(msg);
        ByteBuffer b = Message.create(getType(), nameInBytes.length);
        b.put(nameInBytes);
        return b.array();
    }

    public String getMsg() {
        return msg;
    }
}
