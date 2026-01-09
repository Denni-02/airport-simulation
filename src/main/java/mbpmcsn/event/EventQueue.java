package mbpmcsn.event;

import java.util.PriorityQueue;

/**
 * Manages the list of future events, ordered by time..
 */

public class EventQueue {

    // data struct for the queue
    private final PriorityQueue<Event> queue;

    // time of the last processed event
    private double currentClock;

    public EventQueue() {
        this.queue = new PriorityQueue<>();
        this.currentClock = 0.0;
    }

    // adds a new event to the queue in the correct time order
    public void add(Event e) {
        if (e.getTime() < currentClock) {
            System.err.println("WARNING: Tentativo di schedulare evento nel passato: "
                    + e.getTime() + " < " + currentClock);
        }
        queue.add(e);
    }

    /*
     * removes and returns the next event (lowest time)
     * updates currentClock
     */
    public Event pop() {
        Event e = queue.poll();
        if (e != null) {
            // updating current clock
            currentClock = e.getTime();
        }
        return e;
    }

    // returns the next event without removing
    public Event peek() {
        return queue.peek();
    }

    // indicates if the queue is empty
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    // clear all
    public void clear() {
        queue.clear();
        currentClock = 0.0;
    }

    public double getCurrentClock() {
        return currentClock;
    }
}
