package bgu.spl.mics.application.objects;
import java.util.LinkedList;
import java.util.List;
/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private List<LandMark> landmarks;
    private List<Pose> poses;

    private FusionSlam() {
        this.landmarks = new LinkedList<>();
        this.poses = new LinkedList<>();
    }

    // Singleton instance holder
    private static class FusionSlamHolder {
        public static final FusionSlam instance = new FusionSlam();
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }

    public List<LandMark> getLandmarks() { return landmarks; }
    public List<Pose> getPoses() { return poses; }

    public void updateMap(List<TrackedObject> trackedObjects) {
        for (TrackedObject trackedObject : trackedObjects) {
            int lmIndex = checkIfLMExists(trackedObject.getId());
            if (lmIndex == -1) {
                LandMark newLandmark = new LandMark(trackedObject.getId(), trackedObject.getDescription());
                List<CloudPoint> coordinates = trackedObject.getCoordinates();
                for (CloudPoint coordinate : coordinates) {
                    newLandmark.addCoordinate(coordinate);
                }
                landmarks.add(newLandmark);
            } else {
                landmarks.get(lmIndex).addCoordinate(trackedObject.getCoordinate());
            }
        }
    }

    private int checkIfLMExists(String id) {
        for (int i = 0; i < landmarks.size(); i++) {
            if (landmarks.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private void addCoordinateToLandmark(LandMark landmark, CloudPoint coordinate, int time) {
        Runnable addCoordinate = () -> {
            landmark.addCoordinate(coordinate);
        };
    }

    private boolean checkIfPoseExists(int time) {
        for (Pose pose : poses) {
            if (pose.getTime() == time) {
                return true;
            }
        }
        return false;
    }

    public void updatePose(Pose pose) {
        poses.add(pose);
    }
}
