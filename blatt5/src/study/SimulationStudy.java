package study;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import simulation.lib.Customer;
import simulation.lib.Simulator;
import simulation.lib.counter.DiscreteAutocorrelationCounter;
import simulation.lib.counter.DiscreteConfidenceCounter;
import simulation.lib.counter.DiscreteConfidenceCounterWithRelativeError;
import simulation.lib.counter.DiscreteCounter;
import simulation.lib.histogram.DiscreteHistogram;
import simulation.lib.randVars.RandVar;
import simulation.lib.randVars.continous.ErlangK;
import simulation.lib.randVars.continous.Exponential;
import simulation.lib.randVars.continous.Gamma;
import simulation.lib.rng.StdRNG;
import simulation.lib.statistic.StatisticObject;

/** Estimates the steady-state GI/GI/1 waiting time using batch means. */
public class SimulationStudy {
    static final long DEFAULT_NINIT = 10_000;
    static final int DEFAULT_LBATCH = 20_000;
    private static final int GRID_BATCHES = 250;
    static final long TICKS_PER_SECOND = 1_000_000;
    private static final double ALPHA = 0.10;
    private static final double RELATIVE_ERROR_LIMIT = 0.05;
    private static final double ABSOLUTE_ERROR_LIMIT = 0.0001;
    private static final double MEAN_SERVICE_TIME = 1.0;

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
    private final DiscreteConfidenceCounter waits = new DiscreteConfidenceCounter("waitingTimeSeconds", ALPHA);
    private final DiscreteConfidenceCounter excessWaits = new DiscreteConfidenceCounter("excessWaitingProbability", ALPHA);
    private final DiscreteConfidenceCounterWithRelativeError batchMeans =
        new DiscreteConfidenceCounterWithRelativeError("batchMeanWaitingTimeSeconds", ALPHA);
    private final DiscreteConfidenceCounter batchExcessWaits =
        new DiscreteConfidenceCounter("batchExcessWaitingProbability", ALPHA);
    private final DiscreteAutocorrelationCounter batchAutocorrelation =
        new DiscreteAutocorrelationCounter("batchMeanWaitingTimeSeconds", 1);
    // One-second bins, with a final bin for waits of 99 seconds or more.
    private final DiscreteHistogram histogram = new DiscreteHistogram("waitingTimeSeconds", 100, 0, 100);
    private long zeroWaits;
    private double batchSum;
    private int batchExcessCount;
    private int fixedBatchCount;
    private boolean simulationFinished;

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--grid")) {
            runGrid();
            return;
        }
        if (args.length > 0 && args[0].equals("--rho")) {
            if (args.length < 2 || args.length > 5) {
                throw new IllegalArgumentException("Usage: study.SimulationStudy --rho utilization [arrivalCvar [ninit [lbatch]]]");
            }
            double utilization = Double.parseDouble(args[1]);
            double arrivalCvar = args.length > 2 ? Double.parseDouble(args[2]) : 1;
            long ninit = args.length > 3 ? Long.parseLong(args[3]) : DEFAULT_NINIT;
            int lbatch = args.length > 4 ? Integer.parseInt(args[4]) : DEFAULT_LBATCH;
            runStudy(ninit, lbatch, utilization, arrivalCvar);
            return;
        }
        if (args.length > 2) {
            throw new IllegalArgumentException("Usage: study.SimulationStudy [ninit [lbatch]]");
        }
        long ninit = args.length > 0 ? Long.parseLong(args[0]) : DEFAULT_NINIT;
        int lbatch = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_LBATCH;
        runStudy(ninit, lbatch, 0.80, 1);
        runStudy(ninit, lbatch, 0.95, 1);
    }

    private static void runStudy(long ninit, int lbatch, double utilization, double arrivalCvar) {
        Simulator simulator = new Simulator(ninit, lbatch, utilization, arrivalCvar);
        simulator.start();
        simulator.report();
    }

    private static void runGrid() {
        System.out.println("utilization,arrivalCvar,warmupSamples,batchLength,batches,waitingSamples,exceedances,"
            + "meanWaitSeconds,meanWaitLower90Seconds,meanWaitUpper90Seconds,pw5,pw5Lower90,pw5Upper90");
        for (int step = 1; step <= 19; step++) {
            double utilization = step / 20.0;
            for (double arrivalCvar : new double[] {0.5, 1, 2}) {
                Simulator simulator = new Simulator(DEFAULT_NINIT, DEFAULT_LBATCH, utilization, arrivalCvar);
                SimulationStudy study = simulator.getStudy();
                study.fixedBatchCount = GRID_BATCHES;
                simulator.start();
                study.printGridRow(utilization, arrivalCvar);
            }
        }
    }

    private void printGridRow(double utilization, double arrivalCvar) {
        double probabilityLower = Double.NaN;
        double probabilityUpper = Double.NaN;
        if (batchExcessWaits.getVariance() > 0) {
            probabilityLower = Math.max(0, batchExcessWaits.getLowerBound());
            probabilityUpper = Math.min(1, batchExcessWaits.getUpperBound());
        }
        System.out.printf(Locale.ROOT, "%.2f,%.1f,%d,%d,%d,%d,%d,%s,%s,%s,%s,%s,%s%n",
            utilization, arrivalCvar, ninit, lbatch, batchMeans.getNumSamples(), waits.getNumSamples(),
            Math.round(excessWaits.getSumPowerOne()), batchMeans.getMean(), Math.max(0, batchMeans.getLowerBound()),
            batchMeans.getUpperBound(), batchExcessWaits.getMean(), probabilityLower, probabilityUpper);
    }

    public SimulationStudy(Simulator sim) {
        this(sim, DEFAULT_NINIT, DEFAULT_LBATCH, 0.80, 1);
    }

    public SimulationStudy(Simulator sim, long ninit, int lbatch, double utilization, double arrivalCvar) {
        this(sim, ninit, lbatch, arrivalTimes(utilization, arrivalCvar), serviceTimes());
    }

    static RandVar arrivalTimes(double utilization, double cvar) {
        if (!(utilization >= 0.05 && utilization <= 0.95)) {
            throw new IllegalArgumentException("Utilization must be between 0.05 and 0.95");
        }
        if (!(cvar >= 0.5 && cvar <= 2)) {
            throw new IllegalArgumentException("Interarrival coefficient of variation must be between 0.5 and 2");
        }
        double mean = MEAN_SERVICE_TIME / utilization;
        StdRNG rng = new StdRNG(42);
        // Erlang-4 and exponential are the gamma special cases for these two CVs.
        if (cvar == 0.5) {
            return new ErlangK(rng, mean, 4);
        }
        if (cvar == 1) {
            return new Exponential(rng, mean);
        }
        double shape = 1 / (cvar * cvar);
        return new Gamma(rng, shape, mean / shape);
    }

    static RandVar serviceTimes() {
        return new Exponential(new StdRNG(314159), MEAN_SERVICE_TIME);
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
        simulator.setSimTimeInRealTime(TICKS_PER_SECOND);
        statisticObjects = List.of(firstTenWaits, waits, excessWaits, batchMeans, batchExcessWaits,
            batchAutocorrelation, histogram);
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
        boolean exceedsThreshold = waitingTime > 5 * serviceTime;
        excessWaits.count(exceedsThreshold ? 1 : 0);
        if (exceedsThreshold) {
            batchExcessCount++;
        }
        if (waits.getNumSamples() <= 10) {
            firstTenWaits.count(waitingTime);
        }
        if (waitingTime == 0) {
            zeroWaits++;
        }
        histogram.count(waitingTime);

        batchSum += waitingTime;
        if (waits.getNumSamples() % lbatch == 0) {
            double batchMean = batchSum / lbatch;
            batchMeans.count(batchMean);
            batchAutocorrelation.count(batchMean);
            batchExcessWaits.count((double) batchExcessCount / lbatch);
            batchSum = 0;
            batchExcessCount = 0;
            if (fixedBatchCount > 0) {
                simulationFinished = batchMeans.getNumSamples() == fixedBatchCount;
            } else {
                simulationFinished = batchMeans.getNumSamples() >= 2
                    && (batchMeans.maxRelErr() < RELATIVE_ERROR_LIMIT
                        || batchMeans.maxAbsErr() < ABSOLUTE_ERROR_LIMIT);
            }
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
            System.out.println("Interarrival coefficient of variation: " + randVarInterArrivalTime.getCvar());
            System.out.println("Mean service time (s): " + serviceTime);
            System.out.println("Target utilization: " + utilization);
            System.out.println("Warm-up samples: " + ninit + ", samples per batch: " + lbatch);
            System.out.println("Minimum queue size: " + minQS + ", maximum queue size: " + maxQS);
            System.out.println("Zero waits: " + zeroWaits + " / " + waits.getNumSamples());
            System.out.println(firstTenWaits.report());
            System.out.println("90% confidence intervals assuming independent waiting times");
            System.out.println(waits.report());
            System.out.println(excessWaits.report());
            System.out.println("90% confidence intervals using batch means");
            System.out.println(batchMeans.report());
            System.out.println(batchExcessWaits.report());
            System.out.println("Lag-1 autocorrelation of batch mean waiting times: " + batchAutocorrelation.getAutoCorrelation(1));
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
