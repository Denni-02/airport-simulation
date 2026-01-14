package mbpmcsn.stats.sampling;

import mbpmcsn.csv.annotations.*;

/**
 * Represents a single discrete data point in a time-series
 * It corresponds to a value observed at a specific instant 't'
 * Used to plot the evolution of the system state over time (for es. Transient Analysis)
 */

@CsvDescriptor
public class Sample {
    private final double timestamp; // Time 't' of observation
    private final String centerName; // Source of the data (es. "CheckIn")
    private final String metric; // What is being measured (es. "QueueLength")
    private final double value; // The measured value at time 't'

    public Sample(double timestamp, String centerName, String metric, double value) {
        this.timestamp = timestamp;
        this.centerName = centerName;
        this.metric = metric;
        this.value = value;
    }

    @CsvColumn(order = 1, name = "Time")
    public double getTimestamp() { 
    	return timestamp; 
    }

    @CsvColumn(order = 2, name = "Center")
    public String getCenterName() { 
    	return centerName; 
    }

    @CsvColumn(order = 3, name = "Metric")
    public String getMetric() { 
    	return metric; 
    }

    @CsvColumn(order = 4, name = "Value")
    public double getValue() { 
    	return value; 
    }

    @Override
    public String toString() {
        //for CSV: Time;Center;Metric;Value
        return String.format("%.2f;%s;%s;%.2f", timestamp, centerName, metric, value);
    }
}
