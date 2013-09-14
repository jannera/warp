package com.rasanenj.warp.messaging;

/**
 * @author gilead
 */
public abstract class EntityMessage extends Message {
    final protected long id;

    public EntityMessage(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
