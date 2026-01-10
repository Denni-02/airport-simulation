package mbpmcsn.routing;

import mbpmcsn.center.Center;
import mbpmcsn.desbook.Rngs;
import mbpmcsn.entity.Job;

/**
 * for fixed routing
 * es. Check-In --> Varchi Elettronici
 */
public class FixedRouting implements NetworkRoutingPoint {

    private Center nextCenter;

    // if nextCenter == null --> exit from the system (success)
    public void setDestination(Center nextCenter) {
        this.nextCenter = nextCenter;
    }

    @Override
    public Center getNextCenter(Rngs r, Job job) {
        return nextCenter;
    }
}