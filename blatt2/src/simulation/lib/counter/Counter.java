package simulation.lib.counter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import simulation.lib.statistic.StatisticObject;

/**
 * Basic counter that counts:
 * * sum power two
 * * sum power one
 * * minimum
 * * maximum
 */
public abstract class Counter extends StatisticObject{
	/**
	 * Sum of values counted by this counter
	 */
	protected double sumPowerOne;
	/**
	 * Sum of square values counted by this counter
	 */
	protected double sumPowerTwo;
	private double min;
	private double max;

	/**
	 * Internal constructor
	 * @param variable the observed variable
	 * @param type the type of counter
	 */
	public Counter(String variable) {
		super(variable);
		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;
	}
	
	/**
	 * Returns the mean of the observed variable
	 * @return the mean
	 */
	public abstract double getMean();
	
	/**
	 * Returns the variance of the observed variable
	 * @return the variance
	 */
	public abstract double getVariance();
	
	/**
	 * Returns the standard deviation of the observed variable
	 * @return the standard deviation
	 */
	public double getStdDeviation() {
		/**
		 * TODO Problem 2.1.1 - getStdDeviation
		 * You can use other methods of this class in order to calculate the standard deviation
		 * Hint: See course syllabus 1.3.4
		 */
		return 0;
	}
	
	/**
	 * Returns the co-variance of the observed variable 
	 * @return the co-variance
	 */
	public double getCvar() {
		if(getMean() == 0)
			return getStdDeviation()== 0 ? 0 : Double.MAX_VALUE;
		else
			return getStdDeviation() / getMean();
	}
	
	/**
	 * Returns the minimum of the observed variable
	 * @return the minimum
	 */
	public double getMin() {
		return min;
	}
	
	/**
	 * Returns the maximum of the observed variable
	 * @return the maximum
	 */
	public double getMax() {
		return max;
	}
	
	/**
	 * Returns the number of counted samples
	 * @return the number of samples
	 */
	public long getNumSamples() {
		return numSamples;
	}
	
	/**
	 * Returns the sum of all counted samples
	 * @return the sum of all samples
	 */
	public double getSumPowerOne() {
		return sumPowerOne;
	}
	
	/**
	 * Adds the given value to the sum of counted samples
	 * @param value the value to add
	 */
	public void increaseSumPowerOne(double value) {
		/**
		 * TODO Problem 2.1.1 - increaseSumPowerOne
		 * Implement this function!
		 * This method updates internal data "sumPowerOne" which is needed for the calculation of empirical moments
		 * Hint: DiscreteCounter and ContinuousCounter pass different values to this method
		 */
	}
	
	/**
	 * Returns the sum of all counted samples power two
	 * @return the sum of all samples power two
	 */
	public double getSumPowerTwo() {
		return sumPowerTwo;
	}
	
	/**
	 * Adds the given value to the sum of counted samples power two
	 * @param value the value to add
	 */
	public void increaseSumPowerTwo(double value) {
		/**
		 * TODO Problem 2.1.1 - increaseSumPowerTwo
		 * Implement this function!
		 * This method updates internal data "sumPowerTwo" which is needed for the calculation of empirical moments
		 * Hint: DiscreteCounter and ContinuousCounter pass different values to this method
		 */
	}
	
	/**
	 * Counts a new sample (set min/max and increment sample counter)
	 * @param v the value to count
	 * @param t timestamp for continuous statistic objects
	 */
	@Override
	public void count(double v, long t) {
		/**
		 * TODO Problem 2.1.1 - count
		 * Implement this function!
		 * Hint: Take a look at the javadoc comment of this method.
		 * The timestamp t is only used in the count method of the Continuous Counter.
		 */
	}
	
	/**
	 * Outputs the report of this counter to the command line
	 */
	public String report() {
		String out = super.report();
		out +=	"\tmean: " + getMean() + "\n" +
				"\tvariance: " + getVariance() + "\n" +
				"\tstandard deviation: " + getStdDeviation() + "\n" +
				"\tcoefficient of variation: " + getCvar() + "\n" +
				"\tminimum: " + min + "\n" +
				"\tmaximum: " + max;
		return out;
	}
	
	/**
	 * Write Counter details to csv-file.
	 */
	public void csvReport(String outputdir){
		String content = observedVariable + ";" + numSamples + ";" + getMean() + ";" + getVariance()  + ";" + getStdDeviation() + ";" + getCvar() + ";" + getMin() + ";" + getMax() + "\n";
		String labels = "#" + statisticsType + "\n" + "#variable ; numSamples ; MEAN; VAR; STD; CVAR; MIN; MAX\n";
		writeCsv(outputdir, content, labels);
	}

	protected void writeCsv(String outputdir, String content, String labels) {
		try {
			String dest= outputdir + "/counters";
			File file = new File(dest);
			file.mkdir();

			boolean fexist = new File(dest + "/" + this.getClass().getSimpleName() + ".csv").exists();
			FileWriter csvwriter = new FileWriter(dest + "/" + this.getClass().getSimpleName()  + ".csv",true);

			if(!fexist){
				content = labels + content;
			}

			csvwriter.append(content);
			csvwriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Resets the counter.
	 */
	public void reset(long time) {
		sumPowerOne = 0;
		sumPowerTwo = 0;
		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;
		numSamples = 0;
	}
}
