package simulation.lib.randVars.continous;

import simulation.lib.randVars.RandVar;
import simulation.lib.rng.RNG;

/*
 * Uniform distributed random variable.
 */
public class Uniform extends RandVar {
	private double lower;
	private double upper;

	public Uniform(RNG rng) {
		this(rng, 0.0, 1.0);
	}

	public Uniform(RNG rng, double lower, double upper) {
		super(rng);
		setBounds(lower, upper);
	}

	private void setBounds(double lower, double upper) {
		if (!Double.isFinite(lower) || !Double.isFinite(upper) || lower >= upper) {
			throw new IllegalArgumentException("Uniform requires finite, increasing bounds.");
		}
		this.lower = lower;
		this.upper = upper;
	}

	@Override
	public double getRV() {
		double u = rng.rnd();
		return (1.0 - u) * lower + u * upper;
	}

	@Override
	public double getMean() {
		return lower / 2.0 + upper / 2.0;
	}

	@Override
	public double getVariance() {
		double stdDeviation = (upper / 2.0 - lower / 2.0) / Math.sqrt(3.0);
		return stdDeviation * stdDeviation;
	}

	@Override
	public void setMean(double m) {
		setMeanAndStdDeviation(m, getStdDeviation());

	}

	@Override
	public void setStdDeviation(double s) {
		setMeanAndStdDeviation(getMean(), s);

	}

	@Override
	public void setMeanAndStdDeviation(double m, double s) {
		double halfWidth = Math.sqrt(3.0) * s;
		setBounds(m - halfWidth, m + halfWidth);

	}

	@Override
	public String getType() {
		return "Uniform";
	}

	@Override
	public String toString() {
		return "Uniform{lower=" + lower + ", upper=" + upper + '}';
	}

}
