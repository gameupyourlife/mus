package study;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import simulation.lib.Customer;
import simulation.lib.Simulator;
import simulation.lib.counter.DiscreteAutocorrelationCounter;
import simulation.lib.counter.DiscreteCounter;
import simulation.lib.histogram.DiscreteHistogram;
import simulation.lib.randVars.RandVar;
import simulation.lib.randVars.continous.Exponential;
import simulation.lib.rng.StdRNG;
import simulation.lib.statistic.StatisticObject;

/** Runs the M/M/1 waiting-time study and reports its statistics. */
public class SimulationStudy {
    private static final int SHORT_SAMPLE = 10;
    private static final int LONG_SAMPLE = 100_000;
    private static final int MAX_LAG = 20;
    // Simulator constructs its own study, so pass the run setting through that callback.
    private static final ThreadLocal<Double> SERVICE_MEAN = ThreadLocal.withInitial(() -> 0.80);

    // A fallback horizon. The normal runs stop at the target number of completions.
    protected long cSimulationTime = 2L * LONG_SAMPLE;
    public double interArrivalTime = 1;
    public double serviceTime;

    protected boolean isDebugReport = true;
    protected boolean isCsvReport = true;
    public long simulationTime;
    public RandVar randVarInterArrivalTime;
    public RandVar randVarServiceTime;
    public List<StatisticObject> statisticObjects;
    public long maxQS;
    public long minQS;

    private final Simulator simulator;
    private final DiscreteCounter firstTenWaits = new DiscreteCounter("first10WaitingTimeSeconds");
    private final DiscreteAutocorrelationCounter waits =
        new DiscreteAutocorrelationCounter("waitingTimeSeconds", MAX_LAG);
    // One-second bins, with a final bin for waits of 99 seconds or more.
    private final DiscreteHistogram histogram = new DiscreteHistogram("waitingTimeSeconds", 100, 0, 100);
    private long zeroWaits;

    public static void main(String[] args) {
        runStudy(0.80);
        runStudy(0.95);
    }

    private static void runStudy(double meanServiceTime) {
        Simulator simulator;
        SERVICE_MEAN.set(meanServiceTime);
        try {
            simulator = new Simulator();
        } finally {
            SERVICE_MEAN.remove();
        }
        simulator.start();
        simulator.report();
    }

    public SimulationStudy(Simulator sim) {
        simulator = sim;
        serviceTime = SERVICE_MEAN.get();
        simulator.setSimTimeInRealTime(1000);
        simulationTime = simulator.realTimeToSimTime(cSimulationTime);
        randVarInterArrivalTime = new Exponential(new StdRNG(42), interArrivalTime);
        randVarServiceTime = new Exponential(new StdRNG(314159), serviceTime);
        statisticObjects = List.of(firstTenWaits, waits, histogram);
    }

    public double getNextInterArrivalTime() {
        return randVarInterArrivalTime.getRV();
    }

    public double getNextServiceTime() {
        return randVarServiceTime.getRV();
    }

    public void updateOnCustomerArrival(SimulationState state, long simTime) {
        updateQueueSizeBounds(state);
    }

    public void updateOnServiceCompletion(SimulationState state, Customer currentCustomer, long simTime) {
        updateQueueSizeBounds(state);
        double waitingTime = simulator.simTimeToRealTime(currentCustomer.getTimeInQueue());
        if (state.numSamples <= SHORT_SAMPLE) {
            firstTenWaits.count(waitingTime);
        }
        if (waitingTime == 0) {
            zeroWaits++;
        }
        waits.count(waitingTime);
        histogram.count(waitingTime);
        if (state.numSamples == LONG_SAMPLE) {
            simulator.stopEventHandler(this);
        }
    }

    public void updateQueueOccupancy(SimulationState state, long simTime) {
        // The waiting-time statistics are sampled at service completion.
    }

    private void updateQueueSizeBounds(SimulationState state) {
        minQS = Math.min(minQS, state.queueSize);
        maxQS = Math.max(maxQS, state.queueSize);
    }

    public void report() {
        if (waits.getNumSamples() != LONG_SAMPLE) {
            throw new IllegalStateException("Expected " + LONG_SAMPLE + " completed services, got "
                + waits.getNumSamples());
        }

        double utilization = serviceTime / interArrivalTime;
        if (isDebugReport) {
            System.out.println("Mean interarrival time (s): " + interArrivalTime);
            System.out.println("Mean service time (s): " + serviceTime);
            System.out.println("Target utilization: " + utilization);
            System.out.println("Minimum queue size: " + minQS + ", maximum queue size: " + maxQS);
            System.out.println("Zero waits: " + zeroWaits + " / " + waits.getNumSamples());
            System.out.println(firstTenWaits.report());
            System.out.println(waits.report());
        }
        if (isCsvReport) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            String destination = timestamp + "_SimulationStudy_rho" + Math.round(100 * utilization);
            if (!new File(destination).mkdir()) {
                throw new IllegalStateException("Cannot create report directory " + destination);
            }
            for (StatisticObject statistic : statisticObjects) {
                statistic.csvReport(destination);
            }
            System.out.println("CSV reports: " + destination);
        }
    }
}
