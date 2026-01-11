package mbpmcsn.routing;

import mbpmcsn.center.Center;
import mbpmcsn.desbook.Rngs;
import mbpmcsn.entity.Job;
import static mbpmcsn.core.Constants.P_FAIL;

public final class TraceRouting implements NetworkRoutingPoint {

	/* destination if the additional control is OK */
    private final Center recoveryCenter;

    public TraceRouting(Center recoveryCenter) {
        this.recoveryCenter = recoveryCenter;
    }

    @Override
    public Center getNextCenter(Rngs r, Job job) {

        if (r.random() < P_FAIL) {
            // FAILURE: exit from the system
            job.setSecurityCheckFailed(true);
            return null;
        } else {
            // SUCCESS: go to recovery of personal objects
            job.setSecurityCheckFailed(false);
            return recoveryCenter;
        }
    }
}
