package com.rasanenj.warp.tasks;

/**
 * @author gilead
 */
public abstract class RunnableFPS implements Runnable {
    private long lastTime;

    protected long getFrameLength() {
        return (long) (1 / getFPS() * 1000f);
    }

    public RunnableFPS() {
        lastTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        final long FRAME_LENGTH = getFrameLength();
        while (true) {
            if (System.currentTimeMillis() > lastTime + FRAME_LENGTH) {
                long currTime = System.currentTimeMillis();
                float delta = (currTime - lastTime) / 1000f; // in seconds
                update(delta);
                lastTime = currTime;
            }
            else {

                try {
                    Thread.sleep(FRAME_LENGTH / 3);
                } catch (InterruptedException e) {
                    // do nothing
                }

            }
        }

    }

    protected abstract float getFPS();
    protected abstract void update(float delta);
}
