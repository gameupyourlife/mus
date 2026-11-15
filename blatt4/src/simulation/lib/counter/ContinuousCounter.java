package simulation.lib.counter;

import simulation.lib.Simulator;

/**
 * This class implements a continuous time counter / time weighted counter
 */
public class ContinuousCounter extends Counter {
	private long lastSampleTime;
	private long firstSampleTime;
	private double lastSampleSize;
	
	/**
	 * Constructor
	 * @param variable the variable to observe
	 */	
	public ContinuousCounter(String variable) {
		super(variable);
	}
	
	/**
	 * @see Counter#getMean()
	 */
	@Override
	public double getMean() {
		long interval = lastSampleTime - firstSampleTime;
		return interval > 0 ? getSumPowerOne()/(double) interval : 0;
	}
	
	/**
	 * @see Counter#getVariance()
	 */
	@Override
	public double getVariance() {
		long interval = lastSampleTime - firstSampleTime;
		return interval > 0 ? getSumPowerTwo() / (double) interval - getMean() * getMean() : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void count(double v, long t) {
		super.count(v,t);
		long interval = t - lastSampleTime;
		increaseSumPowerOne(lastSampleSize * interval);
		increaseSumPowerTwo(lastSampleSize * lastSampleSize * interval);
		lastSampleSize = v;
		lastSampleTime = t;
	}

	/**
	 * @see Counter#reset()
	 */
	@Override
	public void reset(long time) {
		super.reset(time);
		firstSampleTime = time;
		lastSampleTime = time;
		lastSampleSize = 0;
	}
}
