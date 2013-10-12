package com.rasanenj.warp.tasks;

/**
 * @author gilead
 */
public abstract class IntervalTask {
    private final long MSG_INTERVAL;
    private long lastRunTime = 0;

    public IntervalTask(float msgsInSeconds) {
        this.MSG_INTERVAL = (long) (1f / msgsInSeconds * 1000f);
    }

    public void update() {
        long timeNow = System.currentTimeMillis();
        if (timeNow - lastRunTime > MSG_INTERVAL) {
            lastRunTime = timeNow;
            run();
        }
    }

    protected abstract void run();

}
