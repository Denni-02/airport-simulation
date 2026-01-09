package mbpmcsn.center;

import mbpmcsn.desbook.Rngs;

/**
 * determines the next destination of a job after completing service
 * at the current center using probabilistic rules defined
 */

public interface NetworkRoutingPoint {
	Center getNextCenter(Rngs rngs, int streamIdx);
}
