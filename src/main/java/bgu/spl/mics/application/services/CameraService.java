package bgu.spl.mics.application.services;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.MessageBusImpl;

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
    public CameraService(Camera camera) {
        super("CameraService");
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
                // Detect objects and send DetectObjectsEvents
                camera.detectObjects(tick).forEach(object -> {
                    sendEvent(new DetectObjectsEvent(object));
                });
            }
        });
    }
}
