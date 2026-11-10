package study;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Function;

import simulation.lib.counter.DiscreteCounter;
import simulation.lib.histogram.DiscreteHistogram;
import simulation.lib.randVars.RandVar;
import simulation.lib.randVars.continous.ErlangK;
import simulation.lib.randVars.continous.Exponential;
import simulation.lib.randVars.continous.HyperExponential;
import simulation.lib.randVars.continous.Uniform;
import simulation.lib.rng.RNG;
import simulation.lib.rng.StdRNG;

public class RandVarTest {
    private static final int SAMPLE_SIZE = 1_000_000;
    private static final double MEAN = 1.0;
    private static final double[] CVARS = {0.1, 1.0, 2.0};
    private static final double MOMENT_TOLERANCE = 1e-12;
    private static final int HISTOGRAM_INTERVALS = 100;
    private static final double HISTOGRAM_LOWER_BOUND = 0.0;
    private static final double HISTOGRAM_UPPER_BOUND = 10.0;
    private static final String DEFAULT_OUTPUT_DIR = "results/randvar";

    public static void main(String[] args) {
        testRandVars(args.length == 0 ? DEFAULT_OUTPUT_DIR : args[0]);
    }

    public static void testRandVars() {
        testRandVars(DEFAULT_OUTPUT_DIR);
    }

    private static void testRandVars(String outputDir) {
        System.out.printf(Locale.ROOT, "%-28s %7s %9s %9s %9s %9s %9s %10s %8s %8s%n",
                "Distribution", "CV exp.", "Mean exp.", "Mean obs.", "Var exp.", "Var obs.", "CV obs.", "n",
                "x < 0", "x >= 10");

        testDistribution(Uniform::new, outputDir);
        testDistribution(Exponential::new, outputDir);
        testDistribution(ErlangK::new, outputDir);
        testDistribution(HyperExponential::new, outputDir);
        plotHistograms(outputDir);
    }

    private static void plotHistograms(String outputDir) {
        Path plotter = findPlotter();
        boolean windows = File.separatorChar == '\\';
        Path repo = plotter.getParent().getParent();
        Path localPython = repo.resolve(windows ? ".venv/Scripts/python.exe" : ".venv/bin/python");
        String python = Files.isRegularFile(localPython) ? localPython.toString() : (windows ? "python" : "python3");
        ProcessBuilder plot = new ProcessBuilder(python, plotter.toString(), outputDir, "--pdf").inheritIO();
        plot.environment().put("MPLBACKEND", "Agg");

        try {
            int status = plot.start().waitFor();
            if (status != 0) {
                throw new IllegalStateException("PDF plotter exited with status " + status
                        + ". Check the Python output above.");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not launch Python. Install Python and Matplotlib "
                    + "or configure them in the repository's .venv.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while generating histogram PDF.", e);
        }
    }

    private static Path findPlotter() {
        for (Path directory = Paths.get("").toAbsolutePath(); directory != null; directory = directory.getParent()) {
            Path plotter = directory.resolve("blatt3").resolve("plot_histograms.py");
            if (Files.isRegularFile(plotter)) {
                return plotter;
            }
        }
        throw new IllegalStateException("Could not find blatt3/plot_histograms.py. Run from the repository.");
    }

    public static void testUniform() {
        testDistribution(Uniform::new, DEFAULT_OUTPUT_DIR);
    }

    private static void testDistribution(Function<RNG, RandVar> create, String outputDir) {
        for (double cvar : CVARS) {
            RandVar randVar = create.apply(new StdRNG());
            try {
                randVar.setMeanAndCvar(MEAN, cvar);
            } catch (UnsupportedOperationException e) {
                skip(randVar.getType(), cvar, e.getMessage());
                continue;
            }
            sample(scenarioName(randVar, cvar), randVar, cvar, outputDir);
        }
    }

    private static String scenarioName(RandVar randVar, double cvar) {
        if (randVar instanceof ErlangK) {
            return randVar.toString();
        }
        if (randVar instanceof HyperExponential) {
            return cvar == 1.0 ? "H2 (equal rates)" : "Hyperexponential H2";
        }
        return randVar.getType();
    }

    private static void sample(String name, RandVar randVar, double cvar, String outputDir) {
        double mean = randVar.getMean();
        double variance = randVar.getVariance();
        double analyticalCvar = randVar.getCvar();
        double requestedStdDeviation = MEAN * cvar;
        checkMoment(name, "mean", MEAN, mean);
        checkMoment(name, "variance", requestedStdDeviation * requestedStdDeviation, variance);
        checkMoment(name, "CV", cvar, analyticalCvar);

        DiscreteCounter counter = new DiscreteCounter(name);
        String histogramName = String.format(Locale.ROOT, "%s, CV %.1f", name, cvar);
        DiscreteHistogram histogram = new DiscreteHistogram(histogramName, HISTOGRAM_INTERVALS,
                HISTOGRAM_LOWER_BOUND, HISTOGRAM_UPPER_BOUND);
        int belowRange = 0;
        int aboveRange = 0;
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            double value = randVar.getRV();
            counter.count(value);
            histogram.count(value);
            // The histogram puts out-of-range samples in the first or last bin.
            if (value < HISTOGRAM_LOWER_BOUND) {
                belowRange++;
            } else if (value >= HISTOGRAM_UPPER_BOUND) {
                aboveRange++;
            }
        }
        histogram.csvReport(outputDir);
        System.out.printf(Locale.ROOT, "%-28s %7.1f %9.6f %9.6f %9.6f %9.6f %9.6f %10d %8d %8d%n",
                name, analyticalCvar, mean, counter.getMean(), variance, counter.getVariance(),
                counter.getCvar(), counter.getNumSamples(), belowRange, aboveRange);
        if (randVar instanceof Uniform && belowRange > 0) {
            System.out.println("  Requires a negative lower bound. For a >= 0, CV <= 1/sqrt(3).");
        }
    }

    private static void checkMoment(String name, String moment, double expected, double actual) {
        if (!(Math.abs(actual - expected) <= MOMENT_TOLERANCE * Math.abs(expected))) {
            throw new IllegalStateException(name + " " + moment + " expected " + expected + ", got " + actual);
        }
    }

    private static void skip(String name, double cvar, String reason) {
        System.out.printf(Locale.ROOT, "%-28s %7.1f  Unavailable: %s%n", name, cvar, reason);
    }
}
