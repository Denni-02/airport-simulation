package mbpmcsn.routing;

import mbpmcsn.center.Center;
import mbpmcsn.desbook.Rngs;
import mbpmcsn.entity.Job;
import static mbpmcsn.core.Constants.P_DESK;

/*
 * for the initial routing
 * check in or elettronic gates
 */

public final class EntryRouting implements NetworkRoutingPoint {

    private final Center checkIn;
    private final Center varchi;

    public EntryRouting(Center checkIn, Center varchi) {
    	this.checkIn = checkIn;
    	this.varchi = varchi;
    }

    @Override
    public Center getNextCenter(Rngs r, Job job) {
        if (r.random() < P_DESK) {
            // go to check-in
            job.setCheckedBaggage(true);
            return checkIn;
        } else {
            // go to e-gates
            job.setCheckedBaggage(false);
            return varchi;
        }
    }
}
