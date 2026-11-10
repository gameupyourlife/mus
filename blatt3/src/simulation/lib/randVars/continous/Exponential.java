package simulation.lib.randVars.continous;

import simulation.lib.randVars.RandVar;
import simulation.lib.rng.RNG;

/*
 * TODO Problem 3.1.2 - implement this class (section 3.2.2 in course syllabus)
 * !!! If an abstract class method does not make sense to be implemented in a particular RandVar class,
 * an UnsupportedOperationException should be thrown !!!
 *
 * Expnential distributed random variable.
 */
public class Exponential extends RandVar {
	private double lambda;

	public Exponential(RNG rng) {
		this.lambda = 1;
		super(rng);
	}

	public Exponential(RNG rng, double lambda) {
		this.lambda = lambda;
		super(rng);
	}

	@Override
	public double getRV() {
		// Erzeugung durch Inversion: U ~ U(0,1), X= -ln(U)/λ
		return (-Math.log(rng.rnd()) / lambda);
	}

	@Override
	public double getMean() {
		return 1 / lambda;
	}

	@Override
	public double getVariance() {
		return 1 / Math.pow(lambda, 2);
	}

	@Override
	public void setMean(double m) {
		this.lambda = 1 / m;
	}

	@Override
	public void setStdDeviation(double s) {
		this.lambda = 1 / s;
	}

	@Override
	public void setMeanAndStdDeviation(double m, double s) {
		if (m != s) {
			throw new UnsupportedOperationException("Mean and standard deviation must be equal for exponential distribution.");
		}

		this.lambda = 1 / m;
	}

	@Override
	public String getType() {
		return "Exponential";
	}

	@Override
	public String toString() {
		return "Exponential{lambda=" + lambda + '}';
	}

}
