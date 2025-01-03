package bgu.spl.mics.application.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationOutput {
    public int systemRuntime;
    public int numDetectedObjects;
    public int numTrackedObjects;
    public int numLandmarks;
    public Map<String, Landmark> landMarks = new HashMap<>(); // Map for landmark entries
    public ErrorDetails error; // Include this only if there's an error

    public static class Landmark {
        public String id;
        public String description;
        public List<CloudPoint> coordinates; // Replace CloudPoint with appropriate class for points

        public Landmark(String id, String description, List<CloudPoint> coordinates) {
            this.id = id;
            this.description = description;
            this.coordinates = coordinates;
        }
    }

    public static class ErrorDetails {
        public String error;
        public String faultySensor;
        public Frames lastFrames;
        public List<Pose> poses;

        public static class Frames {
            public List<StampedDetectedObjects> cameras;
            public List<StampedCloudPoints> lidar;
        }
    }
}
