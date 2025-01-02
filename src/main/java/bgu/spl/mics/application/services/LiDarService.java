package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.messages.DetectObjectEvent;
import bgu.spl.mics.MicroService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LiDarWorkerService");
        this.liderworkertracker = LiDarWorkerTracker;
        this.detectObjectEventsList = new ArrayList<DetectObjectEvent>();
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */

    @Override
    protected void initialize() {
        subscribeEvent(DetectObjectEvent.class, (DetectObjectEvent detectObjectsEvent) -> {
            detectObjectEventsList.add(detectObjectsEvent);
        });
    
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            int tick = tickBroadcast.getTick();
            if (tick == -1) {
                terminate();
            }
            else{
                for (DetectObjectEvent detectObjectEvent : detectObjectEventsList) {
                    if (tick==detectObjectEvent.getTime()+liderworkertracker.getFrequency()) {
                        List<TrackedObject> tracked= liderworkertracker.processData(detectObjectEvent.getDetectedObjects());
                        TrackedObjectsEvent trackedObjectsEvent = new TrackedObjectsEvent(tracked, getName());
                        
                    }
                }
            }
        });
    
        subscribeBroadcast(TerminatedBroadcast.class, termBroad -> {
            terminate();
        });
    
        subscribeBroadcast(CrashedBroadcast.class, crashBroad -> {
            terminate();
        });
    }
}