package mbpmcsn.center;

import mbpmcsn.process.ServiceProcess;
import mbpmcsn.desbook.Rngs;

public abstract class Center {
	protected final ServiceProcess serviceProcess;
	private final NetworkRoutingPoint networkRoutingPoint;

	private final String name;

	protected long numJobsInNode;
	protected long numJobsCompletions;

	protected double firstArrivalTime = Double.NEGATIVE_INFINITY;
	protected double lastArrivalTime;
	protected double lastCompletionTime;

	protected double nodeArea;
	protected double queueArea;
	protected double serviceArea;

	protected Center(ServiceProcess serviceProcess, NetworkRoutingPoint networkRoutingPoint, String name) {
		this.serviceProcess = serviceProcess;
		this.networkRoutingPoint = networkRoutingPoint;
		this.name = name;
	}

	/* utility method (wrapper) that should be used
	 * within onCompletion() to decide the next center
	 * to generate the arrival event for, based on the
	 * routing matrix */
	protected final Center getNextCenter() {
		Rngs rngs = serviceProcess.getRngs();
		int streamIdx = serviceProcess.getStreamIdx() + 1;
		return networkRoutingPoint.getNextCenter(rngs, streamIdx);
	}

	/* called by event handler */
	public abstract void onArrival(Object args);

	/* called by event handler */
	public abstract void onCompletion(Object args);
}

