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
    private final int streamIndex;

    public EntryRouting(Center checkIn, Center varchi, int streamIndex) {
    	this.checkIn = checkIn;
    	this.varchi = varchi;
    	this.streamIndex = streamIndex;
    }

    @Override
    public Center getNextCenter(Rngs r, Job job) {
    	r.selectStream(streamIndex);

    	boolean goesToCheckIn = r.random() < P_DESK;
    	job.setCheckedBaggage(goesToCheckIn);
    	return goesToCheckIn ? checkIn : varchi;
    }
}
