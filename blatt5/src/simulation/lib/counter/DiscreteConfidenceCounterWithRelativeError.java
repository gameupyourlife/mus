package simulation.lib.counter;

/**
 * This class implements a discrete time confidence counter with relativ error.
 */
public class DiscreteConfidenceCounterWithRelativeError extends DiscreteConfidenceCounter {
	
	/**
	 * Basic constructor
	 * @param variable the variable to observe
	 */
	public DiscreteConfidenceCounterWithRelativeError(String variable) {
		this(variable, 0.05);
	}
	
	/**
	 * Constructor
	 * @param variable observed variable
	 * @param alpha level of significance
	 */
	public DiscreteConfidenceCounterWithRelativeError(String variable, double alpha) {
		super(variable, alpha);
	}
	
	/**
	 * Calculates the maximal relative error of the confidence counter.
	 * @return maximal relative error
	 */
	public double maxRelErr() {
		if (getNumSamples() < 2) {
			return Double.POSITIVE_INFINITY;
		}
		double bound = getBound();
		double closestToZero = Math.abs(getMean()) - bound;
		return closestToZero > 0 ? bound / closestToZero : Double.POSITIVE_INFINITY;
	}

	/**
	 * Calculates the maximal absolute error of the confidence counter.
	 * @return maximal absolute error
	 */
	public double maxAbsErr() {
        return getBound();
	}

	/**
	 * @see Counter#report()
	 */
	@Override
	public String report() {
		String out  = super.report();
		out += ("\n\tmaximum relative error = " + maxRelErr() + "\n"
				+ "\tmaximum absolute error = " + maxAbsErr() + "\n");
		return out;
	}
	
	/** Adds the error columns to the confidence counter's CSV row. */
	@Override
	protected void writeCsv(String outputdir, String content, String labels) {
		// Blatt 3's confidence counter also calls this method, with semicolon-separated fields.
		if (labels.startsWith("#")) {
			String values = content.substring(String.valueOf(observedVariable).length());
			content = csvVariable() + values.replace(';', ',');
			labels = labels.substring(1).replace(" ", "").replace(';', ',');
		}
		super.writeCsv(outputdir,
				content.stripTrailing() + "," + maxRelErr() + "," + maxAbsErr() + "\n",
				labels.stripTrailing() + ",maxRelErr,maxAbsErr\n");
	}
}
