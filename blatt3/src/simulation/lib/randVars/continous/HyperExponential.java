package simulation.lib.randVars.continous;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import simulation.lib.randVars.RandVar;
import simulation.lib.rng.RNG;

/**
 * Mixture of exponential phases. The default is an H2 with two unit rates.
 */
public class HyperExponential extends RandVar {
	private static final double PROBABILITY_TOLERANCE = 1e-12;
	private List<Phase> phases;

	public HyperExponential(RNG rng) {
		this(rng, Arrays.asList(0.5, 0.5), Arrays.asList(1.0, 1.0));
	}

	public HyperExponential(RNG rng, List<Double> p, List<Double> lambda) {
		super(rng);
		if (p == null || lambda == null || p.isEmpty() || p.size() != lambda.size()) {
			throw new IllegalArgumentException("Probability and rate lists must have the same nonzero size.");
		}

		double sum = 0.0;
		List<Phase> activePhases = new ArrayList<>();
		for (int i = 0; i < p.size(); i++) {
			Double weight = p.get(i);
			Double rate = lambda.get(i);
			if (weight == null || !Double.isFinite(weight) || weight < 0.0 || weight > 1.0) {
				throw new IllegalArgumentException("Each probability must be between 0 and 1.");
			}
			if (rate == null) {
				throw new IllegalArgumentException("Rate must be finite and positive.");
			}
			Phase phase = new Phase(weight, rate);
			if (weight > 0.0) {
				activePhases.add(phase);
			}
			sum += weight;
		}
		if (Math.abs(sum - 1.0) > PROBABILITY_TOLERANCE) {
			throw new IllegalArgumentException("Probabilities must sum to 1.");
		}

		phases = new ArrayList<>(activePhases.size());
		for (Phase phase : activePhases) {
			phases.add(new Phase(phase.weight / sum, phase.rate));
		}
	}

	@Override
	public double getRV() {
		double u = rng.rnd();
		double cumulativeProbability = 0.0;
		List<Phase> currentPhases = phases;
		int last = currentPhases.size() - 1;
		for (int i = 0; i < last; i++) {
			Phase phase = currentPhases.get(i);
			cumulativeProbability += phase.weight;
			if (u < cumulativeProbability) {
				return Exponential.sample(rng, phase.rate);
			}
		}
		return Exponential.sample(rng, currentPhases.get(last).rate);
	}

	@Override
	public double getMean() {
		double mean = 0.0;
		for (Phase phase : phases) {
			mean += phase.weight / phase.rate;
		}
		return mean;
	}

	@Override
	public double getVariance() {
		double stdDeviation = getStdDeviation();
		return stdDeviation * stdDeviation;
	}

	@Override
	public double getStdDeviation() {
		double mean = getMean();
		if (Double.isInfinite(mean)) {
			return mean;
		}

		// Combine the within-phase variance and the variance of the phase means.
		double stdDeviation = 0.0;
		for (Phase phase : phases) {
			double rootWeight = Math.sqrt(phase.weight);
			double weightedStdDeviation = rootWeight / phase.rate;
			double weightedDifference = weightedStdDeviation - rootWeight * mean;
			stdDeviation = Math.hypot(stdDeviation, Math.hypot(weightedStdDeviation, weightedDifference));
		}
		return stdDeviation;
	}

	@Override
	public void setMean(double m) {
		rescaleRates(getMean(), m, "Mean");
	}

	@Override
	public void setStdDeviation(double s) {
		rescaleRates(getStdDeviation(), s, "Standard deviation");
	}

	private void rescaleRates(double current, double target, String name) {
		requirePositive(target, name);
		List<Phase> scaled = new ArrayList<>(phases.size());
		for (Phase phase : phases) {
			double rate = phase.rate * (current / target);
			if (!Double.isFinite(rate) || rate == 0.0) {
				rate = (phase.rate * current) / target;
			}
			scaled.add(new Phase(phase.weight, rate));
		}
		phases = scaled;
	}

	@Override
	public void setMeanAndStdDeviation(double m, double s) {
		requirePositive(m, "Mean");
		requirePositive(s, "Standard deviation");
		if (s < m) {
			throw new UnsupportedOperationException("Hyperexponential CV must be at least 1.");
		}
		setMeanAndCvar(m, s / m);
	}

	@Override
	public void setCvar(double c) {
		setMeanAndCvar(getMean(), c);
	}

	/**
	 * Rescales the existing mixture when the CV matches. Otherwise fits two phases
	 * that each contribute half of the requested mean.
	 */
	@Override
	public void setMeanAndCvar(double m, double c) {
		requirePositive(m, "Mean");
		requirePositive(c, "CV");
		if (c < 1.0) {
			throw new UnsupportedOperationException("Hyperexponential CV must be at least 1.");
		}
		double currentCvar = getCvar();
		if (c == currentCvar || (c > 1.0 && currentCvar > 1.0
				&& Math.abs(c - currentCvar) <= 4 * Math.ulp(c))) {
			setMean(m);
			return;
		}

		// Rationalize (1 - sqrt((c^2 - 1)/(c^2 + 1))) / 2 for large CVs.
		double inverseCvar = 1.0 / c;
		double inverseSquare = inverseCvar * inverseCvar;
		double root = Math.sqrt((1.0 - inverseSquare) / (1.0 + inverseSquare));
		double p = inverseSquare / ((1.0 + inverseSquare) * (1.0 + root));
		double q = 1.0 - p;
		phases = Arrays.asList(new Phase(p, (2.0 * p) / m), new Phase(q, (2.0 * q) / m));
	}

	private static void requirePositive(double value, String name) {
		if (!Double.isFinite(value) || value <= 0.0) {
			throw new IllegalArgumentException(name + " must be finite and positive.");
		}
	}

	@Override
	public String getType() {
		return "HyperExponential";
	}

	@Override
	public String toString() {
		return "HyperExponential{phases=" + phases + ", mean=" + getMean()
				+ ", variance=" + getVariance() + '}';
	}

	private static final class Phase {
		private final double weight;
		private final double rate;

		private Phase(double weight, double rate) {
			requirePositive(rate, "Rate");
			this.weight = weight;
			this.rate = rate;
		}

		@Override
		public String toString() {
			return "(p=" + weight + ", rate=" + rate + ")";
		}
	}
}
