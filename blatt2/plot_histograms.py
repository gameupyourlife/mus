"""

Usage:
    python plot_histograms.py <folder>

Examples:
    python plot_histograms.py 20261004_144325_SimulationStudy
    python plot_histograms.py all_runs
    python plot_histograms.py all_runs --outdir comparison

The script reads every histogram CSV from <folder>/histograms and writes four
PNGs per observed variable into <folder>/plots.

If <folder> does not contain a histograms directory itself, it is treated as a
collection: every subfolder that does contain one is processed in turn and gets
its own plots subfolder. With --outdir all PNGs are written to one common
folder instead, prefixed with the name of their run.

Requires matplotlib only:
    pip install matplotlib

Reading the CSV files is done for you. The interesting part are the three
plotting functions below - each one documents what the matplotlib calls do and
what is worth changing.
"""

import argparse
import csv
import math
import sys
from itertools import accumulate
from pathlib import Path

import matplotlib.pyplot as plt


# ---------------------------------------------------------------------------
# Appearance of all plots - the first place to experiment
# ---------------------------------------------------------------------------

FIGSIZE = (7.0, 4.5)        # size of a single plot in inches
DPI = 150                   # resolution of the PNGs (use 300 for print)
COLOR_BARS = "#4C72B0"      # bar colour
COLOR_LINE = "#C44E52"      # line colour
GRID = dict(alpha=0.3, linestyle="--", linewidth=0.7)


# ---------------------------------------------------------------------------
# Reading the data - no need to change anything here
# ---------------------------------------------------------------------------

def parse_number(text):
    """Convert one cell into a number: '1,5' -> 1.5, 'NaN' -> nan."""
    return float(text.strip().replace(",", "."))


def read_histogram_csv(path):
    """
    Read one of the three histogram CSV files.

    The files use a semicolon as column separator and a comma as decimal
    separator. The first three lines are comments: type, variable name and
    column headings.

    Returns (meta, columns):
        meta    - dict with the keys "type" and "variable"
        columns - list of columns, each one a list of floats
    """
    comments = []
    rows = []

    with open(path, newline="", encoding="utf-8") as handle:
        for cells in csv.reader(handle, delimiter=";"):
            if not cells or not cells[0].strip():
                continue
            if cells[0].lstrip().startswith("#"):
                comments.append(";".join(cells).lstrip("#").strip())
                continue
            rows.append([parse_number(cell) for cell in cells])

    meta = {
        "type": comments[0] if comments else "",
        "variable": comments[1] if len(comments) > 1 else path.stem,
    }
    columns = [list(column) for column in zip(*rows)] if rows else []
    return meta, columns


def collect_histograms(run_dir):
    """
    Group the CSV files in <run_dir>/histograms by observed variable.

    Returns for example:
        {"waiting_time_per_customer": {"hist": Path, "pdf": Path, "dist": Path}}
    """
    found = {}
    for path in sorted((run_dir / "histograms").glob("*.csv")):
        for suffix in ("_hist", "_pdf", "_dist"):
            if path.stem.endswith(suffix):
                name = path.stem[: -len(suffix)]
                found.setdefault(name, {})[suffix.lstrip("_")] = path
                break
    return found


def has_data(columns):
    """True if the value column holds at least one finite number."""
    if not columns:
        return False
    return any(math.isfinite(value) for value in columns[-1])


def find_runs(path):
    """
    Decide which simulation runs to process.

    If <path> contains a histograms directory it is a single run. Otherwise it
    is treated as a collection and every subfolder holding a histograms
    directory counts as one run.
    """
    if (path / "histograms").is_dir():
        return [path]
    return [entry for entry in sorted(path.iterdir())
            if entry.is_dir() and (entry / "histograms").is_dir()]


# PLOTS

def plot_relative_frequency(ax, meta, columns):
    """
    Relative frequency as a bar chart (from <variable>_hist.csv).

    The file provides three columns: lower bound, upper bound, value.
    A bar belongs in the middle of its interval and must be exactly as wide as
    that interval - hence width=width. Without it matplotlib draws every bar
    0.8 units wide, which only fits by accident.

    Things to try:
      - change color= and edgecolor=
      - ax.set_yscale("log") to reveal long tails
      - ax.set_xlim(0, 20) to zoom into the interesting range
      - replace ax.bar() with ax.plot(centers, values, marker="o")
    """
    lower, upper, values = columns
    centers = [(low + up) / 2 for low, up in zip(lower, upper)]
    width = upper[0] - lower[0]

    ax.bar(centers, values, width=width, color=COLOR_BARS,
           edgecolor="white", linewidth=0.5)

    ax.set_title("Relative frequency")
    ax.set_xlabel(meta["variable"])
    ax.set_ylabel("relative frequency")
    ax.grid(True, **GRID)
    ax.set_axisbelow(True)          # draw the grid behind the bars


def plot_pdf(ax, meta, columns):
    """
    Probability density as a step function (from <variable>_pdf.csv).

    Same three columns as above, but the values are additionally divided by
    the interval width, so the area under the steps is 1.

    ax.stairs(values, edges) needs one edge more than values: for n intervals
    n+1 boundaries. That is why the last upper bound is appended to the list
    of lower bounds.

    Things to try:
      - fill=False with linewidth=2 for an outline only
      - overlay the theoretical density, e.g. for an exponential distribution:
            lam = 1 / 15                      # 1 / mean
            xs = [i * 0.5 for i in range(160)]
            ax.plot(xs, [lam * math.exp(-lam * x) for x in xs],
                    color=COLOR_LINE, label="theoretical")
            ax.legend()
    """
    lower, upper, values = columns
    edges = lower + [upper[-1]]

    ax.stairs(values, edges, fill=True, color=COLOR_BARS, alpha=0.75)

    ax.set_title("Probability density (PDF)")
    ax.set_xlabel(meta["variable"])
    ax.set_ylabel("probability density")
    ax.grid(True, **GRID)
    ax.set_axisbelow(True)


def plot_distribution(ax, meta, columns):
    """
    Distribution (from <variable>_dist.csv).

    Worth a second look: the file header says "probability", but the values
    are the same relative frequencies as in <variable>_hist.csv - only placed
    at the interval centres instead of the interval bounds (see Histogram.java,
    csvReport()).

    The cumulative distribution function F(x) = P(X <= x) is obtained by
    summing them up; accumulate() from itertools does exactly that. The curve
    has to reach 1 at the right edge - if it does not, samples fell outside
    the histogram bounds.

    Things to try:
      - add ax.axhline(0.95) and read off the 95% quantile
      - use drawstyle="steps-post" instead of the straight line
      - drop the raw values and show F(x) only
    """
    centers, values = columns
    cumulative = list(accumulate(values))

    ax.plot(centers, values, marker="o", markersize=3, linewidth=1,
            color=COLOR_BARS, label="values from file")
    ax.plot(centers, cumulative, linewidth=2,
            color=COLOR_LINE, label="cumulative sum: F(x)")

    ax.set_title("Distribution")
    ax.set_xlabel(meta["variable"])
    ax.set_ylabel("probability")
    ax.grid(True, **GRID)
    ax.legend(fontsize="small")


# Which plot is built from which CSV suffix.
PLOT_KINDS = [
    ("hist", plot_relative_frequency),
    ("pdf", plot_pdf),
    ("dist", plot_distribution),
]




#################### OUTPUT FILES

def plot_variable(name, files, out_dir, show=False):
    """
    Create three single PNGs and one overview PNG for a single variable.

    Returns the list of files written.
    """
    data = {}
    for kind, _ in PLOT_KINDS:
        path = files.get(kind)
        if path is None:
            print("  {}: file _{}.csv is missing".format(name, kind),
                  file=sys.stderr)
            continue
        meta, columns = read_histogram_csv(path)
        if not has_data(columns):
            print("  {}: _{}.csv holds no measurements (NaN only) - skipped"
                  .format(name, kind), file=sys.stderr)
            continue
        data[kind] = (meta, columns)

    if not data:
        return []

    written = []
    plot_functions = dict(PLOT_KINDS)

    # one file per plot
    for kind, plot_function in PLOT_KINDS:
        if kind not in data:
            continue
        meta, columns = data[kind]
        fig, ax = plt.subplots(figsize=FIGSIZE)
        plot_function(ax, meta, columns)
        target = out_dir / "{}_{}.png".format(name, kind)
        fig.savefig(target, dpi=DPI, bbox_inches="tight")
        if not show:
            plt.close(fig)
        written.append(target)

    # overview: all plots of one variable side by side
    kinds = [kind for kind, _ in PLOT_KINDS if kind in data]
    fig, axes = plt.subplots(1, len(kinds), squeeze=False,
                             figsize=(FIGSIZE[0] * len(kinds) * 0.85, FIGSIZE[1]))
    for ax, kind in zip(axes[0], kinds):
        meta, columns = data[kind]
        plot_functions[kind](ax, meta, columns)
    fig.suptitle(data[kinds[0]][0]["variable"], fontsize="large")
    fig.tight_layout()
    target = out_dir / "{}_overview.png".format(name)
    fig.savefig(target, dpi=DPI, bbox_inches="tight")
    if not show:
        plt.close(fig)
    written.append(target)

    return written


def process_run(run_dir, out_dir, prefix="", show=False):
    """
    Plot every variable of one run.

    Returns (number of variables plotted, number of PNGs written).
    """
    histograms = collect_histograms(run_dir)
    if not histograms:
        print("  no histogram CSVs found", file=sys.stderr)
        return 0, 0

    out_dir.mkdir(parents=True, exist_ok=True)

    variables = 0
    pngs = 0
    for name, files in histograms.items():
        print("  " + name)
        written = plot_variable(prefix + name, files, out_dir, show=show)
        pngs += len(written)
        if written:
            variables += 1
    return variables, pngs


################### Arguments

def main():
    parser = argparse.ArgumentParser(
        description="Plot the histograms of a simulation run as PNG files. "
                    "Instead of a single run you may also pass a folder "
                    "containing several run folders.")
    parser.add_argument("run_dir", type=Path,
                        help="run folder (e.g. 20261004_144325_SimulationStudy) "
                             "or a folder collecting several runs")
    parser.add_argument("--outdir", type=Path, default=None,
                        help="common output folder for all PNGs "
                             "(default: a plots subfolder inside each run)")
    parser.add_argument("--show", action="store_true",
                        help="also display the plots in a window")
    args = parser.parse_args()

    if not args.run_dir.is_dir():
        parser.error("{} is not a folder".format(args.run_dir))

    runs = find_runs(args.run_dir)
    if not runs:
        parser.error("{} contains neither a histograms subfolder nor run "
                     "folders with one".format(args.run_dir))

    multiple = len(runs) > 1
    total_variables = 0
    total_pngs = 0

    for run_dir in runs:
        if multiple:
            print("=== {} ===".format(run_dir.name))

        # Without --outdir every run gets its own plots folder. With --outdir
        # all PNGs share one folder, so the run name is prepended to each file.
        if args.outdir is not None:
            out_dir = args.outdir
            prefix = run_dir.name + "_" if multiple else ""
        else:
            out_dir = run_dir / "plots"
            prefix = ""

        variables, pngs = process_run(run_dir, out_dir, prefix, show=args.show)
        total_variables += variables
        total_pngs += pngs

        if not multiple:
            print("\n{} variables, {} PNGs written to {}"
                  .format(variables, pngs, out_dir))

    if multiple:
        print("\n{} runs, {} variables, {} PNGs written"
              .format(len(runs), total_variables, total_pngs))

    if args.show:
        plt.show()
    return 0


if __name__ == "__main__":
    sys.exit(main())