"""Plot waiting-time and service-time histograms for all experiment runs.

Run from the blatt2 directory after running the Java experiment:

    python plot_waiting_service_histograms.py

Or provide another experiment directory and output file:

    python plot_waiting_service_histograms.py \
        ..\\results\\experiments --output ..\\results\\waiting_service.png

Requires matplotlib:
    pip install matplotlib
"""

import argparse
import csv
import re
from pathlib import Path

import matplotlib.pyplot as plt


RUN_PATTERN = re.compile(r"service(?P<service>\d+)_T(?P<duration>\d+)$")
TICKS_PER_SECOND = 1000.0


def read_histogram(path):
    """Read a Java histogram CSV and return interval bounds and frequencies."""
    rows = []
    with path.open(newline="", encoding="utf-8") as handle:
        for cells in csv.reader(handle, delimiter=";"):
            if not cells or cells[0].strip().startswith("#"):
                continue
            rows.append([float(cell.strip().replace(",", ".")) for cell in cells])

    if not rows:
        raise ValueError(f"Histogram file is empty: {path}")

    lower, upper, frequency = zip(*rows)
    return list(lower), list(upper), list(frequency)


def find_runs(experiment_dir):
    """Return experiment folders sorted by service time and duration."""
    runs = []
    for path in experiment_dir.iterdir():
        if not path.is_dir():
            continue
        match = RUN_PATTERN.fullmatch(path.name)
        if match is None:
            continue
        runs.append((int(match.group("service")),
                     int(match.group("duration")), path))
    return sorted(runs)


def draw_histogram(axis, path, title):
    lower, upper, frequencies = read_histogram(path)
    lower = [value / TICKS_PER_SECOND for value in lower]
    upper = [value / TICKS_PER_SECOND for value in upper]
    centers = [(low + high) / 2 for low, high in zip(lower, upper)]
    widths = [high - low for low, high in zip(lower, upper)]

    axis.bar(centers, frequencies, width=widths, align="center",
             color="#4C72B0", edgecolor="white", linewidth=0.4)
    axis.set_title(title)
    axis.set_xlabel("time (s)")
    axis.grid(axis="y", alpha=0.3, linestyle="--")
    axis.set_axisbelow(True)


def create_plot(experiment_dir, output_path):
    runs = find_runs(experiment_dir)
    if len(runs) != 6:
        raise ValueError(
            f"Expected 6 experiment folders in {experiment_dir}, found {len(runs)}")

    figure, axes = plt.subplots(2, 6, figsize=(24, 8), sharey="row")
    for column, (service, duration, run_dir) in enumerate(runs):
        title = f"service={service}s, T={duration}s"
        draw_histogram(
            axes[0, column],
            run_dir / "histograms" / "waitingTime_hist.csv",
            title,
        )
        draw_histogram(
            axes[1, column],
            run_dir / "histograms" / "serviceTime_hist.csv",
            title,
        )

    axes[0, 0].set_ylabel("Waiting time per customer\nrelative frequency")
    axes[1, 0].set_ylabel("Service time per customer\nrelative frequency")
    axes[0, 0].set_ylim(bottom=0)
    axes[1, 0].set_ylim(bottom=0)
    figure.suptitle("Waiting time and service time per customer")
    figure.tight_layout()
    output_path.parent.mkdir(parents=True, exist_ok=True)
    figure.savefig(output_path, dpi=180, bbox_inches="tight")
    plt.close(figure)


def main():
    repository_root = Path(__file__).resolve().parents[1]
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "experiment_dir",
        nargs="?",
        type=Path,
        default=repository_root / "results" / "experiments",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=repository_root / "results" / "waiting_service_histograms.pdf",
    )
    args = parser.parse_args()

    create_plot(args.experiment_dir, args.output)
    print(f"Wrote {args.output}")


if __name__ == "__main__":
    main()
