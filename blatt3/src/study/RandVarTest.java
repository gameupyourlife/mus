package study;

import java.util.Locale;

import simulation.lib.counter.DiscreteCounter;
import simulation.lib.randVars.RandVar;
import simulation.lib.randVars.continous.ErlangK;
import simulation.lib.randVars.continous.Exponential;
import simulation.lib.randVars.continous.HyperExponential;
import simulation.lib.randVars.continous.Uniform;
import simulation.lib.rng.StdRNG;

public class RandVarTest {
    private static final int SAMPLE_SIZE = 1_000_000;
    private static final double MEAN = 1.0;
    private static final double[] CVARS = {0.1, 1.0, 2.0};

    public static void main(String[] args) {
        testRandVars();
    }

    public static void testRandVars() {
        System.out.printf(Locale.ROOT, "%-28s %7s %9s %9s %9s %9s %9s %10s%n",
                "Distribution", "CV exp.", "Mean exp.", "Mean obs.", "Var exp.", "Var obs.", "CV obs.", "n");

        testUniform();
        for (double cvar : CVARS) {
            if (cvar == 1.0) {
                sample("Exponential", new Exponential(new StdRNG(), 1.0 / MEAN), cvar);
            } else {
                skip("Exponential", cvar, "mean and standard deviation are equal (CV = 1).");
            }
        }
        for (double cvar : CVARS) {
            if (cvar > 1.0) {
                skip("Erlang-k", cvar, "CV = 1/sqrt(k) with a positive integer k.");
            } else {
                ErlangK erlang = new ErlangK(new StdRNG());
                erlang.setMeanAndCvar(MEAN, cvar);
                sample(erlang.toString(), erlang, cvar);
            }
        }
        for (double cvar : CVARS) {
            if (cvar < 1.0) {
                skip("Hyperexponential H2", cvar, "a mixture of exponentials has CV >= 1.");
            } else {
                HyperExponential h2 = new HyperExponential(new StdRNG());
                h2.setMeanAndCvar(MEAN, cvar);
                String name = cvar == 1.0 ? "H2 (equal rates)" : "Hyperexponential H2";
                sample(name, h2, cvar);
            }
        }
    }

    public static void testUniform() {
        for (double cvar : CVARS) {
            double halfWidth = Math.sqrt(3.0) * MEAN * cvar;
            Uniform uniform = new Uniform(new StdRNG(), MEAN - halfWidth, MEAN + halfWidth);
            sample("Uniform", uniform, cvar);
            if (halfWidth > MEAN) {
                System.out.println("  Requires a negative lower bound. For a >= 0, CV <= 1/sqrt(3).");
            }
        }
    }

    private static void sample(String name, RandVar randVar, double cvar) {
        DiscreteCounter counter = new DiscreteCounter(name);
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            counter.count(randVar.getRV());
        }
        double stdDeviation = MEAN * cvar;
        System.out.printf(Locale.ROOT, "%-28s %7.1f %9.6f %9.6f %9.6f %9.6f %9.6f %10d%n",
                name, cvar, MEAN, counter.getMean(), stdDeviation * stdDeviation, counter.getVariance(),
                counter.getCvar(), counter.getNumSamples());
    }

    private static void skip(String name, double cvar, String reason) {
        System.out.printf(Locale.ROOT, "%-28s %7.1f  Unavailable: %s%n", name, cvar, reason);
    }
}
