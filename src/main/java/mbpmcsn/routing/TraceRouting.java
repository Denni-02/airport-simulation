package mbpmcsn.routing;

import mbpmcsn.center.Center;
import mbpmcsn.desbook.Rngs;
import mbpmcsn.entity.Job;
import static mbpmcsn.core.Constants.P_FAIL;

public final class TraceRouting implements NetworkRoutingPoint {

	/* destination if the additional control is OK */
    private final Center recoveryCenter;
    private final int streamIndex;

    public TraceRouting(Center recoveryCenter, int streamIndex) {
        this.recoveryCenter = recoveryCenter;
        this.streamIndex = streamIndex;
    }

    @Override
    public Center getNextCenter(Rngs r, Job job) {
    	r.selectStream(streamIndex);

    	boolean isTracingFailed = r.random() < P_FAIL;
    	job.setSecurityCheckFailed(isTracingFailed);
    	return isTracingFailed ? null : recoveryCenter;
    }
}
