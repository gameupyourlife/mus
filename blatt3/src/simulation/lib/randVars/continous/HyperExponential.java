package simulation.lib.randVars.continous;

import simulation.lib.randVars.RandVar;
import simulation.lib.rng.RNG;
import static java.lang.Math.*;

import java.util.ArrayList;
import java.util.List;

/*
 * TODO Problem 3.1.2 - implement this class (section 3.2.4 in course syllabus)
 * !!! If an abstract class method does not make sense to be implemented in a particular RandVar class,
 * an UnsupportedOperationException should be thrown !!!
 *
 * Hyperexponential distributed random variable.
 */
public class HyperExponential extends RandVar {
	private List<Double> p;
	private List<Double> lambda;

	public HyperExponential(RNG rng) {
		p = new ArrayList<Double>();
		lambda = new ArrayList<Double>();
		super(rng);
	}

	public HyperExponential(RNG rng, List<Double> p, List<Double> lambda) {
		this.p = p;
		this.lambda = lambda;
		super(rng);
	}

	@Override
	public double getRV() {
		double u = rng.rnd();
		int index = 0;
		double cumulativeProbability = 0.0;

		for (int i = 0; i < p.size(); i++) {
			cumulativeProbability += p.get(i);
			if (u <= cumulativeProbability) {
				index = i;
				break;
			}
		}

		// Generate an exponential random variable with the selected lambda
		double lambdaValue = lambda.get(index);
		Exponential expRV = new Exponential(rng, lambdaValue);
		return expRV.getRV();
	}

	@Override
	public double getMean() {
		double mean = 0;
		for (int i = 0; i < p.size(); i++) {
			mean += p.get(i) / lambda.get(i);
		}

		return mean;
	}

	@Override
	public double getVariance() {
		double sum1 = 0;
		double sum2 = 0;

		for (int i = 0; i < p.size(); i++) {
			sum1 += p.get(i) / Math.pow(lambda.get(i), 2);
			sum2 += p.get(i) / lambda.get(i);
		}

		return 2 * sum1 - Math.pow(sum2, 2);
	}

	@Override
	public void setMean(double m) {
		throw new UnsupportedOperationException("Setting mean is not supported for HyperExponential distribution.");
	}

	@Override
	public void setStdDeviation(double s) {
		throw new UnsupportedOperationException("Setting standard deviation is not supported for HyperExponential distribution.");
	}

	@Override
	public void setMeanAndStdDeviation(double m, double s) {
		throw new UnsupportedOperationException("Setting mean and standard deviation is not supported for HyperExponential distribution.");
	}

	@Override
	public String getType() {
		return "HyperExponential";
	}

	@Override
	public String toString() {
		return "HyperExponential Distribution\n" +
				"Probabilities: " + p.toString() + "\n" +
				"Lambdas: " + lambda.toString() + "\n" +
				"Mean: " + getMean() + "\n" +
				"Variance: " + getVariance();
	}
}
