package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.MicroService;

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
    private ArrayList<DetectObjectEvent> detectObjectEventsList;
    private LiDarDataBase dataBase;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker, CountDownLatch latch) {
        super("LiDarWorkerService", latch);
        this.liderworkertracker = LiDarWorkerTracker;
        this.detectObjectEventsList = new ArrayList<DetectObjectEvent>();
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
            detectObjectEventsList.add(detectObjectsEvent);
            StatisticalFolder.getInstance().addManyTrackedObject(detectObjectsEvent.getDetectedObjects().getDetectedObjects().size());
            complete(detectObjectsEvent, null);
        });
        
    
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            int tick = tickBroadcast.getTick();
            if (tick == -1) {
                terminate();
            } else {
                System.out.println("LiDarWorkerService: Tick received: " + tick);
                for (DetectObjectEvent detectObjectEvent : detectObjectEventsList) {
                    if (tick >= detectObjectEvent.getTime() + liderworkertracker.getFrequency()) {
                        System.out.println("LiDarWorkerService: Processing DetectObjectEvent for tick: " + tick);
                        List<TrackedObject> tracked = liderworkertracker.processData(
                            tick, 
                            detectObjectEvent.getDetectedObjects().getDetectedObjects(), 
                            dataBase
                        );
                        System.out.println("LiDarWorkerService: Sending TrackedObjectsEvent with " + tracked.size() + " tracked objects.");
                        TrackedObjectsEvent trackedObjectsEvent = new TrackedObjectsEvent(tracked, getName());
                        sendEvent(trackedObjectsEvent);
                        System.out.println("LiDarWorkerService: Sent TrackedObjectsEvent at tick: " + tick);

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
         // Count down the latch after initialization
    if (latch != null) {
        latch.countDown();
    }
    }
}