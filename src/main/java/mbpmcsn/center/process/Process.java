package mbpmcsn.center.process;

import mbpmcsn.desbook.Rngs;
import mbpmcsn.center.process.rvg.RandomVariateGenerator;

abstract class Process {
	protected Rngs rngs;
	private final int streamIdx;
	protected final RandomVariateGenerator rvg;

	protected Process(RandomVariateGenerator rvg, int streamIdx) {
		this.streamIdx = streamIdx;
		this.rvg = rvg;
		resetRngs(new Rngs());
	}

	public void resetRngs(Rngs rngs) {
		this.rngs = rngs;
		this.rngs.selectStream(streamIdx);
	}

	public RandomVariateGenerator getRvg() {
		return rvg;
	}

	public int getStreamIdx() {
		return streamIdx;
	}
}

