package bgu.spl.mics.application.objects;

import java.io.Console;
import java.util.LinkedList;
import java.util.List;

import bgu.spl.mics.ConsoleColors;
import bgu.spl.mics.Tuple;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private final int id;
    private final int frequency;
    private STATUS status;
    private int indexCamera=0;
    private List<StampedDetectedObjects> detectedObjectsList;

    public Camera(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.detectedObjectsList = new LinkedList<StampedDetectedObjects>();
        System.out.println(ConsoleColors.LIGHT_GREEN+"Camera initialized with an empty detectedObjectsList"+ConsoleColors.RESET);
    }

    public int getId() { return id; }
    public int getFrequency() { return frequency; }
    public STATUS getStatus() { return status; }
    public List<StampedDetectedObjects> getDetectedObjectsList() {System.out.println(ConsoleColors.LIGHT_GREEN+"Camera: Retrieving detected objects for tick: "+ConsoleColors.RESET + detectedObjectsList.get(0).getTime());
     return detectedObjectsList; }

    public void setStatus(STATUS status) { this.status = status; }
    public void addStampedDetectedObject(StampedDetectedObjects stampedDetectedObjects) { detectedObjectsList.add(stampedDetectedObjects); }

    public StampedDetectedObjects getDetectedObjects(int time) throws InterruptedException {
        if(indexCamera==detectedObjectsList.size()){
            status=STATUS.DOWN;
        }
        
        System.out.println(ConsoleColors.LIGHT_GREEN+"Searching detected objects for time: "+ConsoleColors.RESET + time + ", frequency: " + frequency);
        for (StampedDetectedObjects stampedDetectedObjects : detectedObjectsList) {
            int objectTime = stampedDetectedObjects.getTime();
            if (objectTime == time) {
                Tuple<Boolean, String> msgIfError = stampedDetectedObjects.isError();
                if(!msgIfError.getFirst()){
                    System.out.println(ConsoleColors.LIGHT_GREEN+"Found matching detected object for time: "+ConsoleColors.RESET + time + ", objectTime: " + objectTime);
                    indexCamera++;
                    return stampedDetectedObjects;
                }
                else {
                    status=STATUS.ERROR;
                    throw new InterruptedException(msgIfError.getSecond());
                } 
            }
        }
        System.out.println(ConsoleColors.LIGHT_GREEN+"No matching detected object found for time: "+ConsoleColors.RESET + time);
        return null;
    }
    

}
    
