package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import simulation.lib.histogram.ContinuousHistogram;
import simulation.lib.histogram.DiscreteHistogram;

class HistogramTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void assignsExactBoundariesToTheExpectedBins() {
        ExposedDiscreteHistogram histogram = new ExposedDiscreteHistogram("load", 4, 0, 8);

        assertEquals(0, histogram.binNumber(-1));
        assertEquals(0, histogram.binNumber(0));
        assertEquals(1, histogram.binNumber(2));
        assertEquals(3, histogram.binNumber(6));
        assertEquals(3, histogram.binNumber(8));
        assertEquals(3, histogram.binNumber(100));
    }

    @Test
    void resetClearsDiscreteHistogram() {
        ExposedDiscreteHistogram histogram = new ExposedDiscreteHistogram("load", 2, 0, 2);
        histogram.count(1);
        histogram.reset(20);

        assertEquals(0.0, histogram.bin(0));
        assertEquals(0.0, histogram.bin(1));
        assertEquals(0, histogram.samples());
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
        assertEquals(3, histogram.samples());
        assertEquals(5.0, histogram.normalizingFactor());
    }

    @Test
    void resetClearsContinuousBinsAndTimeWindow() {
        ExposedContinuousHistogram histogram = new ExposedContinuousHistogram("queue", 2, 0, 2);
        histogram.count(1, 0);
        histogram.count(1, 4);
        histogram.reset(10);

        assertEquals(0.0, histogram.bin(0));
        assertEquals(0.0, histogram.bin(1));
        assertEquals(0, histogram.samples());
        assertEquals(0.0, histogram.normalizingFactor());
    }

    @Test
    void writesAllCsvViewsToTheRequestedDirectory() throws Exception {
        ExposedDiscreteHistogram histogram = new ExposedDiscreteHistogram("load", 2, 0, 2);
        histogram.count(0.5);
        histogram.csvReport(temporaryDirectory.toString());

        Path outputDirectory = temporaryDirectory.resolve("histograms");
        assertTrue(Files.exists(outputDirectory.resolve("load_hist.csv")));
        assertTrue(Files.exists(outputDirectory.resolve("load_pdf.csv")));
        assertTrue(Files.exists(outputDirectory.resolve("load_dist.csv")));
        assertTrue(Files.readString(outputDirectory.resolve("load_hist.csv"))
                .contains("#lowerBound ; upperBound ; relative frequency"));
    }

    private static final class ExposedDiscreteHistogram extends DiscreteHistogram {
        private ExposedDiscreteHistogram(String variable, int intervals, double lower, double upper) {
            super(variable, intervals, lower, upper);
        }

        private double bin(int index) {
            return getBinValue(index);
        }

        private int binNumber(double value) {
            return getBinNumber(value);
        }

        private long samples() {
            return numSamples;
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

        private long samples() {
            return numSamples;
        }
    }
}
