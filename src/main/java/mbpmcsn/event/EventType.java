package mbpmcsn.event;

/**
 * Enumeration of types of events
 * - ARRIVAL: a job enters a center
 * - DEPARTURE: a job finishes service at a center
 * - SAMPLING: a special event for collecting statistics
 */

public enum EventType {
    ARRIVAL,
    DEPARTURE,
    SAMPLING
}
