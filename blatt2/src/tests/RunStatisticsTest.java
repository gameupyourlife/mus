package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import simulation.lib.statistic.ContinuousCounterHistogram;
import simulation.lib.statistic.DiscreteCounterHistogram;

/**
 * We apply the statistic objects to provided sample values, which are distributed according to a uniform and a normal distribution.
 * This can be used to test your implementation.
 * Parameters of the normal distribution:
 *   mean = 10, stddev = 2
 * Parameters of the uniform distribution:
 *   min = 5, max = 15
 */
public class RunStatisticsTest {

    /** Directory containing the input CSV files. */
    static final String DATA_DIR = "./blatt2/src/tests/";

    /** Directory containing generated CSV files. */
    static final String RESULTS_DIR = "./results/";

    /** Number of histogram intervals. */
    static final int NUM_INTERVALS = 50;

    /** Histogram bounds for the normal distribution (mean=10, stddev=2). */
    static final double NORMAL_HIST_LOWER = 4.0;
    static final double NORMAL_HIST_UPPER = 16.0;

    /** Histogram bounds for the uniform distribution. */
    static final double UNIFORM_HIST_LOWER = 5.0;
    static final double UNIFORM_HIST_UPPER = 15.0;

    public static void main(String[] args) throws IOException {
        System.out.println("Normal Distribution");
        runTest(
            DATA_DIR + "testsnormal_samples.csv",
            "normal",
            NORMAL_HIST_LOWER,
            NORMAL_HIST_UPPER
        );

        System.out.println("Uniform Distribution");
        runTest(
            DATA_DIR + "testsuniform_samples.csv",
            "uniform",
            UNIFORM_HIST_LOWER,
            UNIFORM_HIST_UPPER
        );
    }

    /**
     * Reads samples from a CSV file, applies discrete and continuous
     * statistic objects, prints reports and writes CSV results.
     *
     * @param csvPath  path to the input CSV file
     * @param name     label for the statistic objects
     * @param lower    lower histogram bound
     * @param upper    upper histogram bound
     */
    private static void runTest(String csvPath, String name,
                                 double lower, double upper) throws IOException {
        new File(RESULTS_DIR).mkdirs();
        double[] samples = readCsv(csvPath);
        System.out.println("Loaded " + samples.length + " samples from: " + csvPath);

        //Discrete
        DiscreteCounterHistogram discrete =
            new DiscreteCounterHistogram(name, NUM_INTERVALS, lower, upper);

        for (double v : samples) {
            discrete.count(v);
        }

        System.out.println("\n-- Discrete --");
        System.out.println(discrete.report());
        discrete.csvReport(RESULTS_DIR);

        //Continuous
        ContinuousCounterHistogram continuous =
            new ContinuousCounterHistogram(name, NUM_INTERVALS, (int) lower, (int) upper);

        for (int i = 0; i < samples.length; i++) {
            continuous.count(samples[i], i);
        }
        continuous.count(samples[samples.length - 1], samples.length);

        System.out.println("-- Continuous --");
        System.out.println(continuous.report());
        continuous.csvReport(RESULTS_DIR);
    }

    /**
     * Reads a CSV file with one double value per line.
     * Lines starting with # are treated as comments and skipped.
     *
     * @param path  path to the CSV file
     * @return array of parsed values
     */
    private static double[] readCsv(String path) throws IOException {
        List<Double> values = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) continue;
                values.add(Double.parseDouble(line));
            }
        }
        return values.stream().mapToDouble(Double::doubleValue).toArray();
    }
}
