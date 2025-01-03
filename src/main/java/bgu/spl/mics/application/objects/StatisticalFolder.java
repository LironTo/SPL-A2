package bgu.spl.mics.application.objects;

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

    public StatisticalFolder() {
        this.systemRuntime = 0;
        this.numDetectedObjects = 0;
        this.numTrackedObjects = 0;
        this.numLandmarks = 0;
    }

    public int getSystemRuntime() { return systemRuntime; }
    public int getNumDetectedObjects() { return numDetectedObjects; }
    public int getNumTrackedObjects() { return numTrackedObjects; }
    public int getNumLandmarks() { return numLandmarks; }

    public void addOneSystemRuntime() { systemRuntime++; }
    public void addOneDetectedObject() { numDetectedObjects++; }
    public void addOneTrackedObject() { numTrackedObjects++; }
    public void addOneLandmark() { numLandmarks++; }

    public StatisticalFolder getInstance() { return instance; }

}
