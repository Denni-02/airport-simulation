package mbpmcsn.center;

import mbpmcsn.process.ServiceProcess;

/**
 * m server, where each server has its own dedicated queue
 * arriving jobs must choose which queue to join based on a specific policy
 * (es. Shortest Queue, Random, or Round Robin)
 */

public class MultiServerMultiQueue extends Center {
	private final int numFlows;

	public MultiServerMultiQueue(ServiceProcess serviceProcess, NetworkRoutingPoint networkRoutingPoint, String name, int numFlows) {
		super(serviceProcess, networkRoutingPoint, name);
		this.numFlows = numFlows;
	}

	@Override
	public void onArrival(Object args) {

	}

	@Override
	public void onCompletion(Object args) {

	}
}
