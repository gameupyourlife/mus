package study;

import java.util.Random;

import simulation.lib.counter.DiscreteAutocorrelationCounter;

public class AutocorrelationTest {
    private static final int SAMPLE_COUNT = 12_000;
    private static final int MAX_LAG = 6;
    private static final double TOLERANCE = 1e-10;

    public static void main(String[] args) {
        testAutocorrelation();
    }

    public static void testAutocorrelation() {
        Random random = new Random(42);
        var constant = new DiscreteAutocorrelationCounter("Constant", MAX_LAG);
        var periodThree = new DiscreteAutocorrelationCounter("2, 2, -2", MAX_LAG);
        var alternating = new DiscreteAutocorrelationCounter("Alternating signs", MAX_LAG);
        var ramp = new DiscreteAutocorrelationCounter("Increasing ramp", MAX_LAG);
        var whiteNoise = new DiscreteAutocorrelationCounter("Independent Gaussian noise", MAX_LAG);
        var movingAverage = new DiscreteAutocorrelationCounter("Moving average of neighboring noise samples", MAX_LAG);

        double previousNoise = random.nextGaussian();
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            double noise = random.nextGaussian();
            constant.count(2);
            periodThree.count(i % 3 == 2 ? -2 : 2);
            alternating.count(i % 2 == 0 ? 1 : -1);
            ramp.count(i);
            whiteNoise.count(noise);
            movingAverage.count((noise + previousNoise) / 2);
            previousNoise = noise;
        }

        requireClose(0, constant.getAutoCovariance(0), TOLERANCE);
        if (!Double.isNaN(constant.getAutoCorrelation(1))) {
            throw new AssertionError("Constant series must have undefined correlation");
        }
        requireClose(32.0 / 9, periodThree.getAutoCovariance(0), TOLERANCE);
        requireClose(-0.5, periodThree.getAutoCorrelation(1), 0.001);
        requireClose(1, periodThree.getAutoCorrelation(3), TOLERANCE);
        requireClose(-1, alternating.getAutoCorrelation(1), TOLERANCE);
        requireClose(1, alternating.getAutoCorrelation(2), TOLERANCE);
        requireClose(1, ramp.getAutoCorrelation(6), 0.01);
        requireClose(0.5, movingAverage.getAutoCorrelation(1), 0.05);
        for (int lag = 1; lag <= MAX_LAG; lag++) {
            requireClose(0, whiteNoise.getAutoCorrelation(lag), 0.05);
        }
        for (int lag = 2; lag <= MAX_LAG; lag++) {
            requireClose(0, movingAverage.getAutoCorrelation(lag), 0.05);
        }

        for (var counter : new DiscreteAutocorrelationCounter[] {
                constant, periodThree, alternating, ramp, whiteNoise, movingAverage
        }) {
            System.out.println(counter.report());
        }

        alternating.setMaxLag(8);
        requireClose(1, alternating.getAutoCorrelation(8), TOLERANCE);
        alternating.reset();
        alternating.count(10, 1);
        alternating.count(14);
        requireClose(2, alternating.getNumSamples(), TOLERANCE);
        requireClose(12, alternating.getMean(), TOLERANCE);
        requireClose(4, alternating.getAutoCovariance(0), TOLERANCE);
        requireClose(-4, alternating.getAutoCovariance(1), TOLERANCE);
        if (!Double.isNaN(alternating.getAutoCovariance(2))) {
            throw new AssertionError("A lag without sample pairs must be undefined");
        }
    }

    private static void requireClose(double expected, double actual, double tolerance) {
        if (!(Math.abs(expected - actual) <= tolerance)) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }
}
