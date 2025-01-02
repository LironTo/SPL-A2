package bgu.spl.mics.application.objects;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private List<StampedCloudPoints> cloudPoints;
    private static LiDarDataBase instance;

    private LiDarDataBase() {
        cloudPoints = new ArrayList<StampedCloudPoints>();
    }
    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        if (instance == null) {
            instance = new LiDarDataBase();
            instance.loadData(filePath);
        }
        return instance;
    }
    public static LiDarDataBase getInstance() {
        if(instance == null)
            throw new IllegalStateException("LiDarDataBase was not initialized");
        return instance;

    }

    private void loadData(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            cloudPoints = objectMapper.readValue(new File(filePath), objectMapper.getTypeFactory().constructCollectionType(List.class, StampedCloudPoints.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<CloudPoint> getCloudPoints(String id, int time) {
        for (StampedCloudPoints stampedCloudPoints : cloudPoints) {
            if (stampedCloudPoints.getId().equals(id) && stampedCloudPoints.getTime() == time) {
                return stampedCloudPoints.getCloudPoints();
            }
        }
        return null;
    }
}
