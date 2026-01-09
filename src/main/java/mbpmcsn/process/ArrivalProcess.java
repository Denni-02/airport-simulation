package mbpmcsn.process;

import mbpmcsn.desbook.Rngs;
import mbpmcsn.process.rvg.RandomVariateGenerator;

/**
 * generates Inter-Arrival Times (time between two consecutive arrivals)
 * and calculates the new arrival time
 */

public final class ArrivalProcess extends Process {
	private double sarrival;

	public ArrivalProcess(RandomVariateGenerator rvg, Rngs rngs, int streamIdx) {
		super(rvg, rngs, streamIdx);
	}

	public double getArrival() {
		rngs.selectStream(streamIdx);
		sarrival += rvg.generate(rngs);
		return sarrival;
	}
}

