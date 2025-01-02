package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

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
        this.detectedObjectsList = new LinkedList<StampedDetectedObjects>();
    }

    public int getId() { return id; }
    public int getFrequency() { return frequency; }
    public STATUS getStatus() { return status; }
    public List<StampedDetectedObjects> getDetectedObjectsList() { return detectedObjectsList; }

    public void setStatus(STATUS status) { this.status = status; }
    public void addStampedDetectedObject(StampedDetectedObjects stampedDetectedObjects) { detectedObjectsList.add(stampedDetectedObjects); }

    public StampedDetectedObjects getDetectedObjects(int time) {
        StampedDetectedObjects detectedObjects = null;
        for (StampedDetectedObjects stampedDetectedObjects : detectedObjectsList) {
            if (stampedDetectedObjects.getTime() == time+frequency) {
                detectedObjects = stampedDetectedObjects;
                break;
            }
        }
        return detectedObjects;
    }
}
