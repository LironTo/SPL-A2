package bgu.spl.mics.application.objects;
import java.util.LinkedList;
import java.util.List;
/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private static FusionSlamHolder instance;
    private List<LandMark> landmarks;
    private List<Pose> poses;

    public FusionSlam() {
        this.instance = new FusionSlamHolder();
        this.landmarks = new LinkedList<>();
        this.poses = new LinkedList<>();
    }

    // Singleton instance holder
    private static class FusionSlamHolder {
        
    }
}
