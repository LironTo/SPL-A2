package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.Map;

public class SimulationErrorOutPut {
    public String error;
    public String faultySensor;
    public Map<String, StampedDetectedObjects> lastCamerasFrame;
    public Map<String, List<TrackedObject>> lastLiDarWorkerTrackersFrame;
    public List<Pose> poses;
    public Statistics statistics;

    public static class Statistics {
        public int systemRuntime;
        public int numDetectedObjects;
        public int numTrackedObjects;
        public int numLandmarks;
        public Map<String, Landmark> landMarks;
    }

    public static class Landmark {
        public String id;
        public String description;
        public List<CloudPoint> coordinates;

        public Landmark(String id, String description, List<CloudPoint> coordinates) {
            this.id = id;
            this.description = description;
            this.coordinates = coordinates;
        }
    }
}
