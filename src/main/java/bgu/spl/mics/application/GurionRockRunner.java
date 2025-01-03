package bgu.spl.mics.application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.objects.Camera;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.SimulationOutput;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.objects.StampedDetectedObjects;  // Update this to the correct class if it exists
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.GPSIMU;  // Assuming the GPSIMU class is in this package
import bgu.spl.mics.application.objects.LandMark;
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
        // Step 5: Wait for all threads to complete
    for (Thread thread : threads) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Step 6: Generate the output file
    String outputFilePath = "./example input/output_file.json";
    new GurionRockRunner().generateOutput(outputFilePath);
    }
    
    private void generateOutput(String outputFilePath) {
        SimulationOutput output = new SimulationOutput();
    
        // Set statistics
        output.systemRuntime = StatisticalFolder.getInstance().getSystemRuntime();
        output.numDetectedObjects = StatisticalFolder.getInstance().getNumDetectedObjects();
        output.numTrackedObjects = StatisticalFolder.getInstance().getNumTrackedObjects();
        output.numLandmarks = StatisticalFolder.getInstance().getNumLandmarks();
    
        // Add landmarks
        FusionSlam fusionSlam = FusionSlam.getInstance();
        List<LandMark> landmarks = fusionSlam.getLandmarks();
        for (LandMark landmark : landmarks) {
            SimulationOutput.Landmark outputLandmark = new SimulationOutput.Landmark(
                landmark.getId(),
                landmark.getDescription(),
                landmark.getCoordinates() // Ensure this matches the expected type in SimulationOutput
            );
            output.landMarks.put(landmark.getId(), outputLandmark);
        }
    
        // Write to JSON file
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(output, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

}
