package mbpmcsn.routing;

import mbpmcsn.center.Center;
import mbpmcsn.desbook.Rngs;
import mbpmcsn.entity.Job;
import static mbpmcsn.core.Constants.P_CHECK;

/*
decides if the job has to do the extra security controls
 */
public class XRayRouting implements NetworkRoutingPoint {

    private Center traceDetection;
    private Center recovery;

    public void setDestinations(Center traceDetection, Center recovery) {
        this.traceDetection = traceDetection;
        this.recovery = recovery;
    }

    @Override
    public Center getNextCenter(Rngs r, Job job) {
        if (r.random() < P_CHECK) {
            // additional controls
            job.setSecurityCheckRequested(true);
            return traceDetection;
        } else {
            // standard controls
            job.setSecurityCheckRequested(false);
            return recovery;
        }
    }
}