package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*; // Ensure PoseEvent is in this package or update the package path
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.ConsoleColors;
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
    public FusionSlamService(FusionSlam fusionSlam, CountDownLatch latch) {
    super("FusionSlamService", latch);
        this.fusionSlam = fusionSlam;
    }

    public FusionSlam getFusionSlam() {
        return fusionSlam;
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
            System.out.println(ConsoleColors.CYAN+"FusionSlamService: Received " + trackedObjects.size() + " tracked objects."+ConsoleColors.RESET);
            for (TrackedObject obj : trackedObjects) {
                System.out.println(ConsoleColors.CYAN+"FusionSlamService: Tracked object - ID: " + obj.getId() + 
                    ", Description: " + obj.getDescription()+ConsoleColors.RESET);
            }
            fusionSlam.updateMap(trackedObjects);
        });

        subscribeEvent(PoseEvent.class, poseEvent -> {
            Pose pose = poseEvent.getPosition();
            fusionSlam.addPose(pose);
        });

        subscribeBroadcast(TickBroadcast.class , tickBroadcast -> {
            latch = tickBroadcast.getLatch();
            int tick = tickBroadcast.getTick();
            if (tick == -1) {
                latch.countDown();
                terminate();
            }
            StatisticalFolder stats = StatisticalFolder.getInstance();
            if(stats.isPoseTerminated() && stats.isCameraServiceTerminated() && stats.isLidarServiceTerminated()){
                System.out.println(ConsoleColors.CYAN+"sending finish broadcast"+ConsoleColors.RESET);
                sendBroadcast(new FinishRunBroadcast(getName()));
                latch.countDown();
                terminate();
            }
            if (tickBroadcast.getLatch() != null) {
                tickBroadcast.getLatch().countDown();
                System.out.println(ConsoleColors.CYAN+getName() + ": Acknowledged Tick "+ConsoleColors.RESET + tick);
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class , termBroad -> {
            StatisticalFolder stats = StatisticalFolder.getInstance();
            if(stats.isPoseTerminated() && stats.isCameraServiceTerminated() && stats.isLidarServiceTerminated()){
                System.out.println(ConsoleColors.CYAN+"sending finish broadcast"+ConsoleColors.RESET);
                sendBroadcast(new FinishRunBroadcast(getName()));
                StatisticalFolder.getInstance().setFusionTerminated(true);
                latch.countDown();
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class , crashBroad ->  {
            StatisticalFolder.getInstance().setFusionTerminated(true);
            latch.countDown();
            terminate();
        });
        if (latch != null) {
            latch.countDown();
            System.out.println(ConsoleColors.CYAN+getName() + ": Initialization complete, counted down global latch."+ConsoleColors.RESET);
        }
         // Count down the latch after initialization
    }
}
