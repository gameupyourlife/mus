package simulation.lib.statistic;

/**
 * Superclass for statistic objects such as Counters and Histograms.
 */
public abstract class StatisticObject {
	/**
	 * Name of variable, which the statistic object observes.
	 */
	protected String observedVariable;

	/**
	 * Type of the statistics object (default set in constructor is the class name).
	 */
	protected String statisticsType;

	/**
	 * Number of samples counted.
	 */
	protected long numSamples;

	public StatisticObject(String observedVariable) {
		this.observedVariable = observedVariable;
		this.statisticsType = this.getClass().getSimpleName();
	}

	/**
	 * Count sample.
	 * @param value value to be added to the statistic
	 * @param time timestamp only used for continuous statistic objects
	 */
	public void count(double value, long time) {
		numSamples++;
	}


	/**
	 * Report via String.
	 * @return Reportstring
	 */
	public String report() {
		String out = "";
		if(observedVariable!= null) out += "observed random variable: " + observedVariable + "\n";
		out +=	"\t" + statisticsType + "\n" +
				"\tnumber of samples: " + numSamples + "\n";
		return out;
	}
	
	/**
	 * Report to csv-file.
	 * @param outputdir filepath
	 */
	public abstract void csvReport(String outputdir);
}
