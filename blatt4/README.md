# Exercises 4.2 and 4.3

From the repository root in PowerShell, compile and run the two entry points.

```powershell
javac -d blatt4/out (Get-ChildItem blatt4/src -Recurse -Filter *.java).FullName
java -cp blatt4/out study.AutocorrelationTest
java -cp blatt4/out study.SimulationStudy
```

The simulation writes one timestamped report directory per utilization to the current
directory. Each contains a waiting-time histogram and the covariance and correlation
values for lags 0 through 20. To plot a run with Matplotlib installed, pass its directory
to `python blatt4/plot_histograms.py <run-directory>`.

## 4.2 Autocorrelation counter

For lag `k`, the counter averages the products of deviations from the overall sample
mean across the `n - k` available pairs. It divides by the lag-zero covariance to
obtain the autocorrelation. A lag without pairs is undefined, as is the autocorrelation
of a constant series.

| Series | Observed behavior |
| --- | --- |
| Constant 2 | Covariance is zero at every lag and autocorrelation is undefined. |
| Repeated 2, 2, −2 | Autocorrelation is about −0.5 at lags 1 and 2, then returns to 1 at lag 3. |
| Alternating 1, −1 | Autocorrelation is −1 at odd lags and 1 at even lags. |
| Increasing ramp | Nearby samples follow the same trend. Autocorrelation is 0.999 at lag 6. |
| Independent Gaussian noise | The observed autocorrelations at lags 1–6 are close to zero. |
| Two-sample moving average | Neighbors share one noise sample. Lag 1 is 0.509 and higher lags are close to zero. |

## 4.3 M/M/1 simulation

With a mean interarrival time of 1 s, utilization equals the mean service time divided
by 1 s. The required mean service time is **0.80 s** at 80% utilization and **0.95 s**
for the comparison run. Both runs start with an empty queue and use the same arrival
and service seeds.

| Utilization | Mean wait, first 10 | Mean wait, first 100,000 | Steady-state mean wait |
| --- | ---: | ---: | ---: |
| 80% | 4.944 s | 3.237 s | 3.200 s |
| 95% | 6.446 s | 18.686 s | 18.050 s |

The first ten waits are affected by the empty initial state and a few early arrivals
and service times. A short sample can land on either side of the long-run mean, as
these runs show. The 100,000-customer samples give estimates much closer to the
steady-state M/M/1 mean `ρ × E[S] / (1 − ρ)`.

Waiting-time histogram for all 100,000 measured customers, with the exported
one-second bins grouped for display.

[80% utilization histogram](plots/waiting-time-rho80.png) ·
[95% utilization histogram](plots/waiting-time-rho95.png)

| Wait (s) | 80% utilization | 95% utilization |
| --- | ---: | ---: |
| [0, 1) | 37.651% | 10.264% |
| [1, 5) | 39.112% | 16.510% |
| [5, 10) | 16.257% | 15.646% |
| [10, 20) | 6.543% | 21.176% |
| [20, 50) | 0.437% | 28.792% |
| 50 or more | 0.000% | 7.612% |

Exactly zero waits account for 20.137% and 5.374% of the two samples, respectively.
The higher utilization leaves less idle time and produces a longer right tail.
The last exported histogram bin includes every wait of 99 seconds or more.

| Lag | 80% utilization | 95% utilization |
| --- | ---: | ---: |
| 1 | 0.964 | 0.997 |
| 5 | 0.847 | 0.987 |
| 10 | 0.734 | 0.975 |
| 20 | 0.564 | 0.951 |

At 95% utilization the queue takes longer to clear, so a backlog affects more
successive customers and the correlation decays more slowly across lags.
