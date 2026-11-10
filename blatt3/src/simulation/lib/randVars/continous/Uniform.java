package simulation.lib.randVars.continous;

import simulation.lib.randVars.RandVar;
import simulation.lib.rng.RNG;

/**
 * Uniformly distributed random variable.
 */
public class Uniform extends RandVar {

    private double a;
    private double b;

    public Uniform(RNG rng) {
        super(rng);
        a = 0;
        b = 1;
    }

    /**
     * @see RandVar#getRV()
     */
    @Override
    public double getRV() {
        return a + (b - a) * rng.rnd();
    }

    /**
     * @see RandVar#getMean()
     */
    @Override
    public double getMean() {
        return a / 2 + b / 2;
    }

    /**
     * @see RandVar#getVariance()
     */
    @Override
    public double getVariance() {
        double width = b - a;
        return width * width / 12;
    }

    /**
     * @see RandVar#setMean(double)
     */
    @Override
    public void setMean(double m) {
        if (!Double.isFinite(m)) {
            throw new IllegalArgumentException("mean must be finite");
        }
        double halfWidth = (b - a) / 2;
        double lower = m - halfWidth;
        double upper = m + halfWidth;
        if (!Double.isFinite(lower) || !Double.isFinite(upper)) {
            throw new IllegalArgumentException("mean produces non-finite bounds");
        }
        a = lower;
        b = upper;
    }

    /**
     * @see RandVar#setStdDeviation(double)
     */
    @Override
    public void setStdDeviation(double s) {
        setMeanAndStdDeviation(getMean(), s);
    }

    /**
     * @see RandVar#setMeanAndStdDeviation(double, double)
     */
    @Override
    public void setMeanAndStdDeviation(double m, double s) {
        if (!Double.isFinite(m) || !Double.isFinite(s) || s < 0) {
            throw new IllegalArgumentException("mean must be finite and standard deviation must be finite and nonnegative");
        }

        double halfWidth = Math.sqrt(3) * s;
        double lower = m - halfWidth;
        double upper = m + halfWidth;
        if (!Double.isFinite(lower) || !Double.isFinite(upper)) {
            throw new IllegalArgumentException("mean and standard deviation produce non-finite bounds");
        }
        a = lower;
        b = upper;
    }

    /**
     * @see RandVar#getType()
     */
    @Override
    public String getType() {
        return "Uniform";
    }

    @Override
    public String toString() {
        return super.toString()
                + "\tparameters:\n"
                + "\t\ta: " + a + "\n"
                + "\t\tb: " + b + "\n";
    }
}
