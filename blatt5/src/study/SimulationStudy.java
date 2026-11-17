package study;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import simulation.lib.Customer;
import simulation.lib.Simulator;
import simulation.lib.counter.DiscreteConfidenceCounterWithRelativeError;
import simulation.lib.counter.DiscreteCounter;
import simulation.lib.histogram.DiscreteHistogram;
import simulation.lib.randVars.RandVar;
import simulation.lib.randVars.continous.Exponential;
import simulation.lib.rng.StdRNG;
import simulation.lib.statistic.StatisticObject;

/** Estimates the steady-state GI/GI/1 waiting time using batch means. */
public class SimulationStudy {
    private static final long DEFAULT_NINIT = 10_000;
    private static final int DEFAULT_LBATCH = 50_000;
    private static final double ALPHA = 0.10;
    private static final double RELATIVE_ERROR_LIMIT = 0.05;
    private static final double ABSOLUTE_ERROR_LIMIT = 0.0001;

    public final long ninit;
    public final int lbatch;
    public final double interArrivalTime;
    public final double serviceTime;

    protected boolean isDebugReport = true;
    protected boolean isCsvReport = true;
    public RandVar randVarInterArrivalTime;
    public RandVar randVarServiceTime;
    public List<StatisticObject> statisticObjects;
    public long maxQS;
    public long minQS = Long.MAX_VALUE;

    private final Simulator simulator;
    private final DiscreteCounter firstTenWaits = new DiscreteCounter("first10WaitingTimeSeconds");
    private final DiscreteCounter waits = new DiscreteCounter("waitingTimeSeconds");
    private final DiscreteConfidenceCounterWithRelativeError batchMeans =
        new DiscreteConfidenceCounterWithRelativeError("batchMeanWaitingTimeSeconds", ALPHA);
    // One-second bins, with a final bin for waits of 99 seconds or more.
    private final DiscreteHistogram histogram = new DiscreteHistogram("waitingTimeSeconds", 100, 0, 100);
    private long zeroWaits;
    private double batchSum;
    private boolean simulationFinished;

    public static void main(String[] args) {
        if (args.length > 2) {
            throw new IllegalArgumentException("Usage: study.SimulationStudy [ninit [lbatch]]");
        }
        long ninit = args.length > 0 ? Long.parseLong(args[0]) : DEFAULT_NINIT;
        int lbatch = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_LBATCH;
        runStudy(ninit, lbatch, 0.80);
        runStudy(ninit, lbatch, 0.95);
    }

    private static void runStudy(long ninit, int lbatch, double meanServiceTime) {
        Simulator simulator = new Simulator(ninit, lbatch,
            new Exponential(new StdRNG(42), 1), new Exponential(new StdRNG(314159), meanServiceTime));
        simulator.start();
        simulator.report();
    }

    public SimulationStudy(Simulator sim) {
        this(sim, DEFAULT_NINIT, DEFAULT_LBATCH,
            new Exponential(new StdRNG(42), 1), new Exponential(new StdRNG(314159), 0.80));
    }

    public SimulationStudy(Simulator sim, long ninit, int lbatch, RandVar arrivals, RandVar service) {
        if (ninit < 0 || lbatch <= 0) {
            throw new IllegalArgumentException("ninit must be nonnegative and lbatch must be positive");
        }
        simulator = sim;
        this.ninit = ninit;
        this.lbatch = lbatch;
        randVarInterArrivalTime = arrivals;
        randVarServiceTime = service;
        interArrivalTime = arrivals.getMean();
        serviceTime = service.getMean();
        simulator.setSimTimeInRealTime(1_000_000);
        statisticObjects = List.of(firstTenWaits, waits, batchMeans, histogram);
    }

    public double getNextInterArrivalTime() {
        return randVarInterArrivalTime.getRV();
    }

    public double getNextServiceTime() {
        return randVarServiceTime.getRV();
    }

    public void updateOnCustomerArrival(SimulationState state, long simTime) {
        if (state.isTransientPhaseOver()) {
            updateQueueSizeBounds(state);
        }
    }

    public void updateOnServiceCompletion(SimulationState state, Customer currentCustomer, long simTime) {
        if (!state.isTransientPhaseOver()) {
            return;
        }
        updateQueueSizeBounds(state);
        double waitingTime = simulator.simTimeToRealTime(currentCustomer.getTimeInQueue());
        waits.count(waitingTime);
        if (waits.getNumSamples() <= 10) {
            firstTenWaits.count(waitingTime);
        }
        if (waitingTime == 0) {
            zeroWaits++;
        }
        histogram.count(waitingTime);

        batchSum += waitingTime;
        if (waits.getNumSamples() % lbatch == 0) {
            batchMeans.count(batchSum / lbatch);
            batchSum = 0;
            simulationFinished = batchMeans.getNumSamples() >= 2
                && (batchMeans.maxRelErr() < RELATIVE_ERROR_LIMIT
                    || batchMeans.maxAbsErr() < ABSOLUTE_ERROR_LIMIT);
        }
    }

    public boolean isSimulationFinished() {
        return simulationFinished;
    }

    public void updateQueueOccupancy(SimulationState state, long simTime) {
        // The waiting-time statistics are sampled at service completion.
    }

    private void updateQueueSizeBounds(SimulationState state) {
        minQS = Math.min(minQS, state.queueSize);
        maxQS = Math.max(maxQS, state.queueSize);
    }

    public void report() {
        double utilization = serviceTime / interArrivalTime;
        if (isDebugReport) {
            System.out.println("Mean interarrival time (s): " + interArrivalTime);
            System.out.println("Mean service time (s): " + serviceTime);
            System.out.println("Target utilization: " + utilization);
            System.out.println("Warm-up samples: " + ninit + ", samples per batch: " + lbatch);
            System.out.println("Minimum queue size: " + minQS + ", maximum queue size: " + maxQS);
            System.out.println("Zero waits: " + zeroWaits + " / " + waits.getNumSamples());
            System.out.println(firstTenWaits.report());
            System.out.println(waits.report());
            System.out.println("90% confidence interval for mean waiting time (s)");
            System.out.println(batchMeans.report());
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
