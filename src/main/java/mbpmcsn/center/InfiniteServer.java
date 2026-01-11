package mbpmcsn.center;

import mbpmcsn.process.ServiceProcess;
import mbpmcsn.event.Event;
import mbpmcsn.event.EventQueue;
import mbpmcsn.event.EventType;
import mbpmcsn.entity.Job;
import mbpmcsn.routing.NetworkRoutingPoint;
import mbpmcsn.stats.StatCollector;
import mbpmcsn.stats.OnSamplingCallback;

/**
 * represents a Delay Node (no queue)
 * every arriving job is immediately served.
 */

public class InfiniteServer extends Center {
	public InfiniteServer(
			int id, 
			String name, 
			ServiceProcess serviceProcess, 
			NetworkRoutingPoint networkRoutingPoint,
			StatCollector statCollector,
			OnSamplingCallback onSamplingCallback) {

		super(
				id, name, serviceProcess, 
				networkRoutingPoint, statCollector, 
				onSamplingCallback
		);
	}

	@Override
	public void onArrival(Event event, EventQueue eventQueue) {
		numJobsInNode++;

		double now = eventQueue.getCurrentClock();
		collectTimeStats(now);

		double svc = serviceProcess.getService();
		Job job = event.getJob();

		Event departureEvent = new Event(
				now + svc, EventType.DEPARTURE, this, job, null);

		eventQueue.add(departureEvent);
	}

	@Override
	public void onDeparture(Event event, EventQueue eventQueue) {
		numJobsInNode--;

		double now = eventQueue.getCurrentClock();
		collectTimeStats(now);

		Job job = event.getJob();
		Center nextCenter = getNextCenter(job);

		Event arrivalEvent = new Event(
				now, EventType.ARRIVAL, nextCenter, job, null);

		eventQueue.add(arrivalEvent);
	}
	
	@Override
	public Object doSample() {
		return null;
	}
}

