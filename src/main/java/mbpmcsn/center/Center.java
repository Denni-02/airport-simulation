package mbpmcsn.center;

import mbpmcsn.process.ServiceProcess;
import mbpmcsn.desbook.Rngs;

/**
 * abstract base class representing a generic node in the queueing network
 * maintains the state of the center and manages the statistics
 */

public abstract class Center {

	protected final ServiceProcess serviceProcess;
	private final NetworkRoutingPoint networkRoutingPoint;

	private final String name;

	protected long numJobsInNode;
	protected long numJobsCompletions;

	protected double firstArrivalTime = Double.NEGATIVE_INFINITY;
	protected double lastArrivalTime;
	protected double lastCompletionTime;

	protected double nodeArea;
	protected double queueArea;
	protected double serviceArea;

	/** DA VALUTARE!
	 * La mia idea è di togliere le variabili nodeArea, queueArea, serviceArea, ecc.
	 * E di gestire in maniera centralizzata cone le mappe di StatCollector
	 * Quindi gli attributi diventerebbero:

	 protected final int id; // ID (es. Constants.ID_CHECKIN)
	 protected final String name;

	 protected final ServiceProcess serviceProcess;
	 protected final NetworkRoutingPoint networkRoutingPoint;
	 protected final StatCollector statCollector;

	 protected long numJobsInNode = 0; // totale utenti nel centro (coda + servizio), stato minimo comune
	 protected double lastUpdateTime = 0.0;  // per le statistiche Time-Weighted

	 * Conseguentemente aggiorniamo il costruttore:

	 protected Center(int id, String name, ServiceProcess serviceProcess, NetworkRoutingPoint networkRoutingPoint, StatCollector statCollector) {
	 	this.id = id;
	 	this.name = name;
	 	this.serviceProcess = serviceProcess;
	 	this.networkRoutingPoint = networkRoutingPoint;
	 	this.statCollector = statCollector;
	 }

	 * E aggiungiamo il metodo collectTimeStats
	 * che aggiorna l'integrale del numero di utenti nel nodo, per esempio nome metrica = N_CheckIn
	 * poi le sottoclassi che fanno override aggiungono la logica per le metriche Queue_
	 * e infine aggiorna il timestamp

	 protected void collectTimeStats(double currentClock) {
	 	double duration = currentClock - lastUpdateTime;
	 	statCollector.updateArea("N_" + name, numJobsInNode, duration);
	 	lastUpdateTime = currentClock;
	 }

	 * Per esempio la collectTimeStat di MultiServerSingleQueue fa:
	 * double duration = currentClock - lastUpdateTime;
	 * statCollector.updateArea("Queue_" + name, queue.size(), duration);
	 * e poi chiama super.collectTimeStats(currentClock);

	 * Infine i nuovi getter:

	 public int getId() { return id; }
	 public String getName() { return name; }
	 */

	protected Center(ServiceProcess serviceProcess, NetworkRoutingPoint networkRoutingPoint, String name) {
		this.serviceProcess = serviceProcess;
		this.networkRoutingPoint = networkRoutingPoint;
		this.name = name;
	}


	/* utility method (wrapper) that should be used
	 * within onCompletion() to decide the next center
	 * to generate the arrival event for, based on the
	 * routing matrix */
	protected final Center getNextCenter() {
		Rngs rngs = serviceProcess.getRngs();
		int streamIdx = serviceProcess.getStreamIdx() + 1;
		return networkRoutingPoint.getNextCenter(rngs, streamIdx);
	}

	/** ALTRA COSA DA VALUTARE!
	 * Non ha più senso passare Event e invece di Object args?
	 * Accediamo facilmente a e.getJob() e e.getTime()
	 */

	/* called by event handler */
	public abstract void onArrival(Object args);

	/* called by event handler */
	public abstract void onCompletion(Object args);
}

