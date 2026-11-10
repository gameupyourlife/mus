package simulation.lib.randVars.continous;

import simulation.lib.randVars.RandVar;
import simulation.lib.rng.RNG;

/**
 * Erlang-k distributed random variable.
 */
public class ErlangK extends RandVar {

    private int k;
    private double lambda;

    public ErlangK(RNG rng) {
        super(rng);
        setMeanAndStdDeviation(1, 1);
    }

    /**
     * @see RandVar#getRV()
     */
    @Override
    public double getRV() {
        double value = 0;
        for (int i = 0; i < k; i++) {
            value -= Math.log1p(-rng.rnd()) / lambda;
        }
        return value;
    }

    /**
     * @see RandVar#getMean()
     */
    @Override
    public double getMean() {
        return k / lambda;
    }

    /**
     * @see RandVar#getVariance()
     */
    @Override
    public double getVariance() {
        return k / (lambda * lambda);
    }

    /**
     * @see RandVar#setMean(double)
     */
    @Override
    public void setMean(double m) {
        setMeanAndStdDeviation(m, getStdDeviation());
    }

    /**
     * @see RandVar#setStdDeviation(double)
     */
    @Override
    public void setStdDeviation(double s) {
        setMeanAndStdDeviation(getMean(), s);
    }

    /**
     * Sets moments whose ratio corresponds to a positive integer Erlang shape.
     *
     * @see RandVar#setMeanAndStdDeviation(double, double)
     */
    @Override
    public void setMeanAndStdDeviation(double m, double s) {
        if (!Double.isFinite(m) || !Double.isFinite(s) || m <= 0 || s <= 0) {
            throw new IllegalArgumentException("mean and standard deviation must be finite and positive");
        }

        double shape = (m / s) * (m / s);
        double roundedShape = Math.rint(shape);
        if (!Double.isFinite(shape)
                || roundedShape < 1
                || roundedShape > Integer.MAX_VALUE
                || Math.abs(shape - roundedShape) > 1e-10 * Math.max(1, shape)) {
            throw new IllegalArgumentException(
                    "mean and standard deviation must define a positive integer Erlang shape");
        }

        k = (int) roundedShape;
        lambda = k / m;
    }

    /**
     * @see RandVar#getType()
     */
    @Override
    public String getType() {
        return "k-Erlangen";
    }

    @Override
    public String toString() {
        return super.toString()
                + "\tparameters:\n"
                + "\t\tk: " + k + "\n"
                + "\t\tlambda: " + lambda + "\n";
    }
}
