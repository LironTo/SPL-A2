package bgu.spl.mics.application.objects;

import java.util.List;

public class config {
    private Cameras Cameras;
    private LidarWorkers LiDarWorkers;
    private String poseJsonFile;
    private int TickTime;
    private int Duration;

    public Cameras getCameras() { return Cameras; }
    public LidarWorkers getLidarWorkers() { return LiDarWorkers; }
    public String getPoseJsonFile() { return poseJsonFile; }
    public int getTickTime() { return TickTime; }
    public int getDuration() { return Duration; }
    public void setPoseJsonFilePrev(String path) { this.poseJsonFile = path + poseJsonFile.substring(1); }

    public static class Cameras {
        private List<CameraConfiguration> CamerasConfigurations;
        private String camera_datas_path;

        public List<CameraConfiguration> getCamerasConfigurations() { return CamerasConfigurations; }
        public String getCameraDatasPath() { return camera_datas_path; }
        public void setCameraDatasPathPrev(String path) { this.camera_datas_path = path + camera_datas_path.substring(1); }
    }

    public static class LidarWorkers {
        private List<LidarConfiguration> LidarConfigurations;
        private String lidars_data_path;

        public List<LidarConfiguration> getLidarConfigurations() { return LidarConfigurations; }
        public String getLidarsDataPath() { return lidars_data_path; }
        public void setLidarsDataPathPrev(String path) { this.lidars_data_path = path + lidars_data_path.substring(1); }
    }

    public static class CameraConfiguration {
        private int id;
        private int frequency;
        private String camera_key;

        public int getId() { return id; }
        public int getFrequency() { return frequency; }
        public String getCameraKey() { return camera_key; }
    }

    public static class LidarConfiguration {
        private int id;
        private int frequency;

        public int getId() { return id; }
        public int getFrequency() { return frequency; }
    }
    public static int getNumberOfCameras(config configuration) {
        return configuration.getCameras().getCamerasConfigurations().size();
    }
    public static int getNumberOfLidarWorkers(config configuration) {
        return configuration.getLidarWorkers().getLidarConfigurations().size();
    }
    public static int calculateNumberOfServices(config configuration) {
        int cameraServices = configuration.getCameras().getCamerasConfigurations().size();
        int lidarServices = configuration.getLidarWorkers().getLidarConfigurations().size();
        return cameraServices + lidarServices + 2;
    }
}
