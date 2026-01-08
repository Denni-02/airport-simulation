package mbpmcsn.event;

public class Event {

	/* event types for each center */
	public enum Type {
		ARRIVAL,
		DEPARTURE 
	}

	/* time at which event happens */
	private final double t;

	/* event type */
	private final Event.Type x;

	/* optional/arbitrary data that can be passed to
	 * center.onArrival and center.onCompletion,
	 * these data may be needed for event handling */
	private final Object args;

	public Event(double t, Event.Type x, Object args) {
		this.t = t;
		this.x = x;
		this.args = args;
	}

	public double getT() {
		return t;
	}

	public Event.Type getX() {
		return x;
	}

	public Object getArgs() {
		return args;
	}
}
