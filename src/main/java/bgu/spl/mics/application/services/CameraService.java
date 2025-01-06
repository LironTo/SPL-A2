package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.MicroService;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private Camera camera;
    

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera, CountDownLatch latch) {
        super("CameraService", latch);
        this.camera = camera;

       
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        // Register to TickBroadcasts
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            latch= tickBroadcast.getLatch();
            int tick = tickBroadcast.getTick();
            if (tick == -1) {
                terminate();
            } else {
                System.out.println("CameraService: Tick received: " + tick);
                StampedDetectedObjects stamped = camera.getDetectedObjects(tick);
                if(camera.getStatus()==STATUS.ERROR){ //only after the methos the status may change
                    StatisticalFolder.getInstance().setCrashedOccured(true, getName());
                    sendBroadcast(new CrashedBroadcast(getName()));
                    terminate();
                }
                else if(camera.getStatus()==STATUS.DOWN){
                    StatisticalFolder.getInstance().incementOffCameraServiceCounter();
                    sendBroadcast(new TerminatedBroadcast(getName()));
                    terminate();
                }
               else if(stamped!=null&&stamped.getDetectedObjects()!=null&&!stamped.getDetectedObjects().isEmpty()){
                    StatisticalFolder.getInstance().updateLastCameraFrame(getName(), stamped);
                    System.out.println("Detected " + stamped.getDetectedObjects().size() + " objects");
                    StatisticalFolder.getInstance().addManyDetectedObject(stamped.getDetectedObjects().size());
                    int freq= this.camera.getFrequency();
                    StatisticalFolder.getInstance().updateLastCameraFrame(getName(), stamped);
                    sendEvent(new DetectObjectEvent(stamped, getName(), freq));
                }
            }
        if (tickBroadcast.getLatch() != null) {
            tickBroadcast.getLatch().countDown();
            System.out.println(getName() + ": Acknowledged Tick " + tick);
        }
        });
        subscribeBroadcast(TerminatedBroadcast.class , termBroad -> {
        });
        subscribeBroadcast(FinishRunBroadcast.class, (finishRunBroadcast) -> {
            // Terminate the service when the FinishRunBroadcast is received
            terminate();
        });

        subscribeBroadcast(CrashedBroadcast.class , crashBroad ->  {
            //StatisticalFolder.getInstance().setCrashedOccured(true, getName());
           // camera.setStatus(STATUS.ERROR);
            terminate();
        });
        if (latch != null) {
            latch.countDown();
            System.out.println(getName() + ": Initialization complete, counted down global latch.");
        }
         // Count down the latch after initialization
    }
}
