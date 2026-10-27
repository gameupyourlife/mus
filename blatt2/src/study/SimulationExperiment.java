package study;

import java.nio.file.Path;

import simulation.lib.Simulator;

/**
 * Runs the requested service-time and simulation-duration combinations.
 */
public final class SimulationExperiment {
    private static final long[] SERVICE_TIMES = {9, 10, 11};
    private static final long[] SIMULATION_TIMES = {10000, 100000};

    private SimulationExperiment() {
    }

    public static void main(String[] args) {
        for (long serviceTime : SERVICE_TIMES) {
            for (long simulationTime : SIMULATION_TIMES) {
                runStudy(serviceTime, simulationTime);
            }
        }
    }

    private static void runStudy(long serviceTime, long simulationTime) {
        System.out.printf("%nserviceTime=%d s, simulationTime=%d s%n",
                serviceTime, simulationTime);

        Simulator simulator = new Simulator(serviceTime, simulationTime);
        simulator.start();
        SimulationStudy study = simulator.getStudy();
        study.printMetrics();
        study.report(Path.of("..", "results", "experiments",
            "service" + serviceTime + "_T" + simulationTime).toString());
    }
}
