package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import simulation.lib.statistic.DiscreteCounterHistogram;

class CompositeStatisticTest {

    @Test
    void countUpdatesTheCounterAndResetClearsIt() {
        DiscreteCounterHistogram statistics =
                new DiscreteCounterHistogram("responseTime", 4, 0, 4);

        statistics.count(1);
        statistics.count(3);

        assertEquals(2, statistics.getNumSamples());
        assertEquals(2.0, statistics.getMean(), 1e-12);
        assertEquals(2.0, statistics.getVariance(), 1e-12);

        statistics.reset(50);

        assertEquals(0, statistics.getNumSamples());
    }
}
