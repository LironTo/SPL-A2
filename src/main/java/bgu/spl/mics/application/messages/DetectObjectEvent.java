package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class DetectObjectEvent implements Event<Boolean> {
    private final String sender;
    private final int time;
    private final int cameraFreq;
    private final StampedDetectedObjects detectedObjects;

    public DetectObjectEvent( StampedDetectedObjects detectedObjects, String senderName, int cameraFreq) {
        this.sender = senderName;
        this.time = detectedObjects.getTime();
        this.detectedObjects = detectedObjects;
        this.cameraFreq = cameraFreq;
    }
    public String getSender() {
        return sender;
    }

    public int getCameraFreq() {
        return cameraFreq;
    }

    public int getTime() {
        return time;
    }

    public StampedDetectedObjects getDetectedObjects() {
        return detectedObjects;
    }
}
