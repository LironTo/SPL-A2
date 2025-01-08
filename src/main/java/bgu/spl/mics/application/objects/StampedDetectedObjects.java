package bgu.spl.mics.application.objects;

import bgu.spl.mics.Tuple;
import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private final int time;
    private List<DetectedObject> detectedObjects;

    public StampedDetectedObjects(int time, List<DetectedObject> DetectedObjects) {
        this.time = time;
        this.detectedObjects = DetectedObjects;
    }

    public int getTime() {
        return time;
    }

    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }
    public Tuple<Boolean, String> isError(){
        for(DetectedObject detectedObject: detectedObjects){
            if(detectedObject.getId().equals("ERROR")){
                return new Tuple<Boolean, String>(true, detectedObject.getDescription());
            }
        }
        return new Tuple<Boolean, String>(false, null);
    }
}
