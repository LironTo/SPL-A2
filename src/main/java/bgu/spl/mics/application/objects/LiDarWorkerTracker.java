package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private int id;
    private int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;

    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
    }

    public int getId() { return id; }
    public int getFrequency() { return frequency; }
    public STATUS getStatus() { return status; }
    public List<TrackedObject> getLastTrackedObjects() { return lastTrackedObjects; }

    public List<TrackedObject> processData(int tick, List<DetectedObject> detectedObjects, LiDarDataBase dataBase) {
        List<TrackedObject> trackedObjects = new ArrayList<TrackedObject>();

        for (DetectedObject detectedObject : detectedObjects) {
            TrackedObject trackedObject = new TrackedObject(detectedObject.getId(), tick, detectedObject.getDescription(), dataBase.getCloudPoints(detectedObject.getId(), tick));
            trackedObjects.add(trackedObject);
        }

        return trackedObjects;
    }
}
