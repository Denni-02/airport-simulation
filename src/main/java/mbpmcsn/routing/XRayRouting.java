package mbpmcsn.routing;

import mbpmcsn.center.Center;
import mbpmcsn.desbook.Rngs;
import mbpmcsn.entity.Job;
import static mbpmcsn.core.Constants.P_CHECK;

/*
decides if the job has to do the extra security controls
 */

public final class XRayRouting implements NetworkRoutingPoint {

    private final Center traceDetection;
    private final Center recovery;
    private final int streamIndex;

    public XRayRouting(Center traceDetection, Center recovery, int streamIndex) {
        this.traceDetection = traceDetection;
        this.recovery = recovery;
        this.streamIndex = streamIndex;
    }

    @Override
    public Center getNextCenter(Rngs r, Job job) {
    	r.selectStream(streamIndex);

    	boolean areFurtherChecksNeeded = r.random() < P_CHECK;
    	job.setSecurityCheckRequested(areFurtherChecksNeeded);
    	return areFurtherChecksNeeded ? traceDetection : recovery;
    }
}
