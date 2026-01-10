package mbpmcsn.center;

import mbpmcsn.desbook.Rngs;
import mbpmcsn.entity.Job;

/**
 * determines the next destination of a job after completing service
 * at the current center using probabilistic rules defined
 */

public interface NetworkRoutingPoint {
	/* passing job allows to change attributes of the incoming job,
	 * that is, domain-specific infos, e.g. checkedBaggage, determined
	 * by probabilities */
	Center getNextCenter(Rngs rngs, Job job);
}
