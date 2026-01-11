package mbpmcsn.routing;

import mbpmcsn.center.Center;
import mbpmcsn.desbook.Rngs;
import mbpmcsn.entity.Job;

/*
 * for fixed routing
 * es. Check-In --> Varchi Elettronici
 */

public final class FixedRouting implements NetworkRoutingPoint {

    private final Center nextCenter;

    public FixedRouting(Center nextCenter) {
    	this.nextCenter = nextCenter;
    }

    @Override
    public Center getNextCenter(Rngs r, Job job) {
        return nextCenter;
    }
}
