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
import java.util.concurrent.CountDownLatch;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.SimulationOutput;
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
        String outputFilePath = "./example_output.json"; // Path for output JSON

        // Step 1: Parse Configuration File
        try (FileReader reader = new FileReader(folderAddress + "configuration_file.json")) {
            configuration = gson.fromJson(reader, config.class);
            System.out.println("Configuration loaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Step 2: Initialize LiDarDataBase
        configuration.getLidarWorkers().setLidarsDataPathPrev(folderAddress);
        LiDarDataBase lidarDatabase = LiDarDataBase.getInstance(configuration.getLidarWorkers().getLidarsDataPath());
        System.out.println("LiDarDataBase initialized with path: " + configuration.getLidarWorkers().getLidarsDataPath());

        // Step 3: Initialize Services
        List<Thread> threads = new ArrayList<>();

        // CountDownLatch to synchronize initialization
        int numberOfServices = config.calculateNumberOfServices(configuration);
        // Update with the number of services
        CountDownLatch initLatch = new CountDownLatch(numberOfServices);

        // Initialize each service
        initializeCameraServices(configuration, folderAddress, threads, initLatch);
        initializeLiDarServices(configuration, threads, initLatch);
        initializeFusionSlamService(threads, initLatch);
        initializePoseService(configuration, folderAddress, threads, initLatch);
        // Initialize TimeService

        // Step 4: Start All Threads
        for (Thread thread : threads) {
            thread.start();
        }
       Thread timeservice= new Thread(initializeTimeService(configuration, threads, initLatch, numberOfServices));
        timeservice.start();


        // Step 5: Wait for Threads to Finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Step 6: Generate Output
        generateOutput(outputFilePath);
        System.out.println("Simulation output written to: " + outputFilePath);
    }

    private static void initializeCameraServices(config configuration, String folderAddress, List<Thread> threads, CountDownLatch initLatch) {
        configuration.getCameras().setCameraDatasPathPrev(folderAddress);
        Map<String, List<StampedDetectedObjects>> cameraData = null;

        // Load camera data
        try (FileReader reader = new FileReader(configuration.getCameras().getCameraDatasPath())) {
            Type mapType = new TypeToken<Map<String, List<StampedDetectedObjects>>>() {}.getType();
            cameraData = new Gson().fromJson(reader, mapType);
            System.out.println("Loaded camera data successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load camera data.");
            return;
        }

        // Initialize cameras
        for (config.CameraConfiguration camConfig : configuration.getCameras().getCamerasConfigurations()) {
            Camera camera = new Camera(camConfig.getId(), camConfig.getFrequency());
            String cameraKey = camConfig.getCameraKey();

            if (cameraData != null && cameraData.containsKey(cameraKey)) {
                List<StampedDetectedObjects> detectedObjects = cameraData.get(cameraKey);

                System.out.println("Loading detected objects for camera: " + cameraKey);
                for (StampedDetectedObjects stampedDetectedObjects : detectedObjects) {
                    if (stampedDetectedObjects.getDetectedObjects() == null || stampedDetectedObjects.getDetectedObjects().isEmpty()) {
                        System.out.println("Warning: DetectedObjects is null or empty for time: " + stampedDetectedObjects.getTime());
                    } else {
                        System.out.println("Loaded detected objects for time: " + stampedDetectedObjects.getTime() +
                            ", size: " + stampedDetectedObjects.getDetectedObjects().size());
                    }
                    camera.addStampedDetectedObject(stampedDetectedObjects);
                }
                System.out.println("Assigned detected objects to camera: " + cameraKey);
            } else {
                System.out.println("No detected objects found for camera: " + cameraKey);
            }

            CameraService cameraService = new CameraService(camera, initLatch);
            threads.add(new Thread(cameraService));
        }
    }

    private static void initializeLiDarServices(config configuration, List<Thread> threads, CountDownLatch initLatch) {
        for (config.LidarConfiguration lidarConfig : configuration.getLidarWorkers().getLidarConfigurations()) {
            LiDarWorkerTracker lidarWorker = new LiDarWorkerTracker(lidarConfig.getId(), lidarConfig.getFrequency());
            LiDarService lidarService = new LiDarService(lidarWorker, initLatch);
            threads.add(new Thread(lidarService));
        }
    }

    private static void initializeFusionSlamService(List<Thread> threads, CountDownLatch initLatch) {
        FusionSlam fusionSlam = FusionSlam.getInstance();
        FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam, initLatch);
        threads.add(new Thread(fusionSlamService));
    }

    private static void initializePoseService(config configuration, String folderAddress, List<Thread> threads, CountDownLatch initLatch) {
        configuration.setPoseJsonFilePrev(folderAddress);
        GPSIMU gpsimu = GPSIMU.getInstance(configuration.getPoseJsonFile());
        PoseService poseService = new PoseService(gpsimu, initLatch);
        threads.add(new Thread(poseService));
    }

    private static TimeService initializeTimeService(config configuration, List<Thread> threads, CountDownLatch initLatch, int numberOfServices) {
        TimeService timeService = new TimeService(configuration.getTickTime(), configuration.getDuration(), initLatch, numberOfServices);
        return timeService;
    }

    private static void generateOutput(String outputFilePath) {
        SimulationOutput output = new SimulationOutput();

        output.systemRuntime = StatisticalFolder.getInstance().getSystemRuntime();
        output.numDetectedObjects = StatisticalFolder.getInstance().getNumDetectedObjects();
        output.numTrackedObjects = StatisticalFolder.getInstance().getNumTrackedObjects();
        output.numLandmarks = StatisticalFolder.getInstance().getNumLandmarks();

        FusionSlam fusionSlam = FusionSlam.getInstance();
        List<LandMark> landmarks = fusionSlam.getLandmarks();
        for (LandMark landmark : landmarks) {
            SimulationOutput.Landmark outputLandmark = new SimulationOutput.Landmark(
                landmark.getId(),
                landmark.getDescription(),
                landmark.getCoordinates()
            );
            output.landMarks.put(landmark.getId(), outputLandmark);
        }

        try (FileWriter writer = new FileWriter(outputFilePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(output, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
