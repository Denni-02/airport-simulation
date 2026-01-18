package mbpmcsn.runners;

import mbpmcsn.core.Constants;
import mbpmcsn.desbook.Rngs;
import mbpmcsn.runners.smbuilders.SimulationModelBuilder;
import mbpmcsn.runners.steadystate.VeryLongRun;
import mbpmcsn.stats.batchmeans.BatchCollector;

public class VerificationRunner implements Runner {

	private final SimulationModelBuilder builder;
	private final Rngs rngs;
	private final double arrivalsMeanTime;

	public VerificationRunner(
			SimulationModelBuilder builder, 
			double arrivalsMeanTime) {

		this.builder = builder;
		this.arrivalsMeanTime = arrivalsMeanTime;
		this.rngs = new Rngs();
		this.rngs.plantSeeds(Constants.SEED);
	}

	@Override
	public void runIt() {
		System.out.println("===================================================================");
		System.out.println("   VERIFICATION RUNNER - Analisi M/M/k");
		System.out.println("   [Ipotesi]: Arrivi Poissoniani, Servizi Esponenziali, t -> inf");
		System.out.println("===================================================================");

		// 1. ESECUZIONE SIMULAZIONE, to be changed with steady state, batch means technique!!!
		VeryLongRun run = new VeryLongRun(
				builder,
				rngs,
				true,  // Attiva M/M/k
				arrivalsMeanTime,
				Constants.TIME_WARMUP
		);

		System.out.println(">>> Avvio simulazione Steady State per verifica...");
		run.run();

		BatchCollector batchCollector = run.getBatchCollector();

		// 2. CONFRONTO ANALITICO
		double lambdaTot = 1 / arrivalsMeanTime;

		System.out.println("\n--- RISULTATI VERIFICA ---");
		System.out.printf("Lambda Totale (Input): %.4f pax/sec\n", lambdaTot);

		// --- VERIFICA CENTRO 1: Check-In (M/M/k) ---
		double lambdaCheckIn = lambdaTot * Constants.P_DESK;
		verifyMMkNode("CheckIn", batchCollector, lambdaCheckIn, Constants.M1, Constants.MEAN_S1);

		// --- VERIFICA CENTRO 2: Varchi (M/M/k) ---
		verifyMMkNode("Varchi", batchCollector, lambdaTot, Constants.M2, Constants.MEAN_S2);

		// --- VERIFICA CENTRO 3: XRay (M/M/k approssimato) ---
		verifyIndependentMM1("XRay", batchCollector, lambdaTot, Constants.M3, Constants.MEAN_S3);

		// --- VERIFICA CENTRO 4: Trace Detection (M/M/k) ---
		double lambdaTrace = lambdaTot * Constants.P_CHECK;
		verifyMMkNode("TraceDetection", batchCollector, lambdaTrace, Constants.M4, Constants.MEAN_S4);

		// --- VERIFICA CENTRO 5: Recupero (M/M/inf) ---
		verifyInfiniteServer("Recupero", batchCollector, Constants.MEAN_S5);
	}

	/*
	 * Verifica per nodi M/M/k (Single Queue o Multi Queue approssimata)
	 */
	private void verifyMMkNode(String name, BatchCollector collector, double lambda, int k, double meanService) {
		double mu = 1.0 / meanService;
		double rho = lambda / (k * mu);

		System.out.printf("\n>>> Centro: %-15s [M/M/%d] (Erlang-C)\n", name, k);

		if (checkInstability(rho)) {
			return;
		}

		// Calcolo P0
		double sum = 0.0;
		double a = lambda / mu;

		for (int n = 0; n < k; n++) {
			sum += Math.pow(a, n) / factorial(n);
		}

		double termK = (Math.pow(a, k) / factorial(k)) * (1.0 / (1.0 - rho));
		double p0 = 1.0 / (sum + termK);

		// Erlang-C: Probabilità di attesa in coda
		double pq = (Math.pow(a, k) * p0) / (factorial(k) * (1.0 - rho));

		// Tempi medi
		double E_Tq = pq / (k * mu - lambda);
		double E_Ts_Theor = E_Tq + meanService;

		compareAndPrint(name, collector, rho, E_Ts_Theor);
	}

	/**
	 * Caso: k * M/M/1 (Multi-Coda con Round Robin)
	 * Formula: M/M/1 su flusso diviso
	 */
	private void verifyIndependentMM1(String name, BatchCollector collector, double lambdaTot, int k, double meanService) {
		System.out.printf("\n>>> Centro: %-15s [k * M/M/1] (Round Robin)\n", name);

		// Dividiamo il flusso, ogni server riceve lambda/k
		double lambdaSingle = lambdaTot / k;
		double mu = 1.0 / meanService;

		// Utilizzo del singolo server (uguale all'utilizzo globale)
		double rho = lambdaSingle / mu;

		if (checkInstability(rho)) {
			return;
		}

		// Formula esatta M/M/1 per il Tempo di Risposta (Wait + Service)
		// E[Ts] = 1 / (mu - lambda)
		double E_Ts_Theor = 1.0 / (mu - lambdaSingle);

		compareAndPrint(name, collector, rho, E_Ts_Theor);
	}

	/**
	 * Verifica specifica per nodi M/M/infinito (Infinite Server)
	 * In questi nodi non esiste coda (Tq = 0), quindi Ts = S
	 */
	private void verifyInfiniteServer(String name, BatchCollector collector, double meanService) {
		System.out.printf("\n>>> Centro: %-15s [M/M/inf] (Delay)\n", name);

		// Non c'è stabilità da controllare (rho è sempre 0 per definizione)
		// Il tempo di risposta è puramente il tempo di servizio
		compareAndPrint(name, collector, 0.0, meanService);
	}

	private boolean checkInstability(double rho) {
		if (rho >= 1.0) {
			System.out.printf("    [ERRORE CRITICO] Sistema Instabile (Rho = %.4f >= 1.0).\n", rho);
			System.out.println("    La teoria prevede code infinite. Impossibile verificare.");
			return true;
		}

		return false;
	}

	private void compareAndPrint(String name, BatchCollector collector, double rho, double expectedVal) {
		// Recupero il valore simulato (Media Globale accumulata)
		double simulatedVal = collector.getBatchGrandMean("Ts_" + name);

		double error = Math.abs(simulatedVal - expectedVal) / expectedVal * 100.0;

		System.out.printf("    Utilizzo (Rho)   : %.4f\n", rho);
		System.out.printf("    E[Ts] Teorico    : %.4f s\n", expectedVal);
		System.out.printf("    E[Ts] Simulato   : %.4f s\n", simulatedVal);

		if (error < 5.0) {
			System.out.printf("    [OK] Verifica Superata (Errore: %.2f%%)\n", error);
		} else {
			System.out.printf("    [WARNING] Errore alto (%.2f%%). Aumentare durata?\n", error);
		}
	}

	// Helper matematico per Erlang-C
	private double factorial(int n) {
		if (n == 0) {
			return 1.0;
		}

		double fact = 1.0;
		for (int i = 1; i <= n; i++) {
			fact *= i;
		}

		return fact;
	}
}
