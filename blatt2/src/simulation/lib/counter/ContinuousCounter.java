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
		/**
		 * TODO Problem 2.1.1 - getMean
		 * Implement this function!
		 * Hint: See course syllabus 1.5.3.2
		 */

		
		return 0;
	}
	
	/**
	 * @see Counter#getVariance()
	 */
	@Override
	public double getVariance() {
		/**
		 * TODO Problem 2.1.1 - getVariance
		 * Implement this function!
		 * Hint: See course syllabus 1.5.3.2 and 1.4.1 ff.
		 */
		return 0;
	}

	/**
	 * Counts a value.
	 * @param v value to count
	 * @param t timestamp
	 * @see	Counter#count(double, long)
	 */
	@Override
	public void count(double v, long t) {
		/**
		 * TODO Problem 2.1.1 - count
		 * Implement this function!
		 * Update the counter's internal data for the calculation of empirical moments
		 * Also update lastSampleSize and lastSampleTime
		 * Hint: See course syllabus 1.5.3.2
		 */

		// Call this method in the super class
		super.count(v, t);
	}

	/**
	 * Resets the counter.
	 */
	@Override
	public void reset(long time) {
		super.reset(time);
		firstSampleTime = time;
		lastSampleTime = time;
		lastSampleSize = 0;
	}
}
