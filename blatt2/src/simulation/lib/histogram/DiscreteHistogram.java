package simulation.lib.histogram;

/**
 * This class implements a discrete time histogram
 */
public class DiscreteHistogram extends Histogram {
	
	/**
	 * Constructor
	 * @param variable the observed variable
	 * @param numIntervals number of intervals
	 * @param lowerBound the lower bound of the histogram
	 * @param upperBound the upper bound of the histogram
	 */	
	public DiscreteHistogram(String variable, int numIntervals, double lowerBound, double upperBound) {
		super(variable, numIntervals, lowerBound, upperBound);
	}

	/**
	 * Counts a value in the histogram.
	 * @param v  the value to count
	 * @param t  timestamp. Unused, since this class is a discrete statistic object
	 *
	 * @see Histogram#count(double,long)
	 *
	 */
	@Override
	public void count(double v, long t) {
		/**
		 * TODO Problem 2.1.2 - count
		 * Implement this function!
		 * Also update numSamples
		 * Hint: See course syllabus 1.5.2
		 */
	}

	public void count(double v) {
		count(v,0);
	}
	
	/**
	 * @see Histogram#getNormalizingFactor()
	 */
	@Override
	protected double getNormalizingFactor() {
		return numSamples;
	}
	
	/**
	 * @see Histogram#reset(long)
	 */
	@Override
	public void reset(long time) {
		super.reset(time);
		numSamples = 0;
	}
}
