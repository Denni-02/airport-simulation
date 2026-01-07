package mbpmcsn.center.process;

import mbpmcsn.center.process.rvg.RandomVariateGenerator;

public final class ServiceProcess extends Process {
	public ServiceProcess(RandomVariateGenerator rvg, int streamIdx) {
		super(rvg, streamIdx);
	}

	public double getService() {
		return rvg.generate(rngs);
	}
}

