package mbpmcsn.center;

import mbpmcsn.process.ServiceProcess;

public class MultiServerSingleQueue extends Center {
	private final int numServers;

	public MultiServerSingleQueue(ServiceProcess serviceProcess,NetworkRoutingPoint networkRoutingPoint, String name, int numServers) {
		super(serviceProcess, networkRoutingPoint, name);
		this.numServers = numServers;
	}

	@Override
	public void onArrival(Object data) {

	}

	@Override
	public void onCompletion(Object data) {

	}
}

