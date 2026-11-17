package simulation.lib;

import java.util.function.Function;

import simulation.lib.event.CustomerArrivalEvent;
import simulation.lib.event.Event;
import simulation.lib.event.ServiceCompletionEvent;
import simulation.lib.randVars.RandVar;
import study.SimulationState;
import study.SimulationStudy;

/**
 * Main Simulator class for the discrete event simulation
 */
public class Simulator {
	private long simTimeInRealTime;
	private long now;
	private SortableQueue ec;
	private boolean stop;
	
	/**
	 * Contains simulator statistics and parameters
	 */
	private SimulationStudy sims;
	
	/**
	 * Contains simulator state.
	 */
	private SimulationState state;
	
	/**
	 * Constructor 
	 * Create event chain (ec), SimulationStudy- and SimulationState- objects
	 * Pushes first event (customer arrival event) to queue
	 */
	public Simulator() {
		this(SimulationStudy::new);
	}

	/** Creates a study with the given warm-up, batch length, and independent time generators. */
	public Simulator(long ninit, int lbatch, RandVar arrivals, RandVar service) {
		this(simulator -> new SimulationStudy(simulator, ninit, lbatch, arrivals, service));
	}

	private Simulator(Function<Simulator, SimulationStudy> createStudy) {
		// create event chain
		ec = new SortableQueue();
		sims = createStudy.apply(this);
		state = new SimulationState(sims);
		// push the first customer arrival at t = 0
		pushNewEvent(new CustomerArrivalEvent(state, 0));
	}
	
	/**
	 * Sets the number of ticks in simulation time per unit in real time
	 * @param simTimeInRealTime number of ticks per unit in real time
	 */
	public void setSimTimeInRealTime(long simTimeInRealTime) {
		this.simTimeInRealTime = simTimeInRealTime;
	}
	
	/**
	 * Converts real time to sim time
	 * @param realTime units in real time
	 * @return units in sim time
	 * @throws Exception if sim time is out of range
	 */
	public long realTimeToSimTime(double realTime) throws NumberFormatException {
		double tmp = realTime * simTimeInRealTime;
		if(tmp > Long.MAX_VALUE)
			throw new NumberFormatException("simulation time out of range: " + tmp + " > " + Long.MAX_VALUE);
		return (long) Math.ceil(tmp);
	}
	
	/**
	 * Converts sim time to real time
	 * @param simTime units in sim time
	 * @return units in real time
	 */
	public double simTimeToRealTime(long simTime) {
		return (double) simTime / simTimeInRealTime;
	}
	
	/**
	 * Starts the simulation
	 * @throws Exception is thrown when event order is invalid
	 */
	private void run() {
		while(!stop) {
			Event e = (Event) ec.popNextElement();
			if(e != null) {
				//check if event time is in the past
				if(e.getTime() < now) {
					throw new RuntimeException("Event time " + e.getTime()
							+ " smaller than current time " + now);
				}

				//set event time as new simtime
				now = e.getTime();
				
				//register for notification
				e.register(this);

				// process event
				e.process();

				//unregister notifications
				e.unregister(this);
			} else {
				System.out.println("Event chain empty.");
				stop = true;
			}
		}
	}
	
	/**
	 * Pushes a new event into the event chain at the correct place
	 * @param e the new event
	 */
	private void pushNewEvent(Event e) {
		ec.pushNewElement(e);
	}
	
	/**
	 * Stops the simulator
	 */
	private void stop() {
		stop = true;
	}

    /**
     * Starts the simulation.
     */
	public void start() {
		stop = false;
		run();
	}
	
	/**
	 * Returns the current sim time
	 * @return current sim time
	 */
	public long getSimTime() {
	    return now;
	}
	
	/**
	 * Resets the simulator
	 */
	public void reset() {
		now = 0;
		stop = false;
		ec.clear();
	}
	
	/**
	 * Reports results.
	 */
	public void report() {
	    sims.report();
	}
	

	/**
	 * Event notification for updating simulation statistics.
	 * @param sender CustomerArrivalEvent, ServiceCompletionEvent
	 * @param arg current customer or null if CustomerArrivalEvent is sender
	 */
	public void updateStatisticsHandler(Object sender, Object arg) {
		if (sender.getClass() == CustomerArrivalEvent.class) {
			sims.updateOnCustomerArrival(state, getSimTime());
		}else if(sender.getClass() == ServiceCompletionEvent.class){
			sims.updateOnServiceCompletion(state, (Customer) arg, getSimTime());
			if (sims.isSimulationFinished()) {
				stop();
			}
		}
	}
	
	/**
	 * Event notification for updating customer queue occupancy
	 * @param sender CAE, SCE
	 */
	public void updateQueueOccupancyHandler(Object sender) {
		sims.updateQueueOccupancy(state, getSimTime());
	}

	/**
	 * Event notification for pushing new event into event chain.
	 * 	@param c type of new event that should be pushed CAE, SCE
	 */
	public void pushNewEventHandler(Class<?> c) {
		if (c == CustomerArrivalEvent.class) {
			pushNewEvent(new CustomerArrivalEvent(state, this.getSimTime() + realTimeToSimTime(sims.randVarInterArrivalTime.getRV())));
		}else if(c == ServiceCompletionEvent.class){
			pushNewEvent(new ServiceCompletionEvent(state, this.getSimTime() + realTimeToSimTime(sims.randVarServiceTime.getRV())));	
		}
	}

	/**
	 * Event notification for stopping the simulation
	 * @param sender SimulationTerminationEvent
	 */
	public void stopEventHandler(Object sender) {
		stop();
	}
}
