package mbpmcsn.process;

import mbpmcsn.desbook.Rngs;
import mbpmcsn.process.rvg.RandomVariateGenerator;

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

