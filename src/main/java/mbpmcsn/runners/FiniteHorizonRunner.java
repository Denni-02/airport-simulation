package mbpmcsn.runners;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

import mbpmcsn.stats.accumulating.StatCollector;
import mbpmcsn.stats.ie.IntervalEstimation;
import mbpmcsn.stats.sampling.Sample;
import mbpmcsn.desbook.Rngs;
import mbpmcsn.runners.smbuilders.SimulationModelBuilder;
import mbpmcsn.stats.accumulating.StatLogger;

import static mbpmcsn.core.Constants.SEED;

/**
 * finite horizon simulation with a specific duration (one working day, 06:00 - 24:00).
 */

public final class FiniteHorizonRunner implements Runner {

	private static final int NUM_REPLICATIONS = 64;

	private final SimulationModelBuilder builder;
	private final double simulationTime;
	private final boolean approxServicesAsExp;
	private final double samplingInterval;
	private final Rngs rngs;

	public FiniteHorizonRunner(
			SimulationModelBuilder smBuilder,
			double simulationTime,
			boolean approxServicesAsExp,
			double samplingInterval) {

		this.builder = smBuilder;
		this.simulationTime = simulationTime;
		this.approxServicesAsExp = approxServicesAsExp;
		this.samplingInterval = samplingInterval;
		this.rngs = new Rngs();
		this.rngs.plantSeeds(SEED);
	}

	@Override 
	public void runIt() {

		// STAMPA HEADER
		printExperimentHeader();

		// MAPPE  PER RACCOGLIERE I DATI DI TUTTE LE REPLICAZIONI
		Map<String, List<Double>> populationData = new HashMap<>();
		Map<String, List<Double>> timeData = new HashMap<>();

		// LOOP DELLE REPLICAZIONI
		for (int i = 0; i < NUM_REPLICATIONS; i++) {

			// CAMPIONAMENTO DELLA PRIMA RUN
			double currentSampling = (i == 0) ? samplingInterval : 0;

			// ESECUZIONE DELLA SINGOLA REPLICA
			SingleReplication run = new SingleReplication(
					builder, rngs, simulationTime, approxServicesAsExp, currentSampling
			);

			run.runReplication();
			StatCollector stats = run.getStatCollector();

			// ACCUMULO DATI SU TUTTE LE RUN
			for (final String key : stats.getPopulationStats().keySet()) {
				populationData.putIfAbsent(key, new ArrayList<>());
				populationData.get(key).add(stats.getPopulationMean(key));
			}

			for (final String key : stats.getTimeStats().keySet()) {
				timeData.putIfAbsent(key, new ArrayList<>());
				timeData.get(key).add(stats.getTimeWeightedMean(key));
			}

			// GESTIONE OUTPUT DETTAGLIATO (SOLO RUN 1)
			if (i == 0) {
				printPilotRunDiagnostic(stats, run.getSampleCollector().getSamples());
				System.out.println("\n... Esecuzione delle restanti " + (NUM_REPLICATIONS - 1) + " replicazioni in background ...");
			}
		}

		// REPORT MEDIA SU TUTTE LE RUN
		printFinalScientificResults(populationData, timeData);
	}

	private void printExperimentHeader() {
		System.out.println("\n");
		System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("||   AVVIO ESPERIMENTO DI SIMULAZIONE A ORIZZONTE FINITO          ||");
		System.out.printf( "||   Replicazioni: %-3d                                            ||\n", NUM_REPLICATIONS);
		System.out.printf( "||   Durata singola run: %-10.0f secondi                       ||\n", simulationTime);
		System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n");
	}

	// Per la prima replica stampiamo sia statistiche time e jov avg sia il sampling
	private void printPilotRunDiagnostic(StatCollector stats, List<Sample> samples) {
		System.out.println("\n");
		System.out.println("####################################################################");
		System.out.println("#  SEZIONE 1: DIAGNOSTICA REPLICAZIONE PILOTA (RUN #1 di 64)       #");
		System.out.println("####################################################################");

		System.out.println("\nA. Medie della Singola Replica ");
		StatLogger.printReport(stats);

		System.out.println("\nB. Campionamento Temporale ");
		System.out.println("");
		int totalSamples = samples.size();
		System.out.printf("Campioni raccolti: %d (Intervallo: %.2f s)\n", totalSamples, samplingInterval);

		if (totalSamples > 0) {
			System.out.println("\nTime       | Center               | Metric                    | Value");
			System.out.println("-----------+----------------------+---------------------------+----------");
			for (Sample s : samples) {
				System.out.printf("%-10.2f | %-20s | %-25s | %.4f\n",
						s.getTimestamp(), s.getCenterName(), s.getMetric(), s.getValue());
			}
			System.out.println("-----------+----------------------+---------------------------+----------");
		} else {
			System.out.println("(Nessun campione raccolto)");
		}
		System.out.println("####################################################################");
	}

	// Stampa il report scientifico finale (Intervalli di confidenza)
	private void printFinalScientificResults(Map<String, List<Double>> popData, Map<String, List<Double>> timeData) {
		System.out.println("\n\n");
		System.out.println("####################################################################");
		System.out.println("#  SEZIONE 2: RISULTATI SCIENTIFICI FINALI (SU 64 REPLICAZIONI)    #");
		System.out.println("####################################################################");

		System.out.println("\n>>> Statistiche Job Based <<<\n");
		System.out.println("Metrica                        | Media Stimata | Intervallo (95%) | Range Confidenza [Min ... Max]");
		System.out.println("-------------------------------+---------------+------------------+---------------------------------");
		for (String key : new TreeMap<>(popData).keySet()) {
			printIntervalRow(key, popData.get(key));
		}

		System.out.println("\n\n>>> Statistiche Time Based <<<\n");
		System.out.println("Metrica                        | Media Stimata | Intervallo (95%) | Range Confidenza [Min ... Max]");
		System.out.println("-------------------------------+---------------+------------------+---------------------------------");
		for (String key : new TreeMap<>(timeData).keySet()) {
			printIntervalRow(key, timeData.get(key));
		}
		System.out.println("\n");
	}

	private void printIntervalRow(String metric, List<Double> values) {
		try {
			double width = IntervalEstimation.width(values);
			double mean = values.stream().mapToDouble(v -> v).average().orElse(0.0);
			double min = mean - width;
			double max = mean + width;

			System.out.printf("%-30s | %13.4f | +/- %12.4f | [%10.4f ... %10.4f]\n",
					metric, mean, width, min, max);
		} catch (Exception e) {
			System.out.printf("%-30s |   DATI INSUFFICIENTI PER STIMA   |\n", metric);
		}
	}
}
