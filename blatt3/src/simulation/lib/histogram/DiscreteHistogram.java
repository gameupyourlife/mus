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
	 * @see Histogram#count(double)
	 */
	@Override
	public void count(double v, long t) {
		super.count(v,t);
		if(getNumIntervals() > 0) {
			this.incrementBin(this.getBinNumber(v), 1);
		}
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
	 * @see Histogram#reset()
	 */
	@Override
	public void reset(long time) {
		super.reset(time);
		numSamples = 0;
	}
}
