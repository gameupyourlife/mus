package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import simulation.lib.histogram.ContinuousHistogram;
import simulation.lib.histogram.DiscreteHistogram;

class HistogramTest {

    @Test
    void putsValuesAtBoundsIntoTheExpectedBins() {
        ExposedDiscreteHistogram histogram = new ExposedDiscreteHistogram("load", 3, 0, 3);

        histogram.count(-1);
        histogram.count(0);
        histogram.count(0.99);
        histogram.count(1);
        histogram.count(2.99);
        histogram.count(3);
        histogram.count(10);

        assertEquals(3.0, histogram.bin(0));
        assertEquals(1.0, histogram.bin(1));
        assertEquals(3.0, histogram.bin(2));
        assertEquals(7, histogram.getNumSamples());
    }

    @Test
    void resetClearsDiscreteHistogram() {
        ExposedDiscreteHistogram histogram = new ExposedDiscreteHistogram("load", 2, 0, 2);
        histogram.count(1);
        histogram.reset(20);

        assertEquals(0.0, histogram.bin(0));
        assertEquals(0.0, histogram.bin(1));
        assertEquals(0, histogram.getNumSamples());
    }

    @Test
    void continuousHistogramWeightsEachStateByItsDuration() {
        ExposedContinuousHistogram histogram = new ExposedContinuousHistogram("queue", 3, 0, 3);

        histogram.count(0, 0);
        histogram.count(1, 2);
        histogram.count(2, 5);

        assertEquals(2.0, histogram.bin(0));
        assertEquals(3.0, histogram.bin(1));
        assertEquals(0.0, histogram.bin(2));
        assertEquals(5.0, histogram.normalizingFactor());
    }

    private static final class ExposedDiscreteHistogram extends DiscreteHistogram {
        private ExposedDiscreteHistogram(String variable, int intervals, double lower, double upper) {
            super(variable, intervals, lower, upper);
        }

        private double bin(int index) {
            return getBinValue(index);
        }
    }

    private static final class ExposedContinuousHistogram extends ContinuousHistogram {
        private ExposedContinuousHistogram(String variable, int intervals, int lower, int upper) {
            super(variable, intervals, lower, upper);
        }

        private double bin(int index) {
            return getBinValue(index);
        }

        private double normalizingFactor() {
            return getNormalizingFactor();
        }
    }
}
