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
            int tick = tickBroadcast.getTick();
            if (tick == -1) {
                terminate();
            } else {
                System.out.println("CameraService: Tick received: " + tick);
                StampedDetectedObjects stamped = camera.getDetectedObjects(tick);
                if(stamped!=null&&stamped.getDetectedObjects()!=null&&!stamped.getDetectedObjects().isEmpty()){
                    System.out.println("Detected " + stamped.getDetectedObjects().size() + " objects");
                    StatisticalFolder.getInstance().addManyDetectedObject(stamped.getDetectedObjects().size());
                    sendEvent(new DetectObjectEvent(stamped, getName()));
                }
            }
            if (tickBroadcast.getLatch() != null) {
                tickBroadcast.getLatch().countDown();
                System.out.println(getName() + ": Acknowledged Tick " + tick);
            }
        });
        subscribeBroadcast(TerminatedBroadcast.class , termBroad -> {
            camera.setStatus(STATUS.DOWN);
            terminate();
        });

        subscribeBroadcast(CrashedBroadcast.class , crashBroad ->  {
            camera.setStatus(STATUS.ERROR);
            terminate();
        });
         // Count down the latch after initialization
    if (latch != null) {
        latch.countDown();
    }
    }
}
