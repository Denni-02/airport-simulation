package mbpmcsn.center;

import mbpmcsn.process.ServiceProcess;

/**
 * represents a Delay Node (no queue)
 * every arriving job is immediately served.
 */

public class InfiniteServer extends Center {
	public InfiniteServer(ServiceProcess serviceProcess, NetworkRoutingPoint networkRoutingPoint, String name) {
		super(serviceProcess, networkRoutingPoint, name);
	}

	@Override
	public void onArrival(Object args) {

	}

	@Override
	public void onCompletion(Object args) {

	}
}

