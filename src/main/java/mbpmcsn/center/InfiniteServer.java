package mbpmcsn.center;

import mbpmcsn.process.ServiceProcess;

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

