package study;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import simulation.lib.Customer;
import simulation.lib.Simulator;
import simulation.lib.counter.Counter;
import simulation.lib.counter.ContinuousCounter;
import simulation.lib.counter.DiscreteCounter;
import simulation.lib.histogram.ContinuousHistogram;
import simulation.lib.histogram.DiscreteHistogram;
import simulation.lib.statistic.StatisticObject;

/**
 * Represents a simulation study. Contains diverse counters for statistics and
 * program/simulator parameters. Starts the simulation.
 */
public class SimulationStudy {
	/*
	 * TODO Problem 2.2.4 - configure program arguments here
	 * Here you can set the different parameters for your simulation
	 * Note: Units are real time units (seconds).
	 * They get converted to simulation time units in setSimulationParameters.
	 */
	protected long cInterArrivalTime = 10;
	protected long cServiceTime = 9;
	protected long cSimulationTime = 100000;

	/**
	 * Name of the simulation run. Included in output folder name.
	 * Example: "rho075_cVar2"
	 */
	protected String simName = "";


	/**
	 * Main method
	 * 
	 * @param args
	 *            - none
	 */
	public static void main(String[] args) {
		Simulator simulator = new Simulator();
		simulator.start();
		simulator.getStudy().printMetrics();
		simulator.report();
	}

	// PARAMETERS
	/**
	 * Turn on/off debug report in console.
	 */
	protected boolean isDebugReport = true;

	/**
	 * Turn on/off report in csv-files.
	 */
	protected boolean isCsvReport = true;

	/**
	 * Simulation time.
	 */
	public long simulationTime;

	/**
	 * inter arrival time of customers (in simulation time).
	 */
	public long interArrivalTime;

	/**
	 * service time of a customer (in simulation time).
	 */
	public long serviceTime;

	// STATISTICS
	/**
	 * List that contains all statistical relevant object such as counters and
	 * histograms.
	 */
	public List<StatisticObject> statisticObjects;

	/**
	 * Maximum queue size.
	 */
	public long maxQS;

	/**
	 * Minimum queue size.
	 */
	public long minQS;

	private static final int NUM_HISTOGRAM_INTERVALS = 50;
	private static final int QUEUE_HISTOGRAM_LOWER = 0;
	private static final int UTILIZATION_HISTOGRAM_LOWER = 0;
	private static final int UTILIZATION_HISTOGRAM_UPPER = 1;

	private DiscreteCounter waitingTimeCounter;
	private DiscreteHistogram waitingTimeHistogram;
	private DiscreteCounter serviceTimeCounter;
	private DiscreteHistogram serviceTimeHistogram;
	private Counter queueOccupancyCounter;
	private ContinuousHistogram queueOccupancyHistogram;
	private Counter serverUtilizationCounter;
	private ContinuousHistogram serverUtilizationHistogram;
	private boolean discreteStateStatistics;

	private Simulator simulator;

	/**
	 * Constructor
	 * @param sim Simulator instance.
	 */
	public SimulationStudy(Simulator sim) {
		this(sim, 9, 100000);
	}

	/**
	 * Constructor with configurable experiment parameters.
	 * @param sim simulator instance
	 * @param serviceTime service time in real-time seconds
	 * @param simulationTime total simulation time in real-time seconds
	 */
	public SimulationStudy(Simulator sim, long serviceTime, long simulationTime) {
		this(sim, serviceTime, simulationTime, false);
	}

	/**
	 * Constructor with configurable state-statistics sampling.
	 * @param sim simulator instance
	 * @param serviceTime service time in real-time seconds
	 * @param simulationTime total simulation time in real-time seconds
	 * @param discreteStateStatistics use event-based counters for queue and utilization
	 */
	public SimulationStudy(Simulator sim, long serviceTime, long simulationTime,
			boolean discreteStateStatistics) {
		simulator = sim;
		cServiceTime = serviceTime;
		cSimulationTime = simulationTime;
		this.discreteStateStatistics = discreteStateStatistics;
		simulator.setSimTimeInRealTime(1000);
		setSimulationParameters();
		initStatistics();
	}

	/**
	 * Sets simulation parameters, converts real time to simulation time if
	 * needed.
	 */
	private void setSimulationParameters() {
		simulationTime = simulator.realTimeToSimTime(cSimulationTime);
		interArrivalTime = simulator.realTimeToSimTime(cInterArrivalTime);
		serviceTime = simulator.realTimeToSimTime(cServiceTime);
	}

	/**
	 * Initializes statistic objects.
	 * Adds them into statisticObjects List for report.
	 */
	private void initStatistics() {
		statisticObjects = new ArrayList<>();

		maxQS = Long.MIN_VALUE;
		minQS = Long.MAX_VALUE;

		waitingTimeCounter = new DiscreteCounter("waitingTime");
		waitingTimeHistogram = new DiscreteHistogram("waitingTime", NUM_HISTOGRAM_INTERVALS,
				0.0, simulator.realTimeToSimTime(expectedWaitingTimeUpperBound()));
		serviceTimeCounter = new DiscreteCounter("serviceTime");
		serviceTimeHistogram = new DiscreteHistogram("serviceTime", NUM_HISTOGRAM_INTERVALS,
				0.0, simulator.realTimeToSimTime(Math.max(cServiceTime + 1, 20)));
		queueOccupancyCounter = discreteStateStatistics
				? new DiscreteCounter("queueOccupancy")
				: new ContinuousCounter("queueOccupancy");
		queueOccupancyHistogram = new ContinuousHistogram("queueOccupancy", NUM_HISTOGRAM_INTERVALS,
				QUEUE_HISTOGRAM_LOWER, (int) Math.max(100, simulationTime / interArrivalTime + 1));
		serverUtilizationCounter = discreteStateStatistics
				? new DiscreteCounter("serverUtilization")
				: new ContinuousCounter("serverUtilization");
		serverUtilizationHistogram = new ContinuousHistogram("serverUtilization", NUM_HISTOGRAM_INTERVALS,
				UTILIZATION_HISTOGRAM_LOWER, UTILIZATION_HISTOGRAM_UPPER);

		statisticObjects.add(waitingTimeCounter);
		statisticObjects.add(waitingTimeHistogram);
		statisticObjects.add(serviceTimeCounter);
		statisticObjects.add(serviceTimeHistogram);
		statisticObjects.add(queueOccupancyCounter);
		statisticObjects.add(queueOccupancyHistogram);
		statisticObjects.add(serverUtilizationCounter);
		statisticObjects.add(serverUtilizationHistogram);
	}

	private double expectedWaitingTimeUpperBound() {
		if (cServiceTime <= cInterArrivalTime) {
			return Math.max(cServiceTime, cInterArrivalTime);
		}
		return cSimulationTime * (cServiceTime - cInterArrivalTime)
				/ (double) cInterArrivalTime + cServiceTime;
	}

	/**
	 * Update statistics, fired from customer arrival event.
	 * @param state simulation state
	 * @param simTime current simulation time
	 */
	public void updateOnCustomerArrival(SimulationState state, long simTime) {
		updateQueueSizeBounds(state);
		updateServerUtilization(state, simTime);
	}

	/**
	 * Update statistics, fired from service completion event.
	 * @param state simulation state
	 * @param currentCustomer current customer
	 * @param simTime current simulation time
	 */
	public void updateOnServiceCompletion(SimulationState state, Customer currentCustomer, long simTime) {
		updateQueueSizeBounds(state);

		long waitingTime = currentCustomer.getTimeInQueue();
		long serviceTime = currentCustomer.getTimeInService();
		waitingTimeCounter.count(waitingTime);
		waitingTimeHistogram.count(waitingTime);
		serviceTimeCounter.count(serviceTime);
		serviceTimeHistogram.count(serviceTime);
		updateServerUtilization(state, simTime);
	}

	/**
	 * Update customer queue occupancy, fired from customer arrival and service completion event.
	 * @param state simulation state
	 * @param simTime current simulation time
	 */
	public void updateQueueOccupancy(SimulationState state, long simTime) {
		queueOccupancyCounter.count(state.queueSize, simTime);
		queueOccupancyHistogram.count(state.queueSize, simTime);
	}

	/**
	 * Closes the final continuous-statistic interval at simulation termination.
	 * @param state current simulation state
	 * @param simTime termination time
	 */
	public void updateOnSimulationTermination(SimulationState state, long simTime) {
		if (!discreteStateStatistics) {
			updateQueueOccupancy(state, simTime);
			updateServerUtilization(state, simTime);
		}
	}

	private void updateServerUtilization(SimulationState state, long simTime) {
		double utilization = state.serverBusy ? 1.0 : 0.0;
		serverUtilizationCounter.count(utilization, simTime);
		serverUtilizationHistogram.count(utilization, simTime);
	}

	/**
	 * Prints the requested mean and coefficient-of-variation metrics.
	 */
	public void printMetrics() {
		printMetric("waiting time per customer", waitingTimeCounter, 0.001);
		printMetric("service time per customer", serviceTimeCounter, 0.001);
		printMetric("queue occupancy over time", queueOccupancyCounter, 1.0);
		printMetric("server utilization over time", serverUtilizationCounter, 1.0);
	}

	private void printMetric(String name, Counter counter, double meanScale) {
		System.out.printf("  %-32s mean=%12.6f, cvar=%12.6f%n",
				name, counter.getMean() * meanScale, counter.getCvar());
	}

	/**
	 * Updates minimum and maximum queue size.
	 * @param state simulation state
	 */
	private void updateQueueSizeBounds(SimulationState state) {
		minQS = state.queueSize < minQS ? state.queueSize : minQS;
		maxQS = state.queueSize > maxQS ? state.queueSize : maxQS;
	}

	/**
	 * Report results. Print to console if isDebugReport = true. Print to csv
	 * files if isCsvReport = true. Note: Histogramms are only printed to csv
	 * files.
	 */
	public void report() {
		String sd = new SimpleDateFormat("yyyyMMdd_HHmmss_").format(new Date(System.currentTimeMillis()));
		String destination = sd + this.getClass().getSimpleName() + (simName.isEmpty() ? "" : "_" + simName);
		report(destination);
	}

	/**
	 * Writes this study's statistic objects to a specified directory.
	 * @param destination output directory
	 */
	public void report(String destination) {

		if (isCsvReport) {
			File file = new File(destination);
			file.mkdirs();
			for (StatisticObject so : statisticObjects) {
				so.csvReport(destination);
			}
		}
		if (isDebugReport) {
			System.out.println("E[interArrivalTime] = " + simulator.simTimeToRealTime(interArrivalTime) + "\n"
					+ "E[serviceTime] = " + simulator.simTimeToRealTime(serviceTime) +  "\n" + "server utilization: "
					+ ((double) serviceTime / (double) interArrivalTime) + "\n");

			/*
			 * TODO Problem 2.2.2 & 2.2.3 - print statistic objects
			 * If you want to see the output of the statistic objects directly, you can print them here in the debug report.
			 */

			System.out.println("minimum queue size: " + minQS + "\n" + "maximum queue size: " + maxQS);
		}

	}
}
