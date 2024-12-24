package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    // TODO: Define fields and methods.
    private int cameraId;
    private int frequency;
    private List<StampedDetectedObjects> detectedObjectsList = new CopyOnWriteArrayList<>();
    public enum CameraStatus {
        UP, DOWN, ERROR
    }
}
