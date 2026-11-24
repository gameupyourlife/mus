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

## Task 6 — Batch-means estimates across the grid

[Task 6 results](task6-grid.csv) gives the simulated mean wait in seconds and the excess probability `P(W > 5 s)` with their two-sided 90% batch-means confidence intervals at all 57 settings. Each setting discards 10,000 warm-up waits and then uses 250 batches of 20,000 waits, for 5,000,000 measured waits. The fixed run length gives rare exceedances more time to occur and avoids selecting intervals through the mean-only precision rule. The CSV includes the exceedance count, and probability bounds are restricted to `[0, 1]`.

```powershell
java -Xmx256m -cp blatt5/out study.SimulationStudy --grid | Set-Content -Encoding utf8 blatt5/task6-grid.csv
```

## Tasks 7 and 8 — Interval comparison at utilization 0.95 and arrival CV 2

Both runs discard 10,000 warm-up waits and use the same seeds. The run with `lbatch = 20000` meets the mean-wait precision rule after 163 batches, or 3,260,000 retained waits. The same observations give a mean wait of 46.2726 s and an excess probability of 0.885976 under either calculation.

| Metric | Interval calculation | 90% interval | Full width |
| --- | --- | ---: | ---: |
| Mean wait | Batch means | [44.0707, 48.4745] s | 4.4038 s |
| Mean wait | Individual waits assumed independent | [46.2315, 46.3138] s | 0.08230 s |
| Excess probability | Batch proportions | [0.881872, 0.890079] | 0.008207 |
| Excess probability | Individual indicators assumed independent | [0.885686, 0.886265] | 0.000579 |

The mean-wait interval from batch means is about 53.5 times wider on the same data. Consecutive waits often share the same backlog. The independence calculation omits the positive serial covariance and underestimates the sampling variance. The batch means retain the effect of dependence within each batch.

With `lbatch = 1`, the rule stops after three retained waits and reports a mean of 73.1997 s with a nominal interval of [70.5791, 75.8202] s. The three waits come from one short stretch of the same queue trace, so their apparent precision is misleading. The batch-means estimate and interval are the defensible simulation results. As a separate check, the continuous-time model gives a steady-state mean of 48.4897 s. It lies just above this particular 90% batch-means interval. This reference uses `sigma = (1 + 4(1 - sigma)/0.95)^(-1/4)` and `E[W] = sigma / (1 - sigma)`. [Adan and Resing, Queueing Theory, §§8.1–8.3](https://classes.engineering.wustl.edu/cse538-online/images/0/0f/Adan_and_Resing_Queueing_Theory.pdf#page=80)

```powershell
java -cp blatt5/out study.SimulationStudy --rho 0.95 2 10000 20000
java -cp blatt5/out study.SimulationStudy --rho 0.95 2 10000 1
```
