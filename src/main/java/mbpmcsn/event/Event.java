package mbpmcsn.event;

import mbpmcsn.center.Center;
import mbpmcsn.entity.Job;

/*
 * Represents a discrete event in the simulation.
 */

public final class Event implements Comparable<Event> {

	/* time at which this event happens */
	private final double t;

	/* event type */
	private final EventType type;

	/* job info that triggered event */
	private final Job job;

	/* target center */
	private final Center targetCenter;

	/* optional args */
	private final Object args;

	public Event(double t, EventType type, Center targetCenter, Job job, Object args) {
		this.t = t;
		this.type = type;
		this.targetCenter = targetCenter;
		this.job = job;
		this.args = args;
	}

	public double getTime() {
		return t;
	}

	public EventType getType() { 
		return type; 
	}

	public Center getTargetCenter() { 
		return targetCenter; 
	}

	public Job getJob() { 
		return job; 
	}

	public Object getArgs() {
		return args;
	}

	@Override
	public int compareTo(Event other) {
		return Double.compare(this.t, other.t);
	}

	@Override
	public String toString() {
		return String.format("Event[t=%.4f, type=%s, center=%s, job=%s]",
				t, type, (targetCenter != null ? targetCenter.getClass().getSimpleName() : "None"), job);
	}
}
