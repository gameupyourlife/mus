package study;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import simulation.lib.counter.DiscreteConfidenceCounter;
import simulation.lib.randVars.RandVar;
import simulation.lib.randVars.continous.ErlangK;
import simulation.lib.randVars.continous.Exponential;
import simulation.lib.randVars.continous.HyperExponential;
import simulation.lib.randVars.continous.Normal;
import simulation.lib.rng.StdRNG;

/** Measures how often the confidence intervals contain the population mean. */
public class DCCTest {
    private static final double MEAN = 10.0;
    private static final double[] CVARS = {0.25, 0.5, 1.0, 2.0, 4.0};
    private static final int[] SAMPLE_SIZES = {5, 10, 50, 100};
    private static final int EXPERIMENTS = 500;
    private static final long SEED = 42L;
    private static final String DEFAULT_CSV = "results/3.2/confidence-coverage.csv";

    public static void main(String[] args) {
        testDCC(Paths.get(args.length == 0 ? DEFAULT_CSV : args[0]));
    }

    public static void testDCC() {
        testDCC(Paths.get(DEFAULT_CSV));
    }

    private static void testDCC(Path csvPath) {
        StdRNG rng = new StdRNG(SEED);
        StringBuilder csv = new StringBuilder(
                "distribution,cv,sample_size,experiments,hits_90,coverage_90,hits_95,coverage_95\n");
        System.out.printf(Locale.ROOT, "Mean %.0f, %d experiments per scenario, seed %d.%n",
                MEAN, EXPERIMENTS, SEED);
        System.out.println("Each cell gives coverage for the 90% and 95% intervals, respectively.");
        System.out.println();
        printHeader();
        testDistribution(new Normal(rng), csv);
        testDistribution(new ErlangK(rng), csv);
        testDistribution(new Exponential(rng), csv);
        testDistribution(new HyperExponential(rng), csv);
        writeCsv(csvPath.toAbsolutePath(), csv);
    }

    private static void printHeader() {
        System.out.print("| Distribution | CV |");
        for (int sampleSize : SAMPLE_SIZES) {
            System.out.print(" n = " + sampleSize + " |");
        }
        System.out.println();
        System.out.print("| :--- | ---: |");
        for (int sampleSize : SAMPLE_SIZES) {
            System.out.print(" ---: |");
        }
        System.out.println();
    }

    private static void testDistribution(RandVar distribution, StringBuilder csv) {
        String name = distribution.getType();
        String csvName = "\"" + name.replace("\"", "\"\"") + "\"";
        for (double cvar : CVARS) {
            try {
                distribution.setMeanAndCvar(MEAN, cvar);
            } catch (UnsupportedOperationException e) {
                continue;
            }

            System.out.printf(Locale.ROOT, "| %s | %s |", name, cvar);
            for (int sampleSize : SAMPLE_SIZES) {
                int[] covered = countCoverage(distribution, sampleSize);
                double coverage90 = (double) covered[0] / EXPERIMENTS;
                double coverage95 = (double) covered[1] / EXPERIMENTS;
                System.out.printf(Locale.ROOT, " %.1f%% / %.1f%% |",
                        100.0 * coverage90, 100.0 * coverage95);
                csv.append(String.format(Locale.ROOT, "%s,%s,%d,%d,%d,%.3f,%d,%.3f\n",
                        csvName, cvar, sampleSize, EXPERIMENTS, covered[0], coverage90, covered[1], coverage95));
            }
            System.out.println();
        }
    }

    private static void writeCsv(Path path, StringBuilder csv) {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, csv.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write coverage CSV to " + path, e);
        }
        System.out.println("CSV written to " + path);
    }

    private static int[] countCoverage(RandVar distribution, int sampleSize) {
        DiscreteConfidenceCounter[] counters = {
                new DiscreteConfidenceCounter(distribution.getType(), 0.10),
                new DiscreteConfidenceCounter(distribution.getType(), 0.05)
        };
        int[] covered = new int[counters.length];

        for (int experiment = 0; experiment < EXPERIMENTS; experiment++) {
            for (int i = 0; i < sampleSize; i++) {
                double sample = distribution.getRV();
                for (DiscreteConfidenceCounter counter : counters) {
                    counter.count(sample);
                }
            }
            for (int i = 0; i < counters.length; i++) {
                DiscreteConfidenceCounter counter = counters[i];
                if (counter.getLowerBound() <= MEAN && MEAN <= counter.getUpperBound()) {
                    covered[i]++;
                }
                counter.reset(0);
            }
        }
        return covered;
    }
}
