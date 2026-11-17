package simulation.lib.histogram;

import simulation.lib.Simulator;

/**
 * This class implements a continuous time histogram
 */
public class ContinuousHistogram extends Histogram {
	private double lastSampleSize;
	private long firstSampleTime;
	private long lastSampleTime;
	private Simulator sim;

	/**
	 * Constructor
	 * @param variable the observed variable
	 * @param numIntervals number of intervals
	 * @param lowerBound the lower bound of the histogram
	 * @param upperBound the upper bound of the histogram
	 */	
	public ContinuousHistogram(String variable, int numIntervals, int lowerBound, int upperBound) {
		super(variable, numIntervals, lowerBound, upperBound);
		this.firstSampleTime = 0;
		this.lastSampleTime = 0;
		this.lastSampleSize = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void count(double v, long t) {
		super.count(v,t);
		if(getNumIntervals() > 0) {
			this.incrementBin(this.getBinNumber(this.lastSampleSize), t - this.lastSampleTime);
			this.lastSampleTime = t;
		}
		this.lastSampleSize = v;
	}
	
	/**
	 * @see Histogram#getNormalizingFactor()
	 */
	@Override
	protected double getNormalizingFactor() {
		return this.lastSampleTime - this.firstSampleTime;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset(long time) {
		super.reset(time);
		this.firstSampleTime = time;
		this.lastSampleTime = time;
		this.lastSampleSize = 0;
	}
}
