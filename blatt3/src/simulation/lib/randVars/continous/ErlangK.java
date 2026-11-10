package simulation.lib.randVars.continous;

import simulation.lib.randVars.RandVar;
import simulation.lib.rng.RNG;

/*
 * Erlang-k distributed random variable.
 */
public class ErlangK extends RandVar {
    private int k = 1;
    private double mean = 1.0;

    public ErlangK(RNG rng) {
        super(rng);
    }

    @Override
    public double getRV() {
        double sum = 0.0;
        for (int i = 0; i < k; i++) {
            sum -= Math.log1p(-rng.rnd());
        }
        return (sum / k) * mean;
    }

    @Override
    public double getMean() {
        return mean;
    }

    @Override
    public double getVariance() {
        double stdDeviation = mean / Math.sqrt(k);
        return stdDeviation * stdDeviation;
    }

    @Override
    public void setMean(double m) {
        requirePositive(m, "Mean");
        mean = m;

    }

    @Override
    public void setStdDeviation(double s) {
        requirePositive(s, "Standard deviation");
        setMean(s * Math.sqrt(k));

    }

    @Override
    public void setMeanAndStdDeviation(double m, double s) {
        requirePositive(m, "Mean");
        requirePositive(s, "Standard deviation");
        double cvar = s / m;
        long shape = Math.round(1.0 / (cvar * cvar));
        if (shape < 1 || shape > Integer.MAX_VALUE
                || Math.abs(cvar - 1.0 / Math.sqrt(shape)) > 4 * Math.ulp(cvar)) {
            throw new UnsupportedOperationException("Erlang CV must equal 1/sqrt(k) for an integer k >= 1.");
        }
        k = (int) shape;
        mean = m;
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive.");
        }

    }

    @Override
    public String getType() {
        return "Erlang-k";
    }

    @Override
    public String toString() {
        return "Erlang(k=" + k + ")";
    }
}
