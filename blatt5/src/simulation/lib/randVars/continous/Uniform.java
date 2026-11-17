package simulation.lib.randVars.continous;

import simulation.lib.randVars.RandVar;
import simulation.lib.rng.RNG;

/**
 * Uniform distributed random variable.
 */
public class Uniform extends RandVar {
	private double lowerBound;
	private double upperBound;
	
	/**
	 * Constructor 1
	 * @param rng random number generator
	 */
	public Uniform(RNG rng) {
		this(rng,0,1);
	}
	
	/**
	 * Constructor 2
	 * @param rng random number generator
	 * @param lowerBound lower bound
	 * @param upperBound upper bound
	 */
	public Uniform(RNG rng, double lowerBound, double upperBound) {
		super(rng);
		setBounds(lowerBound, upperBound);
	}
	
	/**
	 * Set bounds of random variable
	 * @param lowerBound lower bound
	 * @param upperBound upper bound
	 */
	protected void setBounds(double lowerBound, double upperBound) {
		if(lowerBound>=upperBound)
			throw new IllegalArgumentException("uniform distribution: lower bound must not be greater then upper bound");
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	/**
	 * @see RandVar#getMean()
	 */
	@Override
	public double getMean() {
		return (upperBound+lowerBound)/2;
	}

	/**
	 * @see RandVar#getRV()
	 */
	@Override
	public double getRV() {
		return lowerBound + rng.rnd() * (upperBound - lowerBound);
	}

	/**
	 * @see RandVar#getType()
	 */
	@Override
	public String getType() {
		return "Uniform";
	}

	/**)
	 * @see RandVar#getVariance()
	 */
	@Override
	public double getVariance() {
		return Math.pow((upperBound-lowerBound),2)/12;
	}

	/**
	 * @see RandVar#setMean(double)
	 */
	@Override
	public void setMean(double m) {
		double tmp=m-getMean();
		lowerBound+=tmp;
		upperBound+=tmp;
	}

	/**
	 * @see RandVar#setMeanAndStdDeviation(double, double)
	 */
	@Override
	public void setMeanAndStdDeviation(double m, double s) {
		lowerBound = m-Math.sqrt(3)*s;
		upperBound = m+Math.sqrt(3)*s;
	}

	/**
	 * @see RandVar#setStdDeviation(double)
	 */
	@Override
	public void setStdDeviation(double s) {
		setMeanAndStdDeviation(getMean(),s);
	}
	
	@Override
	public String toString() {
		return super.toString() + 
			"\tparameters:\n" +
			"\t\tlowerBound: " + lowerBound + "\n" +
			"\t\tupperBound: " + upperBound + "\n";
	}
	
}
