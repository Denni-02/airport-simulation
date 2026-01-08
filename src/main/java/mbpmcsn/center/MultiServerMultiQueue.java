package mbpmcsn.center;

import mbpmcsn.process.ServiceProcess;

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
