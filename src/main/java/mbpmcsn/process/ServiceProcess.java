package mbpmcsn.process;

import mbpmcsn.desbook.Rngs;
import mbpmcsn.process.rvg.RandomVariateGenerator;

/**
 * models the service duration for a specific server or center
 */

public final class ServiceProcess extends Process {
	public ServiceProcess(RandomVariateGenerator rvg, Rngs rngs, int streamIdx) {
		super(rvg, rngs, streamIdx);
	}

	public double getService() {
		rngs.selectStream(streamIdx);
		return rvg.generate(rngs);
	}
}

