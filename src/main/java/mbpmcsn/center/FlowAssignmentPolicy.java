package mbpmcsn.center;

import java.util.List;

/* Assign arrival flow. May use queues and server statuses to decide. */
public interface FlowAssignmentPolicy {
	int assignFlow(List<SssqStatus> sssqs);
}
