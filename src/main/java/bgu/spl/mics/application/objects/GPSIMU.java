package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {

    private int currentTick;
    private STATUS status;
    private List<Pose> poseList;

    public GPSIMU() {
        this.currentTick = 0;
        this.status = STATUS.UP;
        this.poseList = new ArrayList<>();
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<Pose> getPoseList() {
        return poseList;
    }

    public Pose getCurrentPose() {
        return poseList.isEmpty() ? null : poseList.get(poseList.size() - 1);
    }

    public void addPose(Pose pose) {
        this.poseList.add(pose);
    }

    /**
     * Retrieves the Pose for a specific tick.
     * @param tick The tick to search for.
     * @return The Pose corresponding to the tick, or null if not found.
     */
    public Pose getPose(int tick) {
        for (Pose pose : poseList) {
            if (pose.getTime() == tick) {
                return pose;
            }
        }
        return null;
    }
}
