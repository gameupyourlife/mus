package study;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import simulation.lib.Customer;
import simulation.lib.Simulator;
import simulation.lib.counter.DiscreteAutocorrelationCounter;
import simulation.lib.randVars.RandVar;
import simulation.lib.randVars.continous.Exponential;
import simulation.lib.rng.StdRNG;
import simulation.lib.statistic.ContinuousCounterHistogram;
import simulation.lib.statistic.DiscreteCounterHistogram;
import simulation.lib.statistic.StatisticObject;

/**
 * Represents a simulation study. Contains diverse counters for statistics and
 * program/simulator parameters. Starts the simulation.
 */
public class SimulationStudy {
	/*
	 * TODO Problem 4.3.3 - Set the simulation time here!
	 * Mind the interarrival time of 1s to set the simulation time for approx. 10^1 or 10^5 customers
	 * Note: Units are real time units (seconds).
	 * They get converted to simulation time units in setSimulationParameters. And in the simulator class.
	 */

	// Configure program arguments here!!!
	// Note: Units are real time units.
	// Get converted to simulation time units in setSimulationParameters.
	protected long cSimulationTime = 100000;
	/**
	 * inter arrival time of customers (in real time).
	 */
	public double interArrivalTime = 1;

	/**
	 * service time of a customer (in real time).
	 */
	public double serviceTime = 1;

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
	 * Random number generator for inter arrival times.
	 */
	public RandVar randVarInterArrivalTime;

	/**
	 * random number generator for service times
	 */
	public RandVar randVarServiceTime;

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

	// Statistic objects. All of them are contained in statisticObjects.
	/*
	 * TODO Problem 4.3 - instantiate statistic objects
	 */

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
		/*
		 * TODO Problem 4.3.1 - create RVs here
		 *  use the datafields randVarInterArrivalTime and randVarServiceTime
		 */

	}

	/**
	 * Returns the next inter arrival time in real time unit.
	 * Used by the Simulator to queue the next CAE.
	 */
	public double getNextInterArrivalTime() {
		/*
		 * TODO Problem 4.3.1 - return IAT using RV here
		 */
		return interArrivalTime;
	}

	/**
	 * Returns the next service time in real time unit.
	 * Used by the Simulator to queue the next SCE.
	 */
	public double getNextServiceTime() {
		/*
		 * TODO Problem 4.3.1 - return ST using RV here
		 */
		return serviceTime;
	}



	/**
	 * Initializes statistic objects.
	 * Adds them into statisticObjects List for report.
	 */
	private void initStatistics() {
		statisticObjects = new ArrayList<>();

		maxQS = Long.MIN_VALUE;
		minQS = Long.MAX_VALUE;
	}

	/**
	 * Update statistics, fired from customer arrival event.
	 * @param state simulation state
	 * @param simTime current simulation time
	 */
	public void updateOnCustomerArrival(SimulationState state, long simTime) {
		updateQueueSizeBounds(state);
		/*
		 * TODO Problem 4.3  - count statistics here
		 */
	}

	/**
	 * Update statistics, fired from service completion event.
	 * @param state simulation state
	 * @param currentCustomer current customer
	 * @param simTime current simulation time
	 */
	public void updateOnServiceCompletion(SimulationState state, Customer currentCustomer, long simTime) {
		updateQueueSizeBounds(state);
		/*
		 * TODO Problem 4.3  - count statistics here
		 */
	}

	/**
	 * Update customer queue occupancy, fired from customer arrival and service completion event.
	 * @param state simulation state
	 * @param simTime current simulation time
	 */
	public void updateQueueOccupancy(SimulationState state, long simTime) {
		/*
		 * TODO Problem 4.3  - count statistics here
		 */
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
			System.out.println("E[interArrivalTime] = " + interArrivalTime + "\n"
					+ "E[serviceTime] = " + serviceTime + "\n"
					+ "\n" +  "server utilization: "
					+ (serviceTime / interArrivalTime) + "\n");

			System.out.println("minimum queue size: " + minQS + "\n" + "maximum queue size: " + maxQS + "\n");

			/*
			 * TODO Problem 4.3  - report statistics here
			 */
		}

	}
}
