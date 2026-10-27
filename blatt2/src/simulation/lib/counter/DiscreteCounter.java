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
		return sumPowerOne / numSamples;
	}
	
	/**
	 * @see Counter#getVariance()
	 */
	@Override
	public double getVariance() {
		var n = numSamples;
		var factor = (n / (n - 1.0));

		var firstEmpiricMomentum = getMean();
		var secondEmpiricMomentum = this.getSumPowerTwo() / n;

		return factor * (secondEmpiricMomentum - Math.pow(firstEmpiricMomentum, 2));
	}
	
	/**
	 * Counts a new sample.
	 * @param v value to count
	 * @param t unused timestamp, since it is a discrete counter
	 */
	@Override
	public void count(double v, long t) {
		increaseSumPowerOne(v);
		increaseSumPowerTwo(Math.pow(v, 2));

		super.count(v, t);
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
