package simulation.lib.counter;


/**
 * This class implements a discrete time counter
 */
public class DiscreteCounter extends Counter {

	/**
	 * Basic constructor
	 * @param variable the variable to observe
	 */
	public DiscreteCounter(String variable) {
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
		 * Hint: See course syllabus 1.4.1 ff.
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
		 * Hint: See course syllabus 1.4.1 ff.
		 */
		return 0;
	}
	
	/**
	 * Counts a new sample.
	 * @param v value to count
	 * @param t unused timestamp, since it is a discrete counter
	 */
	@Override
	public void count(double v, long t) {
		/**
		 * TODO Problem 2.1.1 - count
		 * Implement this function!
		 * Use the functions already implemented in the super classes.
		 * The timestamp t is unused, since this is a discrete counter.
		 */
	}

	/**
	 * Wrapper with t=0
	 * @see Counter#count(double,long)
	 * @param v value to be counted
	 */
	public void count(double v) {
		count(v,0);
	}
}
