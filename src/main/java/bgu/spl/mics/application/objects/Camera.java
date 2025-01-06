package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private final int id;
    private final int frequency;
    private STATUS status;
    private List<StampedDetectedObjects> detectedObjectsList;

    public Camera(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.detectedObjectsList = new LinkedList<StampedDetectedObjects>();
        System.out.println("Camera initialized with an empty detectedObjectsList");
    }

    public int getId() { return id; }
    public int getFrequency() { return frequency; }
    public STATUS getStatus() { return status; }
    public List<StampedDetectedObjects> getDetectedObjectsList() {System.out.println("Camera: Retrieving detected objects for tick: " + detectedObjectsList.get(0).getTime());
     return detectedObjectsList; }

    public void setStatus(STATUS status) { this.status = status; }
    public void addStampedDetectedObject(StampedDetectedObjects stampedDetectedObjects) { detectedObjectsList.add(stampedDetectedObjects); }

    public StampedDetectedObjects getDetectedObjects(int time) {
        System.out.println("Searching detected objects for time: " + time + ", frequency: " + frequency);
        for (StampedDetectedObjects stampedDetectedObjects : detectedObjectsList) {
            int objectTime = stampedDetectedObjects.getTime();
            if (objectTime == time || objectTime == time + frequency) {  // Adjust logic here
                System.out.println("Found matching detected object for time: " + time + ", objectTime: " + objectTime);
                return stampedDetectedObjects;
            }
        }
        System.out.println("No matching detected object found for time: " + time);
        return null;
    }
    

}
    
