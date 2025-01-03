package bgu.spl.mics.application;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.objects.Camera;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.objects.StampedDetectedObjects;  // Update this to the correct class if it exists
import bgu.spl.mics.application.objects.GPSIMU;  // Assuming the GPSIMU class is in this package
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.config;  // Assuming the config class is in this package
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        Gson gson = new Gson();
        config configuration;
        String folderAddress = "./example input/";
    
        // Step 1: Parse the Configuration File
        try (FileReader reader = new FileReader(folderAddress+"configuration_file.json")) {
            configuration = gson.fromJson(reader, config.class);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    
        // Step 2: Initialize LiDarDataBase
        configuration.getLidarWorkers().setLidarsDataPathPrev(folderAddress);
        LiDarDataBase lidarDatabase = LiDarDataBase.getInstance(configuration.getLidarWorkers().getLidarsDataPath());
    
        // Step 3: Initialize Services
        List<Thread> threads = new ArrayList<>();
    
        // Camera Services
        configuration.getCameras().setCameraDatasPathPrev(folderAddress);
        for (config.CameraConfiguration camConfig : configuration.getCameras().getCamerasConfigurations()) {
            Camera camera = new Camera(camConfig.getId(), camConfig.getFrequency());
            CameraService cameraService = new CameraService(camera);
            threads.add(new Thread(cameraService));
        }
    
        // LiDAR Services
        for (config.LidarConfiguration lidarConfig : configuration.getLidarWorkers().getLidarConfigurations()) {
            LiDarWorkerTracker lidarWorker = new LiDarWorkerTracker(lidarConfig.getId(), lidarConfig.getFrequency());
            LiDarService lidarService = new LiDarService(lidarWorker);
            threads.add(new Thread(lidarService));
        }
    
        // FusionSLAM Service
        FusionSlam fusionSlam= FusionSlam.getInstance();
        FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam);
        threads.add(new Thread(fusionSlamService));
    
        // Pose Service
        configuration.setPoseJsonFilePrev(folderAddress);
        GPSIMU gpsimu = GPSIMU.getInstance(configuration.getPoseJsonFile());
        PoseService poseService = new PoseService(gpsimu);
        threads.add(new Thread(poseService));
    
        // Time Service
        TimeService timeService = new TimeService(configuration.getTickTime(), configuration.getDuration());
        threads.add(new Thread(timeService));
    
        // Step 4: Start All Threads
        for (Thread thread : threads) {
            thread.start();
        }
    }
    
    // Utility Method to Parse JSON Files
    private static <T> List<T> parseJson(String filePath, Class<T> clazz) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            Type type = TypeToken.getParameterized(List.class, clazz).getType();
            return gson.fromJson(reader, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
