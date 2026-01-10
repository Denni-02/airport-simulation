package mbpmcsn.routing;

import mbpmcsn.center.SssqStatus;

import java.util.List;

public class RoundRobinPolicy implements FlowAssignmentPolicy {

    // index of the last selected queue, start from -1
    private int lastAssignedIndex = -1;

    @Override
    public int assignFlow(List<SssqStatus> sssqs) {
        lastAssignedIndex = (lastAssignedIndex + 1) % sssqs.size();
        return lastAssignedIndex;
    }

}
