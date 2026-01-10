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
import mbpmcsn.desbook.Rngs;
import mbpmcsn.event.EventQueue;
import mbpmcsn.process.ArrivalProcess;
import mbpmcsn.stats.StatCollector;
import java.util.ArrayList;
import java.util.List;

import static mbpmcsn.core.Constants.*;

/**
 * represents the simulation mind that initializes the model topology,
 * instantiates the centers and generators, and controls the
 * next-event time advance loop
 */

public final class SimulationModel {

    /* core components */
    private final EventQueue eventQueue;
    private final StatCollector statCollector;
    private final Rngs rngs;

    /* arrival and centers */
    private ArrivalProcess arrivalProcess;
    private MultiServerSingleQueue checkInCenter;    // ID 1
    private MultiServerMultiQueue varchiCenter;      // ID 2
    private InfiniteServer prepCenter;               // ID 3
    private MultiServerMultiQueue xrayCenter;        // ID 4
    private SingleServerSingleQueue traceCenter;     // ID 5
    private InfiniteServer recCenter;           // ID 6

    private final List<Center> centers = new ArrayList<>();

    /* routing */
    private EntryRouting rIngresso;
    private FixedRouting rCheckIn;
    private FixedRouting rVarchi;
    private FixedRouting rPrep;
    private XRayRouting rXray;
    private TraceRouting rTrace;
    private FixedRouting rRecupero;

    public SimulationModel() {
        this.rngs = new Rngs();
        this.rngs.plantSeeds(SEED); // initializing global seed
        this.eventQueue = new EventQueue();
        this.statCollector = new StatCollector();

        setupNetwork();
    }

    // constructor of the network
    private void setupNetwork() {
        createRoutingLogic();  // setting routing
        createCenters(); // creating centers
        connectNetwork(); // connecting centers
    }

    private void createRoutingLogic() {
        this.rIngresso = new EntryRouting();
        this.rCheckIn  = new FixedRouting();
        this.rVarchi   = new FixedRouting();
        this.rPrep     = new FixedRouting();
        this.rXray     = new XRayRouting();
        this.rTrace    = new TraceRouting();
        this.rRecupero = new FixedRouting();
    }

    private void createCenters() {

        // --- Setup Arrivals Generator  ---
        this.arrivalProcess = new ArrivalProcess(
                new ExponentialGenerator(ARRIVAL_MEAN_TIME),
                rngs, STREAM_ARRIVALS
        );
        /**
         * --- Setup Policies per le code ---
         * SQF for elettronic gates
         * Round Robin for XRay
         */
        FlowAssignmentPolicy sqfPolicy = new SqfPolicy(rngs, STREAM_S2_ROUTING);
        FlowAssignmentPolicy rrPolicy = new RoundRobinPolicy();

        // --- Cretion centers ---

        // 1. Check-in
        ServiceProcess sp1 = new ServiceProcess(
                new TruncatedNormalGenerator(MEAN_S1, STD_S1, LB1, UB1),
                rngs, STREAM_S1_SERVICE
        );
        this.checkInCenter = new MultiServerSingleQueue(
                ID_BANCHI_CHECKIN, "CheckIn", sp1, rCheckIn, statCollector, M1
        );

        // 2. Varchi (MSMQ)
        ServiceProcess sp2 = new ServiceProcess(
                new TruncatedNormalGenerator(MEAN_S2, STD_S2, LB2, UB2),
                rngs, STREAM_S2_SERVICE
        );
        this.varchiCenter = new MultiServerMultiQueue(
                ID_VARCHI_ELETTRONICI, "Varchi", sp2, rVarchi, statCollector, M2, sqfPolicy
        );

        // 3. Preparazione
        ServiceProcess sp3 = new ServiceProcess(
                new TruncatedNormalGenerator(MEAN_S3, STD_S3, LB3, UB3),
                rngs, STREAM_S3_SERVICE
        );
        this.prepCenter = new InfiniteServer(
                ID_PREPARAZIONE_OGGETTI, "Preparazione", sp3, rPrep, statCollector
        );

        // 4. X-Ray (MSMQ) - Nota: Erlang
        ServiceProcess sp4 = new ServiceProcess(
                new ErlangGenerator(MEAN_S4, K4),
                rngs, STREAM_S4_SERVICE
        );
        this.xrayCenter = new MultiServerMultiQueue(
                ID_XRAY, "XRay", sp4, rXray, statCollector, M4, rrPolicy
        );

        // 5. Trace Detection
        ServiceProcess sp5 = new ServiceProcess(
                new TruncatedNormalGenerator(MEAN_S5, STD_S5, LB5, UB5),
                rngs, STREAM_S5_SERVICE
        );
        this.traceCenter = new SingleServerSingleQueue(
                ID_TRACE_DETECTION , "TraceDetection", sp5, rTrace, statCollector
        );

        // 6. Recupero Oggetti
        ServiceProcess sp6 = new ServiceProcess(
                new TruncatedNormalGenerator(MEAN_S6, STD_S6, LB6, UB6),
                rngs, STREAM_S6_SERVICE
        );
        this.recCenter = new InfiniteServer(
                ID_RECUPERO_OGGETTI, "Recupero", sp6, rRecupero, statCollector
        );

        centers.add(checkInCenter);
        centers.add(varchiCenter);
        centers.add(prepCenter);
        centers.add(xrayCenter);
        centers.add(traceCenter);
        centers.add(recCenter);
    }

    private void connectNetwork() {
        rIngresso.setDestinations(checkInCenter, varchiCenter);
        rCheckIn.setDestination(varchiCenter);
        rVarchi.setDestination(prepCenter);
        rPrep.setDestination(xrayCenter);
        rXray.setDestinations(traceCenter, recCenter);
        rTrace.setDestination(recCenter);
        rRecupero.setDestination(null);
    }

    // run for a defined time
    public void run(double simulationTime) {

        statCollector.clear();
        eventQueue.clear();

        // first arrival
        arrivalProcess.planNextArrival(eventQueue, 0.0, rIngresso);

        // Next-Event loop
        while (eventQueue.getCurrentClock() < simulationTime) {

            if (eventQueue.isEmpty()) {
                break;
            }

            // extract the upcoming event and process
            mbpmcsn.event.Event e = eventQueue.pop();

            // new arrival
            if (e.getType() == EventType.ARRIVAL) {
                if (e.getTime() == e.getJob().getArrivalTime()) {
                    // new passegger
                    arrivalProcess.planNextArrival(eventQueue, eventQueue.getCurrentClock(), rIngresso);
                }
            }

            processEvent(e);
        }
    }

    private void processEvent(mbpmcsn.event.Event e) {
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

    public StatCollector getStatCollector() {
        return statCollector;
    }

    public List<Center> getCenters() {
        return centers;
    }


}

