Solutions of Group 40 for WiSe 26/27 in Modellierung und Simulation 1/2

## Tests

The automated JUnit test suite is located in `blatt2/test`. Run it from the module directory:

```text
cd blatt2
mvn test
```

The sample program `tests.RunStatisticsTest` remains a manual report generator and is not part of the automated test suite.

## Blatt3 histogram report

Run the Java main class `study.RandVarTest` from the repository. It exports the
histogram CSVs and runs the Python plotter to create
`results/randvar/plots/histograms.pdf`. Pass an output directory as the first
argument to change the destination.

The plotter requires Python with Matplotlib. The Java runner uses the repository's
`.venv` if present, or `python` on Windows and `python3` on macOS and Linux.
