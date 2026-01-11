package mbpmcsn.center;

import java.util.List;
import java.util.ArrayList;
import java.util.Queue;

import mbpmcsn.process.ServiceProcess;
import mbpmcsn.event.Event;
import mbpmcsn.event.EventQueue;
import mbpmcsn.event.EventType;
import mbpmcsn.entity.Job;
import mbpmcsn.flowpolicy.FlowAssignmentPolicy;
import mbpmcsn.routing.NetworkRoutingPoint;
import mbpmcsn.stats.StatCollector;
import mbpmcsn.stats.OnSamplingCallback;

/**
 * m server, where each server has its own dedicated queue
 * arriving jobs must choose which queue to join based on a specific policy
 * (es. Shortest Queue, Random, or Round Robin)
 */

public class MultiServerMultiQueue extends Center {
	/* let the client to decide to which sssq to redirect the job */
	private final FlowAssignmentPolicy flowAssignmentPolicy;

	/* should have fixed number of elements, numFlows */
	private final List<SssqStatus> sssqStatus = new ArrayList<>();

	public MultiServerMultiQueue(
			int id, 
			String name, 
			ServiceProcess serviceProcess, 
			NetworkRoutingPoint networkRoutingPoint,
			StatCollector statCollector,
			OnSamplingCallback onSamplingCallback,
			int numFlows,
			FlowAssignmentPolicy flowAssignmentPolicy) {

		super(
				id, 
				name, 
				serviceProcess, 
				networkRoutingPoint, 
				statCollector,
				onSamplingCallback
		);

		this.flowAssignmentPolicy = flowAssignmentPolicy;

		for(int i = 0; i < numFlows; i++) {
			sssqStatus.add(new SssqStatus());
		}
	}

	@Override
	public void onArrival(Event event, EventQueue eventQueue) {
		/* whole center */
		numJobsInNode++;

		double now = eventQueue.getCurrentClock();
		collectTimeStats(now);

		/* choose the flow based on client-provided policy */
		int flowIdx = flowAssignmentPolicy.assignFlow(sssqStatus);

		/* get the particular sssq associated to the flow */
		SssqStatus sssq = sssqStatus.get(flowIdx);
		boolean activeServer = sssq.hasActiveServer();
		Queue<Job> jobQueue = sssq.getQueue();

		Job job = event.getJob();

		/* checking for particular sssq associated to the flow */
		if(activeServer) {
			/* add to particular sssq associated to the flow */
			jobQueue.add(job);
			return;
		}

		sssq.setActiveServer(true);

		scheduleDepartureEvent(now, job, sssq, eventQueue);
	}

	@Override
	public void onDeparture(Event event, EventQueue eventQueue) {
		/* whole center */
		numJobsInNode--;

		double now = eventQueue.getCurrentClock();
		collectTimeStats(now);

		/* determine the sssq that generated the departure event */
		SssqStatus sssq = (SssqStatus) event.getArgs();

		/* get the associated job */
		Job job = event.getJob();

		/* whole-center exiting job routing applied */
		Center nextCenter = getNextCenter(job);

		/* generate arrival event for next center, as usual.
		 * we don't care anymore about sssq within this center */
		Event arrivalEvent = new Event(
				now, EventType.ARRIVAL, nextCenter, job, null);

		eventQueue.add(arrivalEvent);

		/* try to get a new job to run, if available */
		Queue<Job> jobQueue = sssq.getQueue();

		/* queue for the sssq is empty, nothing to do. */
		if(jobQueue.isEmpty()) {
			sssq.setActiveServer(false);
			return;
		}

		/* keep the sssq server running: do the next job */
		job = jobQueue.remove();

		scheduleDepartureEvent(now, job, sssq, eventQueue);
	}

	@Override
	public Object doSample() {
		return null;
	}

	private void scheduleDepartureEvent(
			double now, Job job, SssqStatus sssq, EventQueue eventQueue) {

		double svc = serviceProcess.getService();

		/* optional args are *not* null, they're being used to identify 
		 * particular sssq when departure event happens.
		 * this links job to its sssq */
		Event departureEvent = new Event(
				now + svc, EventType.DEPARTURE, this, job, sssq);

		eventQueue.add(departureEvent);
	}
}
