package study;

import simulation.lib.Simulator;

/**
 * Compares time-weighted and event-based measurements for one deterministic run.
 */
public final class DiscreteObservationExperiment {
    private static final long SERVICE_TIME = 9;
    private static final long SIMULATION_TIME = 10000;

    private DiscreteObservationExperiment() {
    }

    public static void main(String[] args) {
        run("continuous time-weighted counters", false);
        run("discrete event-based counters", true);
    }

    private static void run(String label, boolean discreteStateStatistics) {
        System.out.printf("%n%s%nserviceTime=%d s, simulationTime=%d s%n",
                label, SERVICE_TIME, SIMULATION_TIME);

        Simulator simulator = new Simulator(
                SERVICE_TIME, SIMULATION_TIME, discreteStateStatistics);
        simulator.start();
        simulator.getStudy().printMetrics();
    }
}
