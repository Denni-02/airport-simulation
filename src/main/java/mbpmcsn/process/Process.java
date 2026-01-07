package mbpmcsn.process;

import mbpmcsn.desbook.Rngs;
import mbpmcsn.process.rvg.RandomVariateGenerator;

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
