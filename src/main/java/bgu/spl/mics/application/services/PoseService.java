package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*;

import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private GPSIMU gpsimu;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu, CountDownLatch latch) {
        super("PoseService", latch);
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        // Subscribing to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            latch= tickBroadcast.getLatch();
            int tick = tickBroadcast.getTick();
            if (tick == -1) {
                // Terminate the service when the tick indicates shutdown
                latch.countDown();
                terminate();
            } else {
                if(gpsimu.getStatus() == STATUS.UP){
                
                Pose pose = gpsimu.getPose(tick);
                if (pose != null) {
                    // Send the PoseEvent if the pose is available
                    PoseEvent poseEvent = new PoseEvent(pose);
                    StatisticalFolder.getInstance().addRobotPose(pose);
                    sendEvent(poseEvent);
                }
            }
            else{
                StatisticalFolder.getInstance().setPoseTerminated(true);
            }
        }
            if (tickBroadcast.getLatch() != null) {
                tickBroadcast.getLatch().countDown();
                System.out.println(getName() + ": Acknowledged Tick " + tick);
            }
        });
        subscribeBroadcast(FinishRunBroadcast.class, (finishRunBroadcast) -> {
            // Terminate the service when the FinishRunBroadcast is received
            latch.countDown();
            terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class , termBroad -> {  
            StatisticalFolder.getInstance().setPoseTerminated(true);
            latch.countDown();
            terminate();          
        });

        subscribeBroadcast(CrashedBroadcast.class , crashBroad ->  {
            gpsimu.setStatus(STATUS.ERROR);
            StatisticalFolder.getInstance().setPoseTerminated(true);
            latch.countDown();
            terminate();
        });
        if (latch != null) {
            latch.countDown();
            System.out.println(getName() + ": Initialization complete, counted down global latch.");
        }
         // Count down the latch after initialization
    }
}
