package mbpmcsn.center;

import mbpmcsn.process.ServiceProcess;

public class SingleServerSingleQueue extends Center {
	public SingleServerSingleQueue(ServiceProcess serviceProcess, NetworkRoutingPoint networkRoutingPoint, String name) {
		super(serviceProcess, networkRoutingPoint, name);
	}

	@Override
	public void onArrival(Object data) {

	}

	@Override
	public void onCompletion(Object data) {

	}
}

