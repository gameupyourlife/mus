package simulation.lib.statistic;

import simulation.lib.counter.ContinuousCounter;
import simulation.lib.histogram.ContinuousHistogram;

/**
 * Composite statistic object that combines a ContinuousCounter and a ContinuousHistogram.
 * A single count() call updates both objects simultaneously.
 */
public class ContinuousCounterHistogram extends StatisticObject {

    private ContinuousCounter counter;
    private ContinuousHistogram histogram;

    /**
     * Constructor.
     * @see simulation.lib.histogram.ContinuousHistogram
     * @see simulation.lib.counter.ContinuousCounter
     */
    public ContinuousCounterHistogram(String variable, int numIntervals, int lowerBound, int upperBound) {
        super(variable);
        this.counter = new ContinuousCounter(variable);
        this.histogram = new ContinuousHistogram(variable, numIntervals, lowerBound, upperBound);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void count(double v, long t) {
        counter.count(v, t);
        histogram.count(v, t);
    }

    /**
     * @return the mean
     * @see simulation.lib.counter.ContinuousCounter#getMean()
     */
    public double getMean() {
        return counter.getMean();
    }

    /**
     * @return the variance
     * @see simulation.lib.counter.ContinuousCounter#getVariance()
     */
    public double getVariance() {
        return counter.getVariance();
    }

    /**
     * @return number of samples
     * @see simulation.lib.counter.ContinuousCounter#getNumSamples()
     */
    public long getNumSamples() {
        return counter.getNumSamples();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String report() {
        return counter.report();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void csvReport(String outputdir) {
        counter.csvReport(outputdir);
        histogram.csvReport(outputdir);
    }

    /**
     * @param time current simulation time
     * @see simulation.lib.counter.ContinuousCounter#reset(long)
     * @see simulation.lib.histogram.ContinuousHistogram#reset(long)
     */
    public void reset(long time) {
        counter.reset(time);
        histogram.reset(time);
    }
}
