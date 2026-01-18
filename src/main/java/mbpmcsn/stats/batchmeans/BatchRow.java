package mbpmcsn.stats.batchmeans;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import mbpmcsn.csv.annotations.*;

/* represents each single batch */

@CsvDescriptor
public final class BatchRow {
	private final String metric;
	private final int numBatch;
	private final double val;

	private BatchRow(String metric, int numBatch, double val) {
		this.metric = metric;
		this.numBatch = numBatch;
		this.val = val;
	}

	@CsvColumn(order = 1, name = "Metric")
	public String getMetric() {
		return metric;
	}

	@CsvColumn(order = 2, name = "NumBatch")
	public int getNumBatch() {
		return numBatch;
	}

	@CsvColumn(order = 3, name = "Value")
	public double getVal() {
		return val;
	}

	public static List<BatchRow> fromMapOfData(Map<String, List<Double>> batches) {
		List<BatchRow> batchesRows = new ArrayList<>();

		for(final String metricNameKey : batches.keySet()) {
			List<Double> localBatches = batches.get(metricNameKey);
			for(int b = 0; b < localBatches.size(); b++) {
				BatchRow row = 
					new BatchRow(
							metricNameKey, b + 1, localBatches.get(b));

				batchesRows.add(row);
			}
		}

		return batchesRows;
	}
}
