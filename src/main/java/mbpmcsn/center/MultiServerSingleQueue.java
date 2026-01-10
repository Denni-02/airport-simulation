package mbpmcsn.center;

import java.util.Queue;
import java.util.LinkedList;

import mbpmcsn.process.ServiceProcess;
import mbpmcsn.event.Event;
import mbpmcsn.event.EventQueue;
import mbpmcsn.event.EventType;
import mbpmcsn.entity.Job;

/**
 * m parallel servers and a single FIFO queue
 * jobs wait in the queue only if all m servers are busy
 */

public class MultiServerSingleQueue extends Center {
	private final int numServers;

	/* maybe this is not needed but makes it more clear */
	private int numActiveServers;

	private Queue<Job> jobQueue = new LinkedList<>();

	public MultiServerSingleQueue(
			int id, 
			String name, 
			ServiceProcess serviceProcess, 
			NetworkRoutingPoint networkRoutingPoint, 
			int numServers) {

		super(id, name, serviceProcess, networkRoutingPoint);
		this.numServers = numServers;
	}

	@Override
	public void onArrival(Event event, EventQueue eventQueue) {
		numJobsInNode++;

		double now = eventQueue.getCurrentClock();
		collectTimeStats(now);

		Job job = event.getJob();

		if(numActiveServers == numServers) {
			jobQueue.add(job);
			return;
		}

		numActiveServers++;

		scheduleDepartureEvent(now, job, eventQueue);
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

		if(jobQueue.isEmpty()) {
			numActiveServers--;
			return;
		}

		job = jobQueue.remove();

		scheduleDepartureEvent(now, job, eventQueue);
	}

	@Override
	public void onSampling(Event event, EventQueue eventQueue) {

	}

	private void scheduleDepartureEvent(
			double now, Job job, EventQueue eventQueue) {

		double svc = serviceProcess.getService();

		Event departureEvent = new Event(
				now + svc, EventType.DEPARTURE, this, job, null);

		eventQueue.add(departureEvent);
	}
}

