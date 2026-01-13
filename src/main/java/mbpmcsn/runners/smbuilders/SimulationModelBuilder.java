package mbpmcsn.runners.smbuilders;

import mbpmcsn.core.SimulationModel;
import mbpmcsn.event.EventQueue;
import mbpmcsn.stats.accumulating.StatCollector;
import mbpmcsn.stats.sampling.SampleCollector;
import mbpmcsn.desbook.Rngs;

public interface SimulationModelBuilder {
	SimulationModel build(
			Rngs rngs, 
			EventQueue eventQueue, 
			StatCollector statCollector, 
			SampleCollector sampleCollector, 
			boolean approxServicesAsExp);
}
