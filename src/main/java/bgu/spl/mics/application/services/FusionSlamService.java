package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*; // Ensure PoseEvent is in this package or update the package path
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.List;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.MicroService;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private FusionSlam fusionSlam;
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
    super("FusionSlamService");
        this.fusionSlam = fusionSlam;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeEvent(TrackedObjectsEvent.class, trackedObjectsEvent -> {
            List<TrackedObject> trackedObjects = trackedObjectsEvent.getSerials();
            System.out.println("FusionSlamService: Received " + trackedObjects.size() + " tracked objects.");
            for (TrackedObject obj : trackedObjects) {
                System.out.println("FusionSlamService: Tracked object - ID: " + obj.getId() + 
                    ", Description: " + obj.getDescription());
            }
            fusionSlam.updateMap(trackedObjects);
        });

        subscribeEvent(PoseEvent.class, poseEvent -> {
            Pose pose = poseEvent.getPosition();
            fusionSlam.addPose(pose);
        });

        subscribeBroadcast(TickBroadcast.class , tickBroadcast -> {
            int tick = tickBroadcast.getTick();
            if (tick == -1) {
                terminate();
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class , termBroad -> {
            terminate();
        });

        subscribeBroadcast(CrashedBroadcast.class , crashBroad ->  {
            terminate();
        });
    }
}
