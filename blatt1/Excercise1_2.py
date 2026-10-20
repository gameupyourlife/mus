from collections import Counter
from itertools import product

import matplotlib.pyplot as plt


DICE_PAIRS = list(product(range(1, 7), repeat=2))


def calculate_values(green: int, red: int) -> dict[str, int]:
    return {
        "Xsum": green + red,
        "Xmin": min(green, red),
        "Xmax": max(green, red),
        "Xdiff1": abs(green - red),
        "Xdiff2": green - red,
    }


def distribution(values: list[int]) -> tuple[list[int], list[float]]:
    counts = Counter(values)
    value_range = sorted(counts)
    probabilities = [counts[value] / len(values) for value in value_range]
    return value_range, probabilities


def expectation(values: list[int]) -> float:
    return sum(values) / len(values)


def variance(values: list[int]) -> float:
    mean = expectation(values)
    return sum((value - mean) ** 2 for value in values) / len(values)


all_values = {
    name: [
        calculate_values(green, red)[name]
        for green, red in DICE_PAIRS
    ]
    for name in ("Xsum", "Xmin", "Xmax", "Xdiff1", "Xdiff2")
}

fig, axes = plt.subplots(
    len(all_values),
    2,
    figsize=(14, 20),
    layout="constrained",
)

for row, (name, values) in enumerate(all_values.items()):
    value_range, probabilities = distribution(values)
    cumulative_probabilities = []
    cumulative = 0
    for probability in probabilities:
        cumulative += probability
        cumulative_probabilities.append(cumulative)

    probability_axis, cumulative_axis = axes[row]

    probability_axis.bar(value_range, probabilities, width=0.8)
    probability_axis.set_title(
        f"{name}: Wahrscheinlichkeitsverteilung",
        pad=8,
    )
    probability_axis.set_xlabel("Wert", labelpad=8)
    probability_axis.set_ylabel("P(X = x)")
    probability_axis.set_xticks(value_range)
    probability_axis.set_ylim(0, max(probabilities) * 1.2)
    probability_axis.grid(axis="y", alpha=0.3)

    cumulative_axis.step(
        value_range,
        cumulative_probabilities,
        where="post",
        marker="o",
    )
    cumulative_axis.set_title(
        f"{name}: Verteilungsfunktion",
        pad=8,
    )
    cumulative_axis.set_xlabel("x", labelpad=8)
    cumulative_axis.set_ylabel("F(x) = P(X <= x)")
    cumulative_axis.set_xticks(value_range)
    cumulative_axis.set_ylim(0, 1.1)
    cumulative_axis.grid(True, alpha=0.3)

plt.show()