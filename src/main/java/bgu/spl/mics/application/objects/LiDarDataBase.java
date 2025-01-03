package bgu.spl.mics.application.objects;

import java.util.List;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import com.google.gson.Gson;

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
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
    
            // Read the JSON file content
            SCPWrapper[] stampedCloudPoints = gson.fromJson(reader, SCPWrapper[].class);

            for (SCPWrapper scp : stampedCloudPoints) {
                List<CloudPoint> CPS = new ArrayList<CloudPoint>();
                for (List<String> point : scp.cloudPoints) {
                    CPS.add(new CloudPoint(Double.parseDouble(point.get(0)), Double.parseDouble(point.get(1))));
                }
                this.cloudPoints.add(new StampedCloudPoints(scp.time, scp.id, CPS));
            }
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
