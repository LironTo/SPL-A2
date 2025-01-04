package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*;

import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;

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
            int tick = tickBroadcast.getTick();
            if (tick == -1) {
                // Terminate the service when the tick indicates shutdown
                terminate();
            } else {
                Pose pose = gpsimu.getPose(tick);
                if (pose != null) {
                    // Send the PoseEvent if the pose is available
                    PoseEvent poseEvent = new PoseEvent(pose);
                    sendEvent(poseEvent);
                }
            }
            if (tickBroadcast.getLatch() != null) {
                tickBroadcast.getLatch().countDown();
                System.out.println(getName() + ": Acknowledged Tick " + tick);
            }
        });
        subscribeBroadcast(TerminatedBroadcast.class , termBroad -> {
            gpsimu.setStatus(STATUS.DOWN);
            terminate();
        });

        subscribeBroadcast(CrashedBroadcast.class , crashBroad ->  {
            gpsimu.setStatus(STATUS.ERROR);
            terminate();
        });
         // Count down the latch after initialization
    if (latch != null) {
        latch.countDown();
    }
    }
}
