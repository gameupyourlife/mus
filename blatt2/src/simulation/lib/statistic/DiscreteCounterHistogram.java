package simulation.lib.statistic;

import simulation.lib.counter.DiscreteCounter;
import simulation.lib.histogram.DiscreteHistogram;

/**
 * Composite statistic object that combines a DiscreteCounter and a DiscreteHistogram.
 * A single count() call updates both objects simultaneously.
 */
public class DiscreteCounterHistogram extends StatisticObject {

    private DiscreteCounter counter;
    private DiscreteHistogram histogram;

    /**
     * Constructor.
     * @see simulation.lib.histogram.DiscreteHistogram
     * @see simulation.lib.counter.DiscreteCounter
     */
    public DiscreteCounterHistogram(String variable, int numIntervals, double lowerBound, double upperBound) {
        super(variable);
        this.counter = new DiscreteCounter(variable);
        this.histogram = new DiscreteHistogram(variable, numIntervals, lowerBound, upperBound);
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
     * Wrapper for {@link #count(double, long)} with time = 0.
     * @param v value to count
     */
    public void count(double v) {
        count(v, 0);
    }

    /**
     * @return the mean
     * @see simulation.lib.counter.DiscreteCounter#getMean()
     */
    public double getMean() {
        return counter.getMean();
    }

    /**
     * @return the variance
     * @see simulation.lib.counter.DiscreteCounter#getVariance()
     */
    public double getVariance() {
        return counter.getVariance();
    }

    /**
     * @return number of samples
     * @see simulation.lib.counter.DiscreteCounter#getNumSamples()
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
     * @see simulation.lib.counter.DiscreteCounter#reset(long)
     * @see simulation.lib.histogram.DiscreteHistogram#reset(long)
     */
    public void reset(long time) {
        counter.reset(time);
        histogram.reset(time);
    }
}
