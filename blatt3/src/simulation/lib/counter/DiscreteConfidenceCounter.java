package simulation.lib.counter;

import java.util.Arrays;

/**
 * A discrete counter with a Student-t confidence interval for the sample mean.
 */
public class DiscreteConfidenceCounter extends DiscreteCounter {
    private static final double DEFAULT_ALPHA = 0.05;
    private final double alpha;

    // Degrees of freedom, followed by quantile rows for alpha 0.01, 0.05 and 0.10.
    private static final double[][] T_ALPHA_MATRIX = {
            {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 1000000},
            {63.657, 9.925, 5.841, 4.604, 4.032, 3.707, 3.499, 3.355, 3.250, 3.169, 2.845, 2.750, 2.704, 2.678, 2.660, 2.648, 2.639, 2.632, 2.626, 2.576},
            {12.706, 4.303, 3.182, 2.776, 2.571, 2.447, 2.365, 2.306, 2.262, 2.228, 2.086, 2.042, 2.021, 2.009, 2.000, 1.994, 1.990, 1.987, 1.984, 1.960},
            {6.314, 2.920, 2.353, 2.132, 2.015, 1.943, 1.895, 1.860, 1.833, 1.812, 1.725, 1.697, 1.684, 1.676, 1.671, 1.667, 1.664, 1.662, 1.660, 1.645}};

    public DiscreteConfidenceCounter(String variable) {
        this(variable, DEFAULT_ALPHA);
    }

    public DiscreteConfidenceCounter(String variable, double alpha) {
        super(variable);
        if (!(alpha > 0.0 && alpha < 1.0)) {
            throw new IllegalArgumentException("Alpha must be between 0 and 1 (exclusive).");
        }
        this.alpha = alpha;
    }

    public DiscreteConfidenceCounter(String variable, String type, double alpha) {
        this(variable, alpha);
        statisticsType = type;
    }

    /** Returns the table quantile for the given degrees of freedom. */
    public double getT(long degsOfFreedom) {
        double[] degrees = T_ALPHA_MATRIX[0];
        double[] quantiles = T_ALPHA_MATRIX[getRow()];
        int index = Arrays.binarySearch(degrees, degsOfFreedom);
        if (index >= 0) {
            return quantiles[index];
        }

        int high = -index - 1;
        if (high == 0 || high == degrees.length) {
            return quantiles[degrees.length - 1];
        }
        int low = high - 1;
        return linearInterpol(degrees[low], degrees[high], quantiles[low], quantiles[high], degsOfFreedom);
    }

    private double linearInterpol(double dfLow, double dfHigh, double tLow, double tHigh, long degsOfFreedom) {
        double fraction = (degsOfFreedom - dfLow) / (dfHigh - dfLow);
        return tLow + fraction * (tHigh - tLow);
    }

    private int getRow() {
        if (alpha < 0.05) {
            return 1;
        }
        if (alpha < 0.10) {
            return 2;
        }
        return 3;
    }

    private double getSampleT() {
        long n = getNumSamples();
        return n < 2 ? Double.NaN : getT(n - 1);
    }

    /** Returns the half-width of the interval, or NaN with fewer than two samples. */
    public double getBound() {
        return getSampleT() * (getStdDeviation() / Math.sqrt(getNumSamples()));
    }

    public double getLowerBound() {
        return getMean() - getBound();
    }

    public double getUpperBound() {
        return getMean() + getBound();
    }

    @Override
    public String report() {
        return super.report() + "\n" +
                "\talpha = " + alpha + "\n" +
                "\tt(1-alpha/2) = " + getSampleT() + "\n" +
                "\tlower bound = " + getLowerBound() + "\n" +
                "\tupper bound = " + getUpperBound();
    }

    @Override
    public void csvReport(String outputdir) {
        String content = observedVariable + ";" + getNumSamples() + ";" + getMean() + ";" + getVariance() + ";" + getStdDeviation() + ";" +
                getCvar() + ";" + getMin() + ";" + getMax() + ";" + alpha + ";" + getSampleT() + ";" + getLowerBound() + ";" +
                getUpperBound() + "\n";
        String labels = "#counter ; numSamples ; MEAN; VAR; STD; CVAR; MIN; MAX;alpha;t(1-alpha/2);lowerBound;upperBound\n";
        writeCsv(outputdir, content, labels);
    }
}
