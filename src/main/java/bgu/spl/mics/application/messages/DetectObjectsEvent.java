package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class DetectObjectsEvent implements Event<Boolean> {
    private final String sender;
    private final int processedTime;
    private final StampedDetectedObjects detectedObjects;

    public DetectObjectsEvent(StampedDetectedObjects detectedObjects, int processedTime, String senderName) {
        this.detectedObjects = detectedObjects;
        this.processedTime = processedTime;
        this.sender = senderName;
    }
    public String getSender() {
        return sender;
    }

    public int getProcessedTime() {
        return processedTime;
    }

    public StampedDetectedObjects getStampedDetectedObjects() {
        return detectedObjects;
    }
}
