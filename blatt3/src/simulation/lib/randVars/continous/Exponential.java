package simulation.lib.randVars.continous;

import simulation.lib.randVars.RandVar;
import simulation.lib.rng.RNG;

/*
 * Exponentially distributed random variable.
 */
public class Exponential extends RandVar {
	private double lambda;

	public Exponential(RNG rng) {
		this(rng, 1.0);
	}

	public Exponential(RNG rng, double lambda) {
		super(rng);
		setRate(lambda);
	}

	private void setRate(double lambda) {
		if (!Double.isFinite(lambda) || lambda <= 0.0) {
			throw new IllegalArgumentException("Rate must be finite and positive.");
		}
		this.lambda = lambda;
	}

	@Override
	public double getRV() {
		return sample(rng, lambda);
	}

	static double sample(RNG rng, double rate) {
		return -Math.log1p(-rng.rnd()) / rate;
	}

	@Override
	public double getMean() {
		return 1 / lambda;
	}

	@Override
	public double getVariance() {
		double mean = getMean();
		return mean * mean;
	}

	@Override
	public double getStdDeviation() {
		return getMean();
	}

	@Override
	public double getCvar() {
		return 1.0;
	}

	@Override
	public void setMean(double m) {
		setRate(1.0 / m);
	}

	@Override
	public void setStdDeviation(double s) {
		setMean(s);
	}

	@Override
	public void setMeanAndStdDeviation(double m, double s) {
		if (!Double.isFinite(m) || m <= 0.0 || !Double.isFinite(s) || s <= 0.0) {
			throw new IllegalArgumentException("Mean and standard deviation must be finite and positive.");
		}
		if (m != s) {
			throw new UnsupportedOperationException("Mean and standard deviation must be equal for exponential distribution.");
		}
		setMean(m);
	}

	@Override
	public void setCvar(double c) {
		if (c != 1.0) {
			throw new UnsupportedOperationException("Exponential CV is always 1.");
		}
	}

	@Override
	public void setMeanAndCvar(double m, double c) {
		setCvar(c);
		setMean(m);
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
