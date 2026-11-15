package simulation.lib.counter;

import java.util.ArrayList;
import java.util.List;

/** Estimates the autocovariance and autocorrelation of a discrete sample series. */
public class DiscreteAutocorrelationCounter extends DiscreteCounter {
    // Keep the series so increasing maxLag also works for samples already counted.
    private final List<Double> samples = new ArrayList<>();
    private int maxLag;

    public DiscreteAutocorrelationCounter(int maxLag) {
        this(null, maxLag);
    }

    public DiscreteAutocorrelationCounter(String variable, int maxLag) {
        super(variable);
        setMaxLag(maxLag);
    }

    public DiscreteAutocorrelationCounter(String variable, String type, int maxLag) {
        this(variable, maxLag);
        statisticsType = type;
    }

    public int getMaxLag() {
        return maxLag;
    }

    public void setMaxLag(int maxLag) {
        if (maxLag < 0) {
            throw new IllegalArgumentException("Maximum lag must be nonnegative");
        }
        this.maxLag = maxLag;
    }

    @Override
    public void count(double value, long time) {
        super.count(value, time);
        samples.add(value);
    }

    /** Returns the mean centered cross-product of the n - lag available pairs. */
    public double getAutoCovariance(int lag) {
        if (lag < 0 || lag > maxLag) {
            throw new IllegalArgumentException("Lag must be between 0 and " + maxLag);
        }
        int pairs = samples.size() - lag;
        if (pairs <= 0) {
            return Double.NaN;
        }
        if (getMin() == getMax()) {
            return 0;
        }

        double mean = getMean();
        double sum = 0;
        for (int i = 0; i < pairs; i++) {
            sum += (samples.get(i) - mean) * (samples.get(i + lag) - mean);
        }
        return sum / pairs;
    }

    public double getAutoCorrelation(int lag) {
        return getAutoCovariance(lag) / getAutoCovariance(0);
    }

    @Override
    public String report() {
        StringBuilder out = new StringBuilder(super.report());
        out.append("\n\tCorrelation/Covariance:\n");
        double variance = getAutoCovariance(0);
        for (int lag = 0; lag <= maxLag; lag++) {
            double covariance = lag == 0 ? variance : getAutoCovariance(lag);
            out.append("\t\tlag = ").append(lag)
                .append("   covariance = ").append(covariance)
                .append("   correlation = ").append(covariance / variance).append('\n');
        }
        return out.toString();
    }

    @Override
    public void csvReport(String outputdir) {
        StringBuilder content = new StringBuilder();
        double variance = getAutoCovariance(0);
        for (int lag = 0; lag <= maxLag; lag++) {
            double covariance = lag == 0 ? variance : getAutoCovariance(lag);
            content.append(observedVariable).append(';').append(getNumSamples()).append(';')
                .append(lag).append(';').append(covariance).append(';')
                .append(covariance / variance).append('\n');
        }
        writeCsv(outputdir, content.toString(), "#variable;numSamples;lag;covariance;correlation\n");
    }

    public void reset() {
        reset(0);
    }

    @Override
    public void reset(long time) {
        super.reset(time);
        samples.clear();
    }
}
