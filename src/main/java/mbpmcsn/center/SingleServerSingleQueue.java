package mbpmcsn.center;

import java.util.Queue;
import java.util.LinkedList;

import mbpmcsn.process.ServiceProcess;
import mbpmcsn.event.Event;
import mbpmcsn.event.EventQueue;
import mbpmcsn.event.EventType;
import mbpmcsn.entity.Job;

/**
 * 1 server and a single FIFO queue
 */

public class SingleServerSingleQueue extends Center {
	private boolean activeServer;
	private Queue<Job> jobQueue = new LinkedList<>();

	public SingleServerSingleQueue(
			int id, 
			String name, 
			ServiceProcess serviceProcess, 
			NetworkRoutingPoint networkRoutingPoint) {

		super(id, name, serviceProcess, networkRoutingPoint);
	}

	@Override
	public void onArrival(Event event, EventQueue eventQueue) {
		numJobsInNode++;

		double now = eventQueue.getCurrentClock();
		collectTimeStats(now);

		Job job = event.getJob();
		
		if(activeServer) {
			jobQueue.add(job);
			return;
		}

		activeServer = true;

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
			activeServer = false;
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

