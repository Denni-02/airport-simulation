package mbpmcsn.entity;

public class Job {

    private static int ID_COUNTER = 0;

    private final int id;
    private final double arrivalTime; // entry time in the system
    private boolean hasCheckedBaggage;

    public Job(double arrivalTime) {
        this.id = ++ID_COUNTER;
        this.arrivalTime = arrivalTime;
        this.hasCheckedBaggage = false;
    }

    public int getId() { return id; }
    public double getArrivalTime() { return arrivalTime; }
    public boolean hasCheckedBaggage() { return hasCheckedBaggage; }
    public void setHasCheckedBaggage(boolean hasCheckedBaggage) { this.hasCheckedBaggage = hasCheckedBaggage; }

    @Override
    public String toString() {
        return "Job#" + id;
    }
}
