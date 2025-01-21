package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.mics.ConsoleColors;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    public static final StatisticalFolder instance = new StatisticalFolder();
    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;
    private int CameraServiceCounter;
    private int OffCameraServiceCounter=0;
    private int LidarServiceCounter;
    private int OffLidarServiceCounter=0;
    private final ConcurrentHashMap<String, StampedDetectedObjects> lastCameraFrame = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<TrackedObject>> lastLiDarFrame = new ConcurrentHashMap<>();
    private List<Pose> robotPoses = new ArrayList<>();
    private String faultySensor;
    private String error;
    private boolean CrashedOccured=false;
    private boolean isFusionTerminated=false;
    private boolean isPoseTerminated=false;

    public StatisticalFolder() {
        this.systemRuntime = 0;
        this.numDetectedObjects = 0;
        this.numTrackedObjects = 0;
        this.numLandmarks = 0;
    }
    public void setLidarservicesCounter(int num) { LidarServiceCounter=num; }
    public void incementOffLidarServiceCounter() { OffLidarServiceCounter++; 
        System.out.println(ConsoleColors.BROWN+"incrementing lidar off num to "+ConsoleColors.RESET + OffLidarServiceCounter);}
    public void setPoseTerminated(boolean b) { isPoseTerminated=b;
    System.out.println(ConsoleColors.BROWN+"setting pose termination to"+ConsoleColors.RESET+ b); }
    public void setCrashedOccured(boolean b, String faultySensor, String error) { CrashedOccured=b; this.faultySensor=faultySensor; this.error=error; }
    public String getFaultySensor() { return faultySensor; }
    public String getError() { return error; }
    public boolean isCrashedOccured() { return CrashedOccured; }
    public boolean isPoseTerminated() { return isPoseTerminated; }
    public void setFusionTerminated(boolean b) { isFusionTerminated=b; }
    public boolean isFusionTerminated() { return isFusionTerminated; }
    public boolean isLidarServiceTerminated() { return LidarServiceCounter<=OffLidarServiceCounter; }
    public int getSystemRuntime() { return systemRuntime; }
    public int getNumDetectedObjects() { return numDetectedObjects; }
    public int getNumTrackedObjects() { return numTrackedObjects; }
    public int getNumLandmarks() { return numLandmarks; }
    public void setCameras(int num) { CameraServiceCounter=num; }
    public void incementOffCameraServiceCounter() { OffCameraServiceCounter++;
     System.out.println(ConsoleColors.BROWN+"incrementing camera off num to "+ConsoleColors.RESET + OffCameraServiceCounter); }    
    public boolean isCameraServiceTerminated() { return CameraServiceCounter<=OffCameraServiceCounter; }
    public void addOneSystemRuntime() { 
        System.out.println(ConsoleColors.BROWN+"Adding one system runtime"+ConsoleColors.RESET);
        systemRuntime++; }
    public void addOneDetectedObject() { numDetectedObjects++; }
    public void addManyDetectedObject(int num) { 
        System.out.println(ConsoleColors.BROWN+"Adding "+ConsoleColors.RESET + num + " detected objects");
        this.numDetectedObjects+= num; }
    public void addManyTrackedObject(int num) { this.numTrackedObjects+= num; }
    public void addOneTrackedObject() { numTrackedObjects++; }
    public void addOneLandmark() { numLandmarks++; }
     // Last Frame Management
     public void updateLastCameraFrame(String cameraId, StampedDetectedObjects frame) { //document it in camera service
        lastCameraFrame.put(cameraId, frame); // Replace the previous frame
    }

    public void updateLastLiDarFrame(String workerId, List<TrackedObject> trackedObjects) { //document it in lidar service
        lastLiDarFrame.put(workerId, trackedObjects);
    }

    public ConcurrentHashMap<String, List<TrackedObject>> getLastLiDarFrames() {
        return new ConcurrentHashMap<>(lastLiDarFrame); // Return a snapshot for safety
    }

    public ConcurrentHashMap<String, StampedDetectedObjects> getLastCameraFrames() {
        return new ConcurrentHashMap<>(lastCameraFrame); // Return a snapshot for safety
    }
    // Add a new pose
    public synchronized void addRobotPose(Pose pose) { //document the poses in poseservice
         robotPoses.add(pose);
    }

    // Get all recorded poses
    public synchronized List<Pose> getRobotPoses() {
          return new ArrayList<>(robotPoses); // Return a copy to ensure thread safety
    }   


    public static StatisticalFolder getInstance() { return instance; }

    public int getNumOfActiveServices(){
        int sum = CameraServiceCounter - OffCameraServiceCounter;
        sum += LidarServiceCounter - OffLidarServiceCounter;
        if(!isPoseTerminated) sum += 1;
        if(!isFusionTerminated) sum += 1;
        return sum;
    }

}
