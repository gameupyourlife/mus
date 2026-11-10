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
		return getNumSamples() > 0 ? getSumPowerOne() / getNumSamples() : 0;
	}
	
	/**
	 * @see Counter#getVariance()
	 */
	@Override
	public double getVariance() {
		if(getNumSamples() > 0)
			return getNumSamples() / (double)(getNumSamples() - 1) * (getSumPowerTwo() / getNumSamples() - getMean() * getMean()); //Equation 2.30
		else
			return 0;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void count(double v, long t) {
		super.count(v,t);
		increaseSumPowerOne(v);
		increaseSumPowerTwo(v * v);
	}

	/**
	 * Wrapper for @see Counter#count
	 * @param v value to be couted
	 */
	public void count(double v) {
		count(v,0);
	}
}
