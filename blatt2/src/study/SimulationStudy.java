package study;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import simulation.lib.Customer;
import simulation.lib.Simulator;
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

	/*
	 * TODO Problem 2.2 - declaring your statistic objects
	 * Add the statistic objects as datafields here.
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

		/*
          TODO Problem 2.2 - initialize statistic objects and add them to the statisticObjects List
         */
	}

	/**
	 * Update statistics, fired from customer arrival event.
	 * @param state simulation state
	 * @param simTime current simulation time
	 */
	public void updateOnCustomerArrival(SimulationState state, long simTime) {
		updateQueueSizeBounds(state);

		/*
		 * TODO Problem 2.2.3 - customer arrival event: update server utilization here
		 * In a CustomerArrivalEvent, this method is called from the simulator.
		 * All necessary information is given in the simulation state.
		 * Update/Count the statistic objects needed.
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
		 * TODO Problem 2.2.2 & 2.2.3 - update waiting/service time and server utilization measurement objects here
		 * Use your respective counter and histogram to count the service time of the currentCustomer (getTimeInService())
		 * and its waiting time (getTimeInQueue())
		 */
	}

	/**
	 * Update customer queue occupancy, fired from customer arrival and service completion event.
	 * @param state simulation state
	 * @param simTime current simulation time
	 */
	public void updateQueueOccupancy(SimulationState state, long simTime) {
		/*
		 * TODO Problem 2.2.3 - update queue occupancy here
		 * Count the queue size with your respective statistic objects
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
