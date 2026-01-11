package mbpmcsn.stats;

import mbpmcsn.event.Event;
import mbpmcsn.event.EventQueue;

public interface OnSamplingCallback {
	void collectSample(Event event, EventQueue eventQueue, Object data);
}
