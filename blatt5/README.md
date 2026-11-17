# Blatt 5 queueing study

Compile and run from the repository root in PowerShell.

```powershell
javac -d blatt5/out (Get-ChildItem blatt5/src -Recurse -Filter *.java).FullName
java -cp blatt5/out study.SimulationStudy --rho 0.95 2
```

`--rho` accepts utilization from 0.05 to 0.95. The study uses exponential service times with mean 1 s and gamma interarrival times with mean `1 / rho` s. The optional interarrival CV ranges from 0.5 to 2 and defaults to 1. Two more optional arguments set the warm-up count and batch length, which default to 10,000 and 20,000. The study writes CSVs to a timestamped directory.

Both estimates use the same post-warm-up customers. For mean waiting time, the individual observations are the waits themselves. For excess-wait probability, each observation is 1 when the wait is strictly greater than 5 s and 0 otherwise. The study reports a two-sided 90% Student's *t* interval from the individual observations under the independence assumption and another from the complete batch averages. A rare event can yield an uninformative interval when the indicators have no observed variation. The batch interval for mean waiting time controls when the run stops.

## Batch length calibration

The calibration uses fixed-length runs so the precision stopping rule cannot select which batches enter the autocorrelation estimate. It simulates the same first-come, first-served queue through Lindley's waiting-time recursion and measures lag-one correlation with `DiscreteAutocorrelationCounter`.

```powershell
java -cp blatt5/out study.BatchAutocorrelationStudy 100 4096 > pilot.csv
java -cp blatt5/out study.BatchAutocorrelationStudy 20000 1536 > validation.csv
```

Each sweep covers 19 utilization values in increments of 0.05 and three interarrival CVs. With 100 waits per batch, correlation generally increased with utilization and was highest for the most variable arrivals at high utilization.

| Utilization | CV 0.5 | CV 1 | CV 2 |
| ---: | ---: | ---: | ---: |
| 0.50 | 0.014 | 0.021 | 0.095 |
| 0.75 | 0.087 | 0.169 | 0.391 |
| 0.90 | 0.554 | 0.621 | 0.811 |
| 0.95 | 0.847 | 0.858 | **0.941** |

The strongest pilot correlation occurred at utilization 0.95 and CV 2. With 20,000 waits per batch, it fell to 0.037 at that setting. The largest absolute correlation across all 57 validation runs was 0.073, below the calibration criterion of 0.10. The study therefore uses 20,000 as its default batch length. Its per-run lag-one diagnostic can be noisy when the precision rule stops after only a few batches.
