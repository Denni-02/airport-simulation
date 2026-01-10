package mbpmcsn.routing;

import mbpmcsn.center.Center;
import mbpmcsn.desbook.Rngs;
import mbpmcsn.entity.Job;
import static mbpmcsn.core.Constants.P_FAIL;

public class TraceRouting implements NetworkRoutingPoint {

    private Center recoveryCenter; /* destination if the
                                    additional control is OK */

    public void setDestination(Center recoveryCenter) {
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