package mbpmcsn.core;

import mbpmcsn.center.*;
import mbpmcsn.entity.Job;
import mbpmcsn.event.Event;
import mbpmcsn.event.EventType;
import mbpmcsn.process.ServiceProcess;
import mbpmcsn.process.rvg.ErlangGenerator;
import mbpmcsn.process.rvg.ExponentialGenerator;
import mbpmcsn.process.rvg.TruncatedNormalGenerator;
import mbpmcsn.routing.*;
import mbpmcsn.flowpolicy.*;
import mbpmcsn.desbook.Rngs;
import mbpmcsn.event.EventQueue;
import mbpmcsn.process.ArrivalProcess;
import mbpmcsn.stats.StatCollector;
import java.util.ArrayList;
import java.util.List;

import static mbpmcsn.core.Constants.*;

/*
 * represents the simulation mind that initializes the model topology,
 * instantiates the centers and generators, and controls the
 * next-event time advance loop
 */

public final class SimulationModel {

    /* core components */
    private final EventQueue eventQueue;
    private final StatCollector statCollector;
    private final Rngs rngs;

    /* arrival */
    private ArrivalProcess arrivalProcess;

    /* centers */
    private MultiServerSingleQueue checkInCenter;    // ID 1
    private MultiServerMultiQueue varchiCenter;      // ID 2
    private InfiniteServer prepCenter;               // ID 3
    private MultiServerMultiQueue xRayCenter;        // ID 4
    private SingleServerSingleQueue traceCenter;     // ID 5
    private InfiniteServer recCenter;           // ID 6

    private final List<Center> centers = new ArrayList<>();

    /* routing */
    private EntryRouting rIngresso;
    private FixedRouting rCheckIn;
    private FixedRouting rVarchi;
    private FixedRouting rPrep;
    private XRayRouting rXRay;
    private TraceRouting rTrace;
    private FixedRouting rRecupero;

    public SimulationModel(
    		Rngs rngs, 
    		EventQueue eventQueue, 
    		StatCollector statCollector) {

        this.rngs = rngs;
        this.eventQueue = eventQueue;
        this.statCollector = statCollector;

        createRoutingLogic();
        createArrivalProcess();
        createCenters();
        collectAllCenters();
    }

    private void createRoutingLogic() {
        rIngresso = new EntryRouting(checkInCenter, varchiCenter, STREAM_ARRIVALS);
        rCheckIn  = new FixedRouting(varchiCenter);
        rVarchi   = new FixedRouting(prepCenter);
        rPrep     = new FixedRouting(xRayCenter);
        rXRay     = new XRayRouting(traceCenter, recCenter, STREAM_S4_ROUTING);
        rTrace    = new TraceRouting(recCenter, STREAM_S5_ROUTING);
        rRecupero = new FixedRouting(null);
    }

    private void createArrivalProcess() {
    	// --- Setup Arrivals Generator  ---
        arrivalProcess = new ArrivalProcess(
                new ExponentialGenerator(ARRIVAL_MEAN_TIME),
                rngs, STREAM_ARRIVALS
        );
    }

    private void createCenters() {
        // 1. Check-in
        ServiceProcess sp1 = new ServiceProcess(
                new TruncatedNormalGenerator(MEAN_S1, STD_S1, LB1, UB1),
                rngs, STREAM_S1_SERVICE
        );
        checkInCenter = new MultiServerSingleQueue(
                ID_BANCHI_CHECKIN, "CheckIn", sp1, rCheckIn, statCollector, M1
        );

        // 2. Varchi (MSMQ)
        ServiceProcess sp2 = new ServiceProcess(
                new TruncatedNormalGenerator(MEAN_S2, STD_S2, LB2, UB2),
                rngs, STREAM_S2_SERVICE
        );
        FlowAssignmentPolicy sqfPolicy = new SqfPolicy(rngs, STREAM_S2_FLOWPOL);
        varchiCenter = new MultiServerMultiQueue(
                ID_VARCHI_ELETTRONICI, "Varchi", sp2, rVarchi, statCollector, M2, sqfPolicy
        );

        // 3. Preparazione
        ServiceProcess sp3 = new ServiceProcess(
                new TruncatedNormalGenerator(MEAN_S3, STD_S3, LB3, UB3),
                rngs, STREAM_S3_SERVICE
        );
        prepCenter = new InfiniteServer(
                ID_PREPARAZIONE_OGGETTI, "Preparazione", sp3, rPrep, statCollector
        );

        // 4. X-Ray (MSMQ) - Nota: Erlang
        ServiceProcess sp4 = new ServiceProcess(
                new ErlangGenerator(MEAN_S4, K4),
                rngs, STREAM_S4_SERVICE
        );
        FlowAssignmentPolicy rrPolicy = new RoundRobinPolicy();
        xRayCenter = new MultiServerMultiQueue(
                ID_XRAY, "XRay", sp4, rXRay, statCollector, M4, rrPolicy
        );

        // 5. Trace Detection
        ServiceProcess sp5 = new ServiceProcess(
                new TruncatedNormalGenerator(MEAN_S5, STD_S5, LB5, UB5),
                rngs, STREAM_S5_SERVICE
        );
        traceCenter = new SingleServerSingleQueue(
                ID_TRACE_DETECTION , "TraceDetection", sp5, rTrace, statCollector
        );

        // 6. Recupero Oggetti
        ServiceProcess sp6 = new ServiceProcess(
                new TruncatedNormalGenerator(MEAN_S6, STD_S6, LB6, UB6),
                rngs, STREAM_S6_SERVICE
        );
        recCenter = new InfiniteServer(
                ID_RECUPERO_OGGETTI, "Recupero", sp6, rRecupero, statCollector
        );
    }

    private void collectAllCenters() {
        centers.add(checkInCenter);
        centers.add(varchiCenter);
        centers.add(prepCenter);
        centers.add(xRayCenter);
        centers.add(traceCenter);
        centers.add(recCenter);
    }

    /* called from the runner */
	public void planNextArrival() {
		// calculate when a pax arrives (update sarrival)
		double nextArrivalTime = arrivalProcess.getArrival();

		// create the job associated to the pax
		Job newJob = new Job(nextArrivalTime);

		// initial routing: Check-in o Varchi?
		Center firstCenter = rIngresso.getNextCenter(this.rngs, newJob);

		// create event
		Event arrivalEvent = new Event(
				nextArrivalTime,
				EventType.ARRIVAL,
				firstCenter,
				newJob,
				null
		);

		eventQueue.add(arrivalEvent);
	}

	/* this should be moved to the runner */
    // run for a defined time
    public void run(double simulationTime) {

        statCollector.clear();
        eventQueue.clear();

        // first arrival
        planNextArrival();

        // Next-Event loop
        while (eventQueue.getCurrentClock() < simulationTime) {
            if (eventQueue.isEmpty()) {
                break;
            }

            // extract the upcoming event and process
            Event e = eventQueue.pop();

            // new arrival
            if (e.getType() == EventType.ARRIVAL) {
                if (e.getTime() == e.getJob().getArrivalTime()) {
                    planNextArrival();
                }
            }

            processEvent(e);
        }
    }
    /*--------------------------------*/

    /* called from the runner */
    public void processEvent(Event e) {
        // center that manages the event
        Center target = e.getTargetCenter();

        // if target == null --> exit job from the system
        if (target == null) {
            recordSystemExit(e);
            return;
        }

        switch (e.getType()) {
            case ARRIVAL:
                target.onArrival(e, eventQueue);
                break;
            case DEPARTURE:
                target.onDeparture(e, eventQueue);
                break;
            case SAMPLING:
                target.onSampling(e, eventQueue);
                break;
            default:
                throw new IllegalStateException("Tipo evento sconosciuto: " + e.getType());
        }
    }

    private void recordSystemExit(Event e) {
        Job job = e.getJob();
        double exitTime = e.getTime();
        double arrivalTime = job.getArrivalTime();

        double responseTime = exitTime - arrivalTime;

        // save statistic "Tempo di Risposta Aeroporto"
        statCollector.addSample("SystemResponseTime", responseTime);

        /* DA VALUTARE SE HA SENSO
           OPPURE COMUNQUE DISTINGUERE TRA
            - SystemResponseTime_Failed
            - SystemResponseTime (normale senza Success)
        if (job.isSecurityCheckFailed()) {
            statCollector.addSample("SystemResponseTime_Failed", responseTime);
        } else {
            statCollector.addSample("SystemResponseTime_Success", responseTime);
        }
         */
    }

    public List<Center> getCenters() {
        return centers;
    }
}
