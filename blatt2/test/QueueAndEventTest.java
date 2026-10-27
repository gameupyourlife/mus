package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import simulation.lib.Customer;
import simulation.lib.Simulator;
import simulation.lib.SortableQueue;
import simulation.lib.event.CustomerArrivalEvent;
import simulation.lib.event.Event;
import simulation.lib.event.ServiceCompletionEvent;
import simulation.lib.event.SimulationTerminationEvent;
import study.SimulationState;

class QueueAndEventTest {

    @Test
    void queueReturnsCustomersInArrivalOrder() {
        SortableQueue queue = new SortableQueue();
        Customer late = new Customer(20);
        Customer early = new Customer(5);
        Customer middle = new Customer(10);

        queue.pushNewElement(late);
        queue.pushNewElement(early);
        queue.pushNewElement(middle);

        assertEquals(early, queue.popNextElement());
        assertEquals(middle, queue.popNextElement());
        assertEquals(late, queue.popNextElement());
        assertEquals(0, queue.size());
        assertNull(queue.popNextElement());
    }

    @Test
    void customerTimesAreDerivedFromRecordedTimestamps() {
        Customer customer = new Customer(10);
        customer.serviceStartTime = 14;
        customer.serviceEndTime = 23;

        assertEquals(4, customer.getTimeInQueue());
        assertEquals(9, customer.getTimeInService());
    }

    @Test
    void arrivalAndCompletionEventsUpdateSimulationState() {
        SimulationState state = new SimulationState(null);
        CustomerArrivalEvent arrival = new CustomerArrivalEvent(state, 10);
        arrival.process();

        assertTrue(state.serverBusy);
        assertEquals(10, state.customerInService.arrivalTime);
        assertEquals(10, state.customerInService.serviceStartTime);
        assertEquals(0, state.queueSize);

        ServiceCompletionEvent completion = new ServiceCompletionEvent(state, 19);
        completion.process();

        assertFalse(state.serverBusy);
        assertNull(state.customerInService);
        assertEquals(1, state.numSamples);
    }

    @Test
    void eventsAreOrderedByEventTime() {
        Event early = new SimulationTerminationEvent(2);
        Event late = new SimulationTerminationEvent(8);

        assertTrue(early.compareTo(late) < 0);
        assertTrue(late.compareTo(early) > 0);
        assertEquals(0, early.compareTo(new SimulationTerminationEvent(2)));
    }

    @Test
    void simulatorConvertsBetweenRealAndSimulationTime() throws Exception {
        Simulator simulator = new Simulator();
        simulator.setSimTimeInRealTime(10);

        assertEquals(25, simulator.realTimeToSimTime(2.5));
        assertEquals(26, simulator.realTimeToSimTime(2.51));
        assertEquals(2.5, simulator.simTimeToRealTime(25), 1e-12);
    }
}
