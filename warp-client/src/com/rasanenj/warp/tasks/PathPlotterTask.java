package com.rasanenj.warp.tasks;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.entities.ClientShip;

import java.util.Iterator;

/**
 * @author gilead
 */
public class PathPlotterTask implements Task, Iterable<Vector2> {
    private final ClientShip ship;
    private final int SIZE = 200;
    private final float INTERVAL = 0.25f; // in seconds
    private final Array<Vector2> points = new Array<Vector2>(false, SIZE);
    private float timeElapsed = 0;
    private int index = 0;

    public PathPlotterTask(ClientShip ship) {
        this.ship = ship;
        for (int i=0; i < SIZE; i++) {
            points.add(new Vector2(Float.NaN, Float.NaN));
        }
    }

    @Override
    public boolean update(float delta) {
        timeElapsed += delta;
        if (timeElapsed > INTERVAL) {
            timeElapsed -= INTERVAL;
            points.get(index).set(ship.getX(), ship.getY());
            index++;
            if (index >= SIZE) {
                index = 0;
            }
        }
        return true;
    }

    @Override
    public void removeSafely() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ClientShip getShip() {
        return ship;
    }

    private class PathIterator implements Iterator<Vector2> {

        private final Array<Vector2> points;
        private int index, scrolled;
        private final int size;

        public PathIterator(final Array<Vector2> points, final int index) {
            this.points = points;
            this.index = index;
            scrolled = 0;
            size = points.size;
        }

        @Override
        public boolean hasNext() {
            return scrolled < size;
        }

        @Override
        public Vector2 next() {
            Vector2 result = points.get(index);
            index++;
            if (index >= size) {
                index = 0;
            }
            scrolled++;
            return result;
        }

        @Override
        public void remove() {
            throw new RuntimeException("Remove not implemented");
        }
    }

    @Override
    public Iterator<Vector2> iterator() {
        return new PathIterator(points, index);
    }
}
