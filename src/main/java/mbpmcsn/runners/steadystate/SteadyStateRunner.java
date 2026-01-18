package mbpmcsn.runners.steadystate;

import mbpmcsn.runners.Runner;
import mbpmcsn.runners.smbuilders.SimulationModelBuilder;
import mbpmcsn.desbook.Rngs;

import mbpmcsn.core.Constants;


/**
 * infinite horizon simulation to estimate stable performance measures
 * we have to discard the initial warm-up period, use the Batch Means
 * an estimate confidence intervals
 */

public final class SteadyStateRunner implements Runner {
	private final String experimentName;
	private final boolean approxServicesAsExp;
	private final double arrivalsMeanTime;

	private final VeryLongRun veryLongRun;

	public SteadyStateRunner(
			String experimentName,
			SimulationModelBuilder builder,
			boolean approxServicesAsExp,
			double arrivalsMeanTime,
			double timeWarmup) {

		this.experimentName = experimentName;
		this.approxServicesAsExp = approxServicesAsExp;
		this.arrivalsMeanTime = arrivalsMeanTime;

		Rngs rngs = new Rngs();
		rngs.plantSeeds(Constants.SEED);

		veryLongRun = new VeryLongRun(
				builder, rngs, 
				approxServicesAsExp, 
				arrivalsMeanTime,
				timeWarmup);
	}

	@Override
	public void runIt() {
		printExperimentHeader();
		veryLongRun.run();

	}

	private void printExperimentHeader() {
		System.out.println("\n");
		System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("||   AVVIO ESPERIMENTO DI SIMULAZIONE A ORIZZONTE INFINITO          ||");
		System.out.printf( "||   BatchMeans params: (b=%d,k=%d)                       ||\n",
				VeryLongRun.BATCH_SIZE, VeryLongRun.NUM_BATCHES);
		System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n");
	}

}
