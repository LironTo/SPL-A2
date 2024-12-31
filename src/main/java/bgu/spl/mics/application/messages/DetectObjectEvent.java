package bgu.spl.mics.application.messages;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import bgu.spl.mics.Event;
import java.util.Collections;

public class DetectObjectEvent<T> implements Event<T> {
    private final int cameraId;
    private final int time;
    private final List<String> detectedObjects;

    public DetectObjectEvent(int cameraId, int time, List<String> detectedObjects) {
        this.cameraId = cameraId;
        this.time = time;
        this.detectedObjects = Collections.unmodifiableList(new CopyOnWriteArrayList<>(detectedObjects));
    }

    public int getCameraId() {
        return cameraId;
    }

    public int getTime() {
        return time;
    }

    public List<String> getDetectedObjects() {
        return detectedObjects;
    }
}
