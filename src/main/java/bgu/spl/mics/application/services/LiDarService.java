package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private LiDarWorkerTracker liderworkertracker;
    private List<Tuple<Integer, StampedDetectedObjects>> allocTimeSDObjects;
    private LiDarDataBase dataBase;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker, CountDownLatch latch) {
        super("LiDarWorkerService", latch);
        this.liderworkertracker = LiDarWorkerTracker;
        this.allocTimeSDObjects = new ArrayList<Tuple<Integer, StampedDetectedObjects>>();
        this.dataBase = LiDarDataBase.getInstance();
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */

    @Override
    protected void initialize() {
        subscribeEvent(DetectObjectEvent.class, (DetectObjectEvent detectObjectsEvent) -> {
            System.out.println("LiDarWorkerService: Received DetectObjectEvent at time: " + detectObjectsEvent.getTime());
            System.out.println("Detected objects size: " + detectObjectsEvent.getDetectedObjects().getDetectedObjects().size());

            allocTimeSDObjects.add(new Tuple<Integer,StampedDetectedObjects>(
                Math.max(detectObjectsEvent.getTime()+detectObjectsEvent.getCameraFreq(), 
                detectObjectsEvent.getTime()+liderworkertracker.getFrequency()), 
                detectObjectsEvent.getDetectedObjects())); // Max{T + F} , StampedDetectedObjects

            StatisticalFolder.getInstance().addManyTrackedObject(detectObjectsEvent.getDetectedObjects().getDetectedObjects().size());
            complete(detectObjectsEvent, null);
        });
        
    
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            latch= tickBroadcast.getLatch();
            int tick = tickBroadcast.getTick();
            if (tick == -1) {
                terminate();
            } else {
                System.out.println("LiDarWorkerService: Tick received: " + tick);
                for (int i = 0; i < allocTimeSDObjects.size(); i++) {
                    if (allocTimeSDObjects.get(i).getFirst() == tick) {
                        StampedDetectedObjects stamped = allocTimeSDObjects.get(i).getSecond();
                        System.out.println("LiDarWorkerService: Sending TrackedObjectsEvent with " + stamped.getDetectedObjects().size() + " objects");
                        List<TrackedObject> trackedObjects = liderworkertracker.processData(tick, stamped.getDetectedObjects(), dataBase);
                        TrackedObjectsEvent trackedObjectsEvent = new TrackedObjectsEvent(trackedObjects, ("LiDar worker " + liderworkertracker.getId()));
                        System.out.println("LiDarWorkerService: Sent TrackedObjectsEvent at tick: " + tick);
                        allocTimeSDObjects.remove(i);
                        i--;
                        sendEvent(trackedObjectsEvent);
                    }
                }
            }
            if (tickBroadcast.getLatch() != null) {
                tickBroadcast.getLatch().countDown();
                System.out.println(getName() + ": Acknowledged Tick " + tick);
            }
        });
        
    
        subscribeBroadcast(TerminatedBroadcast.class, termBroad -> {
            terminate();
        });
    
        subscribeBroadcast(CrashedBroadcast.class, crashBroad -> {
            terminate();
        });
        if (latch != null) {
            latch.countDown();
            System.out.println(getName() + ": Initialization complete, counted down global latch.");
        }
         // Count down the latch after initialization
    }
}