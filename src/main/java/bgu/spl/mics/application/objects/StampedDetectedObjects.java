package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private final int time;
    private List<DetectedObject> DetectedObjects;

    public StampedDetectedObjects(int time, List<DetectedObject> DetectedObjects) {
        this.time = time;
        this.DetectedObjects = DetectedObjects;
    }

    public int getTime() {
        return time;
    }

    public List<DetectedObject> getDetectedObjects() {
        return DetectedObjects;
    }
}
