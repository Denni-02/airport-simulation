package mbpmcsn.center;

import mbpmcsn.desbook.Rngs;

public interface NetworkRoutingPoint {
	Center getNextCenter(Rngs rngs, int streamIdx);
}
