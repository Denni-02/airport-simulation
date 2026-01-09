package mbpmcsn.process;

import mbpmcsn.desbook.Rngs;
import mbpmcsn.process.rvg.RandomVariateGenerator;

/**
 * abstract base class for all stochastic processes in the simulation
 * links a specific Random Number Generator stream (Rngs)
 * with a specific probability distribution (RandomVariateGenerator)
 * to ensures that every process (arrivals, service) has its own independent
 * sequence of random numbers.
 */

class Process {
	protected final Rngs rngs;
	protected final int streamIdx;
	protected final RandomVariateGenerator rvg;

	protected Process(RandomVariateGenerator rvg, Rngs rngs, int streamIdx) {
		this.streamIdx = streamIdx;
		this.rvg = rvg;
		this.rngs = rngs;
	}

	public RandomVariateGenerator getRvg() {
		return rvg;
	}

	public int getStreamIdx() {
		return streamIdx;
	}

	public Rngs getRngs() {
		return rngs;
	}
}
