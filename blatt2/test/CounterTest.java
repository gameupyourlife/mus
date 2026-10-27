package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import simulation.lib.counter.DiscreteCounter;

class CounterTest {

    @Test
    void calculatesEmpiricalStatistics() {
        DiscreteCounter counter = new DiscreteCounter("responseTime");

        counter.count(1);
        counter.count(2);
        counter.count(5);

        assertEquals(3, counter.getNumSamples());
        assertEquals(8.0, counter.getSumPowerOne());
        assertEquals(8.0 / 3.0, counter.getMean(), 1e-12);
        assertEquals(13.0 / 3.0, counter.getVariance(), 1e-12);
        assertEquals(1.0, counter.getMin());
        assertEquals(5.0, counter.getMax());
        assertEquals(Math.sqrt(13.0 / 3.0), counter.getStdDeviation(), 1e-12);
    }

    @Test
    void resetClearsSamplesAndBounds() {
        DiscreteCounter counter = new DiscreteCounter("responseTime");
        counter.count(4);
        counter.reset(100);

        assertEquals(0, counter.getNumSamples());
        assertEquals(0.0, counter.getSumPowerOne());
        assertEquals(Double.POSITIVE_INFINITY, counter.getMin());
        assertEquals(Double.NEGATIVE_INFINITY, counter.getMax());
    }

    @Test
    void coefficientOfVariationHandlesZeroMean() {
        DiscreteCounter counter = new DiscreteCounter("responseTime");
        counter.count(0);
        counter.count(0);

        assertEquals(0.0, counter.getVariance());
        assertEquals(0.0, counter.getCvar());
    }
}
