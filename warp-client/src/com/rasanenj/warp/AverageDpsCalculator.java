package com.rasanenj.warp;

import com.rasanenj.warp.chart.Chart;
import com.rasanenj.warp.tasks.IntervalTask;

/**
 * @author gilead
 */
public class AverageDpsCalculator extends IntervalTask {
    private final Statistics statistics;
    private final long averageSpanMs;
    private long playerId;

    public AverageDpsCalculator(Statistics statistics, long averageSpanMs) {
        super(1);
        this.statistics = statistics;
        this.averageSpanMs = averageSpanMs;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    @Override
    protected void run() {
        float dps = statistics.getAverageDps(playerId, averageSpanMs);
        // TODO: instead of the following, create a new point in some kind of Storage class
        Chart.addPointToGraph(dps);
    }
}
