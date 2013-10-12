package com.rasanenj.warp.tasks;

/**
 * @author Janne Rasanen
 */
public interface Task {

    /**
     * Updates task by given time delta. If the task is finished and thus
     * ready for deletion, returns false.
     *
     * @param delta
     *             Given time delta since last update.
     * @return
     *             If the task is finished, returns False. Otherwise True.
     */
    public boolean update(float delta);

    /**
     * Removes dynamic content where applicable.
     *
     */
    public void removeSafely();
}
