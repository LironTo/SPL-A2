package bgu.spl.mics.application.objects;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.util.ArrayList;
import java.lang.reflect.Type;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private static GPSIMU instance;
    private static String path;
    private int currentTick;
    private STATUS status;
    private List<Pose> poseList;

    private GPSIMU() {
        poseList = new ArrayList<>();
    }

    public static synchronized GPSIMU getInstance(String poseJsonFile) {
        if (poseJsonFile == null) {
            throw new IllegalArgumentException("poseJsonFile cannot be null.");
        }
        if (path == null) {
            path = poseJsonFile;
            instance = new GPSIMU();
            instance.loadPoses();
        } else if (!path.equals(poseJsonFile)) {
            throw new IllegalStateException("GPSIMU has already been initialized with a different JSON file path: " + path);
        }
        return instance;
    }

    public static GPSIMU getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GPSIMU must be initialized with a JSON file path first.");
        }
        return instance;
    }

    private void loadPoses() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(path)) {
            Type type = new TypeToken<List<Pose>>() {}.getType();
            this.poseList = gson.fromJson(reader, type);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load poses from file: " + path);
        }
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<Pose> getPoseList() {
        return poseList;
    }

    public Pose getCurrentPose() {
        return poseList.isEmpty() ? null : poseList.get(poseList.size() - 1);
    }

    public Pose getPose(int tick) {
        for (Pose pose : poseList) {
            if (pose.getTime() == tick) {
                return pose;
            }
        }
        return null;
    }
}