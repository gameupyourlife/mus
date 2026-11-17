package study;

import java.util.Locale;
import java.util.stream.IntStream;

import simulation.lib.counter.DiscreteAutocorrelationCounter;
import simulation.lib.randVars.RandVar;

/** Measures lag-one autocorrelation of batch means on the exercise grid. */
public class BatchAutocorrelationStudy {
    private static final double[] ARRIVAL_CVARS = {0.5, 1, 2};

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException("Usage: study.BatchAutocorrelationStudy lbatch numBatches [ninit]");
        }
        int lbatch = Integer.parseInt(args[0]);
        int numBatches = Integer.parseInt(args[1]);
        long ninit = args.length == 3 ? Long.parseLong(args[2]) : SimulationStudy.DEFAULT_NINIT;
        if (lbatch <= 0 || numBatches < 2 || ninit < 0) {
            throw new IllegalArgumentException("Require positive lbatch, at least two batches, and nonnegative ninit");
        }

        System.out.println("utilization,arrivalCvar,lbatch,numBatches,ninit,lag1Autocorrelation");
        IntStream.rangeClosed(1, 19).parallel()
            .mapToObj(step -> evaluateUtilization(step / 20.0, lbatch, numBatches, ninit))
            .forEachOrdered(System.out::print);
    }

    private static String evaluateUtilization(double utilization, int lbatch, int numBatches, long ninit) {
        StringBuilder rows = new StringBuilder();
        for (double arrivalCvar : ARRIVAL_CVARS) {
            var arrivals = SimulationStudy.arrivalTimes(utilization, arrivalCvar);
            var service = SimulationStudy.serviceTimes();
            var batchMeans = new DiscreteAutocorrelationCounter("batchMeanWaitingTimeSeconds", 1);
            long wait = 0;
            for (long discarded = 0; discarded < ninit; discarded++) {
                wait = nextWait(wait, arrivals, service);
            }
            for (int batch = 0; batch < numBatches; batch++) {
                double waitingTicks = 0;
                for (int sample = 0; sample < lbatch; sample++) {
                    waitingTicks += wait;
                    wait = nextWait(wait, arrivals, service);
                }
                batchMeans.count(waitingTicks / lbatch / SimulationStudy.TICKS_PER_SECOND);
            }
            rows.append(String.format(Locale.ROOT, "%.2f,%.1f,%d,%d,%d,%.8f%n",
                utilization, arrivalCvar, lbatch, numBatches, ninit, batchMeans.getAutoCorrelation(1)));
        }
        return rows.toString();
    }

    private static long nextWait(long wait, RandVar arrivals, RandVar service) {
        // Lindley's recursion for the same FCFS queue, with the event simulator's time rounding.
        long serviceTicks = (long) Math.ceil(service.getRV() * SimulationStudy.TICKS_PER_SECOND);
        long arrivalTicks = (long) Math.ceil(arrivals.getRV() * SimulationStudy.TICKS_PER_SECOND);
        return Math.max(0, wait + serviceTicks - arrivalTicks);
    }
}
