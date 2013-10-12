package com.rasanenj.warp.tasks;

/**
 * @author Janne Rasanen
 */

import com.badlogic.gdx.utils.Array;

/**
 * A list of tasks. Handles and runs tasks concurrently. Runs the listed tasks
 * until they're finished. Finished tasks are told to remove self safely.
 *
 *
 */
public class TaskHandler {

    /** A list of tasks to handle */

    private Array<Task> taskList = new Array<Task>(false, 16);

    /**
     * Adds a task to the list.
     *
     * @param task
     *            Task to add to be added to the list.
     */
    public void addToTaskList(Task task) {
        taskList.add(task);
    }

    /**
     * Removes a task from the list. The task is told to remove itself safely
     * before being removed from the list itself.
     *
     * @param task
     *            Task to be removed.
     */
    public void removeFromTaskList(Task task) {
        if (task != null)
            task.removeSafely();
        taskList.removeValue(task, true);
    }

    /**
     * Updates the tasks with given simulated time since last update.
     *
     * @param delta
     *            Amount of simulated time that has passed since last update.
     */
    public void update(float delta) {
        for (Task task : taskList) {
            task.update(delta);
        }
    }

    /**
     * Clears the task list. All tasks in the list told to remove themselves
     * safely.
     *
     */
    public void clear() {
        while(taskList.size > 0)
            taskList.pop().removeSafely();
    }

}
