package bgu.spl.mics.application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.SimulationOutput;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.SimulationErrorOutPut;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.config;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * The main entry point for the simulation.
 */
public class GurionRockRunner {

    public static void main(String[] args) {
        args=new String[]{"C:\\Users\\chene\\OneDrive\\Documents\\3rd Semester\\SPL\\SPL-A2\\example_input\\configuration_file.json"};
        if (args.length < 1) {
            System.err.println("Please provide the path to the configuration file as the first argument.");
            System.exit(1);
        }

        Gson gson = new Gson();
        System.out.println("Starting simulation with configuration file: "+ args[0]);
        config configuration;

        // Use Path to manage file paths
        Path configFilePath = Paths.get(args[0]);
        Path folderAddress = configFilePath.getParent();
        Path outputFilePath = configFilePath.resolveSibling("output.json"); // Output file in the same directory

        // Step 1: Parse Configuration File
        try (FileReader reader = new FileReader(configFilePath.toFile())) {
            configuration = gson.fromJson(reader, config.class);
            if (configuration == null) {
                throw new NullPointerException("Configuration file is empty or invalid.");
            }
            System.out.println("Configuration loaded successfully.");
        } catch (Exception e) {
            System.err.println("Error loading configuration file: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Step 2: Initialize LiDarDataBase
        try {
            configuration.getLidarWorkers().setLidarsDataPathPrev(folderAddress.toString());
            LiDarDataBase.getInstance(configuration.getLidarWorkers().getLidarsDataPath());
            System.out.println("LiDarDataBase initialized with path: " + configuration.getLidarWorkers().getLidarsDataPath());
        } catch (Exception e) {
            System.err.println("Error initializing LiDarDataBase: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Step 3: Initialize Services
        List<Thread> threads = new ArrayList<>();

        // CountDownLatch to synchronize initialization
        int numberOfServices = config.calculateNumberOfServices(configuration);
        StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();
        statisticalFolder.setCameras(config.getNumberOfCameras(configuration));
        statisticalFolder.setLidarservicesCounter(config.getNumberOfLidarWorkers(configuration));
        CountDownLatch initLatch = new CountDownLatch(numberOfServices);

        try {
            initializeFusionSlamService(threads, initLatch);
            initializeCameraServices(configuration, folderAddress, threads, initLatch);
            initializeLiDarServices(configuration, threads, initLatch);
            initializePoseService(configuration, folderAddress, threads, initLatch);
        } catch (Exception e) {
            System.err.println("Error initializing services: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Step 4: Start All Threads
        for (Thread thread : threads) {
            thread.start();
        }
        try {
            initializeTimeService(configuration, threads, initLatch, numberOfServices);
            threads.get(threads.size() - 1).start();
        } catch (Exception e) {
            System.err.println("Error initializing TimeService: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("All threads have finished.");

        // Step 6: Generate Output
        try {
            generateOutput(outputFilePath.toString());
            System.out.println("Simulation output written to: " + outputFilePath);
        } catch (Exception e) {
            System.err.println("Error generating output: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeCameraServices(config configuration, Path folderAddress, List<Thread> threads, CountDownLatch initLatch) {
        configuration.getCameras().setCameraDatasPathPrev(folderAddress.toString());
        Map<String, List<StampedDetectedObjects>> cameraData = null;

        try (FileReader reader = new FileReader(configuration.getCameras().getCameraDatasPath())) {
            Type mapType = new TypeToken<Map<String, List<StampedDetectedObjects>>>() {}.getType();
            cameraData = new Gson().fromJson(reader, mapType);
            System.out.println("Loaded camera data successfully.");
        } catch (IOException e) {
            System.err.println("Failed to load camera data: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        for (config.CameraConfiguration camConfig : configuration.getCameras().getCamerasConfigurations()) {
            Camera camera = new Camera(camConfig.getId(), camConfig.getFrequency());
            String cameraKey = camConfig.getCameraKey();

            if (cameraData != null && cameraData.containsKey(cameraKey)) {
                List<StampedDetectedObjects> detectedObjects = cameraData.get(cameraKey);

                System.out.println("Loading detected objects for camera: " + cameraKey);
                for (StampedDetectedObjects stampedDetectedObjects : detectedObjects) {
                    camera.addStampedDetectedObject(stampedDetectedObjects);
                }
                System.out.println("Assigned detected objects to camera: " + cameraKey);
            } else {
                System.out.println("No detected objects found for camera: " + cameraKey);
            }

            CameraService cameraService = new CameraService(camera, initLatch, cameraKey);
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

    private static void initializePoseService(config configuration, Path folderAddress, List<Thread> threads, CountDownLatch initLatch) {
        configuration.setPoseJsonFilePrev(folderAddress.toString());
        GPSIMU gpsimu = GPSIMU.getInstance(configuration.getPoseJsonFile());
        PoseService poseService = new PoseService(gpsimu, initLatch);
        threads.add(new Thread(poseService));
    }

    private static void initializeTimeService(config configuration, List<Thread> threads, CountDownLatch initLatch, int numberOfServices) {
        TimeService timeService = new TimeService(configuration.getTickTime(), configuration.getDuration(), initLatch, numberOfServices);
        threads.add(new Thread(timeService));
    }

    private static void generateOutput(String outputFilePath) {
        if (StatisticalFolder.getInstance().isCrashedOccured()) {
            generateErrorOutput(outputFilePath);
        } else {
            generateSuccessOutput(outputFilePath);
        }
    }

    private static void generateSuccessOutput(String outputFilePath) {
        SimulationOutput output = new SimulationOutput();

        // Collect statistics
        output.systemRuntime = StatisticalFolder.getInstance().getSystemRuntime();
        output.numDetectedObjects = StatisticalFolder.getInstance().getNumDetectedObjects();
        output.numTrackedObjects = StatisticalFolder.getInstance().getNumTrackedObjects();
        output.numLandmarks = StatisticalFolder.getInstance().getNumLandmarks();

        FusionSlam fusionSlam = FusionSlam.getInstance();
        List<LandMark> landmarks = fusionSlam.getLandmarks();

        for (LandMark landmark : landmarks) {
            if (landmark == null || landmark.getCoordinates() == null) {
                System.err.println("Skipped a null landmark or one with null coordinates.");
                continue;
            }

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
            System.err.println("Error writing output file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateErrorOutput(String outputFilePath) {
        SimulationErrorOutPut errorOutput = new SimulationErrorOutPut();

        // Collect error details
        errorOutput.error = StatisticalFolder.getInstance().getError();
        errorOutput.faultySensor = StatisticalFolder.getInstance().getFaultySensor();

        // Get last frames
        errorOutput.lastCamerasFrame = StatisticalFolder.getInstance().getLastCameraFrames();
        errorOutput.lastLiDarWorkerTrackersFrame = StatisticalFolder.getInstance().getLastLiDarFrames();

        // Get poses
        errorOutput.poses = StatisticalFolder.getInstance().getRobotPoses();

        SimulationErrorOutPut.Statistics stats = new SimulationErrorOutPut.Statistics();
        stats.systemRuntime = StatisticalFolder.getInstance().getSystemRuntime();
        stats.numDetectedObjects = StatisticalFolder.getInstance().getNumDetectedObjects();
        stats.numTrackedObjects = StatisticalFolder.getInstance().getNumTrackedObjects();
        stats.numLandmarks = StatisticalFolder.getInstance().getNumLandmarks();

        stats.landMarks = new HashMap<>();
        List<LandMark> landmarks = FusionSlam.getInstance().getLandmarks();
        for (LandMark landmark : landmarks) {
            if (landmark != null) {
                stats.landMarks.put(
                    landmark.getId(),
                    new SimulationErrorOutPut.Landmark(
                        landmark.getId(),
                        landmark.getDescription(),
                        landmark.getCoordinates()
                    )
                );
            }
        }

        errorOutput.statistics = stats;

        try (FileWriter writer = new FileWriter(outputFilePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(errorOutput, writer);
        } catch (IOException e) {
            System.err.println("Error writing error output file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
