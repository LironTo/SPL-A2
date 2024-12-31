package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    // TODO: Define fields and methods.
    private final int id;
    private final int frequency;
    private STATUS status;
    private List<StampedDetectedObjects> detectedObjectsList;

    public Camera(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.detectedObjectsList = new CopyOnWriteArrayList<>();
    }

    public int getId() { return id; }
    public int getFrequency() { return frequency; }
    public STATUS getStatus() { return status; }
    public List<StampedDetectedObjects> getDetectedObjectsList() { return detectedObjectsList; }

    public void setStatus(STATUS status) { this.status = status; }
    public void addStampedDetectedObject(StampedDetectedObjects stampedDetectedObjects) { detectedObjectsList.add(stampedDetectedObjects); }

    public List<DetectedObject> getDetectedObjects(int time) {
        List<DetectedObject> detectedObjects = new LinkedList<DetectedObject>();
        for (StampedDetectedObjects stampedDetectedObjects : detectedObjectsList) {
            if (stampedDetectedObjects.getTime() == time) {
                detectedObjects = stampedDetectedObjects.getDetectedObjects();
            }
        }
        return detectedObjects;
    }
}
