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
            LandMark newLandmark = null;
            if (lmIndex == -1) {
                newLandmark = new LandMark(trackedObject.getId(), trackedObject.getDescription());
            } else {
                newLandmark = landmarks.get(lmIndex);
            }

            List<CloudPoint> coordinates = trackedObject.getCoordinates();
            for (CloudPoint coordinate : coordinates) {
                addCoordinateToLandmark(newLandmark, coordinate, trackedObject.getTime());
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
        new Thread(()->{
            Pose pose = checkIfPoseExists(time);
            while (pose == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pose = checkIfPoseExists(time);
            }
            landmark.addCoordinate(correctedCP(coordinate, pose));
        }).start();
    }

    private Pose checkIfPoseExists(int time) {
        for (Pose pose : poses) {
            if (pose.getTime() == time) {
                return pose;
            }
        }
        return null;
    }

    public void addPose(Pose pose) {
        poses.add(pose);
    }

    public CloudPoint correctedCP(CloudPoint cp, Pose pose) {
        double theta_rad = Math.toRadians(pose.getYaw());
        double sin_theta = Math.sin(theta_rad);
        double cos_theta = Math.cos(theta_rad);
        double x_local = cp.getX();
        double y_local = cp.getY();
        double x_robot = pose.getX();
        double y_robot = pose.getY();
        double x_global = cos_theta * x_local - sin_theta * y_local + x_robot;
        double y_global = sin_theta * x_local + cos_theta * y_local + y_robot;
        return new CloudPoint(x_global, y_global);
    }
}
