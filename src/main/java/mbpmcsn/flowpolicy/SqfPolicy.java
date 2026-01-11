package mbpmcsn.flowpolicy;

import mbpmcsn.center.SssqStatus;
import mbpmcsn.desbook.Rngs;

import java.util.ArrayList;
import java.util.List;

public final class SqfPolicy implements FlowAssignmentPolicy {

    private final Rngs rngs;
    private final int streamIndex;

    public SqfPolicy(Rngs rngs, int streamIndex) {
        this.rngs = rngs;
        this.streamIndex = streamIndex;
    }

    @Override
    public int assignFlow(List<SssqStatus> sssqs) {
        int minSize = Integer.MAX_VALUE;

        // PHASE 1: find min size between queues
        for (SssqStatus status : sssqs) {
            int currentSize = status.getQueue().size();
            if (currentSize < minSize) {
                minSize = currentSize;
            }
        }

        // PHASE 2: index of all queues with min length
        List<Integer> bestCandidates = new ArrayList<>();
        for (int i = 0; i < sssqs.size(); i++) {
            if (sssqs.get(i).getQueue().size() == minSize) {
                bestCandidates.add(i);
            }
        }

        // PHASE 3: decision

        // if 1 winner, return it
        if (bestCandidates.size() == 1) {
            return bestCandidates.get(0);
        }

        // if tie --> random
        rngs.selectStream(streamIndex);
        long randomIndex = (long) (rngs.random() * bestCandidates.size());
        return bestCandidates.get((int) randomIndex);
    }
}

