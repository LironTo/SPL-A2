package bgu.spl.mics.application.objects;

import java.util.List;

public class config {
    private Cameras cameras;
    private LidarWorkers lidarWorkers;
    private String poseJsonFile;
    private int tickTime;
    private int duration;

    public Cameras getCameras() { return cameras; }
    public LidarWorkers getLidarWorkers() { return lidarWorkers; }
    public String getPoseJsonFile() { return poseJsonFile; }
    public int getTickTime() { return tickTime; }
    public int getDuration() { return duration; }

    public static class Cameras {
        private List<CameraConfiguration> camerasConfigurations;
        private String cameraDatasPath;

        public List<CameraConfiguration> getCamerasConfigurations() { return camerasConfigurations; }
        public String getCameraDatasPath() { return cameraDatasPath; }
    }

    public static class LidarWorkers {
        private List<LidarConfiguration> lidarConfigurations;
        private String lidarsDataPath;

        public List<LidarConfiguration> getLidarConfigurations() { return lidarConfigurations; }
        public String getLidarsDataPath() { return lidarsDataPath; }
    }

    public static class CameraConfiguration {
        private int id;
        private int frequency;
        private String cameraKey;

        public int getId() { return id; }
        public int getFrequency() { return frequency; }
        public String getCameraKey() { return cameraKey; }
    }

    public static class LidarConfiguration {
        private int id;
        private int frequency;

        public int getId() { return id; }
        public int getFrequency() { return frequency; }
    }
}
