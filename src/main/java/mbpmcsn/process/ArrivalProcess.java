package mbpmcsn.process;

import mbpmcsn.center.Center;
import mbpmcsn.desbook.Rngs;
import mbpmcsn.entity.Job;
import mbpmcsn.event.Event;
import mbpmcsn.event.EventQueue;
import mbpmcsn.event.EventType;
import mbpmcsn.process.rvg.RandomVariateGenerator;
import mbpmcsn.routing.NetworkRoutingPoint;

/**
 * generates Inter-Arrival Times (time between two consecutive arrivals)
 * and calculates the new arrival time
 */

public final class ArrivalProcess extends Process {
	private double sarrival;

	public ArrivalProcess(RandomVariateGenerator rvg, Rngs rngs, int streamIdx) {
		super(rvg, rngs, streamIdx);
	}

	public double getArrival() {
		rngs.selectStream(streamIdx);
		sarrival += rvg.generate(rngs);
		return sarrival;
	}

	public void planNextArrival(EventQueue eventQueue, double currentClock, NetworkRoutingPoint entryRouting) {

		// calculate when a pax arrives (update sarrival)
		double nextArrivalTime = getArrival();

		// create the job associated to the pax
		Job newJob = new Job(nextArrivalTime);

		// initial routing: Check-in o Varchi?
		Center firstCenter = entryRouting.getNextCenter(this.rngs, newJob);

		// create event
		Event arrivalEvent = new Event(
				nextArrivalTime,
				EventType.ARRIVAL,
				firstCenter,
				newJob,
				null
		);

		eventQueue.add(arrivalEvent);
	}


}

