package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
            instance = new LiDarDataBase();
        return instance;

    }

    public void dump(){
        cloudPoints.clear();
    }

    public List<StampedCloudPoints> getSCloudPoints() {return this.cloudPoints;}

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
        for(int i = cloudPoints.size()-1; i >= 0; i--) {
            if(cloudPoints.get(i).getId().equals(id) && cloudPoints.get(i).getTime() <= time){
                return cloudPoints.get(i).getCloudPoints();
            }
        }
        return null;
    }

    public StampedCloudPoints getSCP(String id, int time){
        for (StampedCloudPoints stampedCloudPoints : cloudPoints) {
            if(stampedCloudPoints.getId().equals(id) && stampedCloudPoints.getTime() == time){
                return stampedCloudPoints;
            }
        }
        return null;
    }

    public List<StampedCloudPoints> getAllSCP(int time){
        List<StampedCloudPoints> list = new CopyOnWriteArrayList<>();
        for (StampedCloudPoints stampedCloudPoints : cloudPoints) {
            if(stampedCloudPoints.getTime() == time) {list.add(stampedCloudPoints);}
        }
        return list;
    }

    public int getLatestTime(String id, int time) {
        for(int i = cloudPoints.size()-1; i >= 0; i--) {
            if(cloudPoints.get(i).getId().equals(id) && cloudPoints.get(i).getTime() <= time){
                return cloudPoints.get(i).getTime();
            }
        }
        return -1;
    }

    public void addStampedCloudPoints(StampedCloudPoints stampedCloudPoints) {
        cloudPoints.add(stampedCloudPoints);
    }
}
