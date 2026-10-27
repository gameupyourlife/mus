package study;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import simulation.lib.Customer;
import simulation.lib.Simulator;
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
		/*
		 * create simulation object
		 */
		Simulator sim = new Simulator();
		/*
		 * run simulation
		 */
		sim.start();
		/*
		 * print out report
		 */
		sim.report();
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
	private static final double TIME_HISTOGRAM_LOWER = 0.0;
	private static final double TIME_HISTOGRAM_UPPER = 100000.0;
	private static final int QUEUE_HISTOGRAM_LOWER = 0;
	private static final int QUEUE_HISTOGRAM_UPPER = 100;
	private static final int UTILIZATION_HISTOGRAM_LOWER = 0;
	private static final int UTILIZATION_HISTOGRAM_UPPER = 1;

	private DiscreteCounter waitingTimeCounter;
	private DiscreteHistogram waitingTimeHistogram;
	private DiscreteCounter serviceTimeCounter;
	private DiscreteHistogram serviceTimeHistogram;
	private ContinuousCounter queueOccupancyCounter;
	private ContinuousHistogram queueOccupancyHistogram;
	private ContinuousCounter serverUtilizationCounter;
	private ContinuousHistogram serverUtilizationHistogram;

	private Simulator simulator;

	/**
	 * Constructor
	 * @param sim Simulator instance.
	 */
	public SimulationStudy(Simulator sim) {
		simulator = sim;
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
				TIME_HISTOGRAM_LOWER, TIME_HISTOGRAM_UPPER);
		serviceTimeCounter = new DiscreteCounter("serviceTime");
		serviceTimeHistogram = new DiscreteHistogram("serviceTime", NUM_HISTOGRAM_INTERVALS,
				TIME_HISTOGRAM_LOWER, TIME_HISTOGRAM_UPPER);
		queueOccupancyCounter = new ContinuousCounter("queueOccupancy");
		queueOccupancyHistogram = new ContinuousHistogram("queueOccupancy", NUM_HISTOGRAM_INTERVALS,
				QUEUE_HISTOGRAM_LOWER, QUEUE_HISTOGRAM_UPPER);
		serverUtilizationCounter = new ContinuousCounter("serverUtilization");
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
		updateQueueOccupancy(state, simTime);
		updateServerUtilization(state, simTime);
	}

	private void updateServerUtilization(SimulationState state, long simTime) {
		double utilization = state.serverBusy ? 1.0 : 0.0;
		serverUtilizationCounter.count(utilization, simTime);
		serverUtilizationHistogram.count(utilization, simTime);
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

		if (isCsvReport) {
			File file = new File(destination);
			file.mkdir();
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
