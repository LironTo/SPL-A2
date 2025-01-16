package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Tuple;

import java.util.ArrayList;
import java.util.List;
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

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker, CountDownLatch latch) {
        super("LiDarTrackerWorker"+LiDarWorkerTracker.getId(), latch);
        this.liderworkertracker = LiDarWorkerTracker;
        this.allocTimeSDObjects = new ArrayList<Tuple<Integer, StampedDetectedObjects>>();
    }

    public LiDarWorkerTracker getLiDarWorkerTracker() {
        return liderworkertracker;
    }

    public List<Tuple<Integer, StampedDetectedObjects>> getAllocTimeSDObjects() {
        return allocTimeSDObjects;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */

    @Override
    protected void initialize() {

        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent detectObjectsEvent) -> {
            System.out.println("LiDarWorkerService: Received DetectObjectEvent at time: " + detectObjectsEvent.getProcessedTime());
            System.out.println("Detected objects size: " + detectObjectsEvent.getStampedDetectedObjects().getDetectedObjects().size());

            int processedTimeByLiDar = detectObjectsEvent.getStampedDetectedObjects().getTime() + liderworkertracker.getFrequency();
            if(detectObjectsEvent.getProcessedTime() >= processedTimeByLiDar){
                int index = allocTimeSDObjects.size();
                allocTimeSDObjects.add(new Tuple<Integer, StampedDetectedObjects>(detectObjectsEvent.getProcessedTime(), detectObjectsEvent.getStampedDetectedObjects()));
                sendEventByIndex(index);
            } else {
                allocTimeSDObjects.add(new Tuple<Integer, StampedDetectedObjects>(processedTimeByLiDar, detectObjectsEvent.getStampedDetectedObjects()));
            }

            StatisticalFolder.getInstance().addManyTrackedObject(detectObjectsEvent.getStampedDetectedObjects().getDetectedObjects().size());
            complete(detectObjectsEvent, null);
        });
        
    
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            latch= tickBroadcast.getLatch();
            int tick = tickBroadcast.getTick();

            if (tick == -1) {
                latch.countDown();
                terminate();
            } 
            
            else 
            {
                if(liderworkertracker.getStatus()==STATUS.UP){
                    System.out.println("LiDarWorkerService: Tick received: " + tick);
                    for (int i = 0; i < allocTimeSDObjects.size(); i++) {
                        if (allocTimeSDObjects.get(i).getFirst() <= tick) {
                            sendEventByIndex(i);
                            i--;
                        }
                    }
                    if (tickBroadcast.getLatch() != null) {
                        tickBroadcast.getLatch().countDown();
                        System.out.println(getName() + ": Acknowledged Tick " + tick);
                    }
                }
        
                else if(liderworkertracker.getStatus()==STATUS.ERROR){
                    StatisticalFolder.getInstance().setCrashedOccured(true, getName(), "Connection to LiDAR lost");
                    sendBroadcast(new CrashedBroadcast(getName()));
                    latch.countDown();
                    terminate();
                }

                else if(liderworkertracker.getStatus()==STATUS.DOWN){
                    StatisticalFolder.getInstance().incementOffLidarServiceCounter();
                    sendBroadcast(new TerminatedBroadcast(getName()));
                    latch.countDown();
                    terminate();
                }
            }
        });
        
    
        subscribeBroadcast(TerminatedBroadcast.class, termBroad -> {
            if(termBroad.getTerminatedName().toLowerCase().contains("camera")) {
                if(StatisticalFolder.getInstance().isCameraServiceTerminated()&&allocTimeSDObjects.isEmpty()) {
                    StatisticalFolder.getInstance().incementOffLidarServiceCounter();
                    latch.countDown();
                    liderworkertracker.setStatus(STATUS.DOWN);
                    sendBroadcast(new TerminatedBroadcast(getName()));}
            }
        });
        subscribeBroadcast(FinishRunBroadcast.class, (finishRunBroadcast) -> {
            // Terminate the service when the FinishRunBroadcast is received
            latch.countDown();
            terminate();
        });
        subscribeBroadcast(CrashedBroadcast.class, crashBroad -> {
            StatisticalFolder.getInstance().incementOffLidarServiceCounter();
            latch.countDown();
            terminate();
        });
        if (latch != null) {
            latch.countDown();
            System.out.println(getName() + ": Initialization complete, counted down global latch.");
        }
         // Count down the latch after initialization
    }

    public void sendEventByIndex(int index){
        StampedDetectedObjects stampedDetectedObjects = allocTimeSDObjects.get(index).getSecond();
        List<TrackedObject> trackedObjects = liderworkertracker.processData(stampedDetectedObjects.getTime(), stampedDetectedObjects.getDetectedObjects());
        StatisticalFolder.getInstance().updateLastLiDarFrame(getName(), trackedObjects);
        TrackedObjectsEvent trackedObjectsEvent = new TrackedObjectsEvent(trackedObjects, getName());
        allocTimeSDObjects.remove(index);
        sendEvent(trackedObjectsEvent);
    }

    public void addStampedDetectedObject(int time, StampedDetectedObjects stampedDetectedObjects){
        if(stampedDetectedObjects != null){
            allocTimeSDObjects.add(new Tuple<Integer, StampedDetectedObjects>(stampedDetectedObjects.getTime(), stampedDetectedObjects));
        }
    }
}