import bgu.spl.mics.*;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CameraLiDarFusionTest {
    private static CameraService cameraService;
    private static LiDarDataBase liDarDataBase;
    private static LiDarService liDarService;
    private static FusionSlamService fusionSlamService;
    private static CountDownLatch latch;

    @BeforeAll
    private static void setUp() {
        latch = new CountDownLatch(1);
        
        Camera camera = new Camera(1, 1);
        cameraService = new CameraService(camera, latch, "camera1");
        List<DetectedObject> detectedObjects = new CopyOnWriteArrayList<DetectedObject>();
        detectedObjects.add(new DetectedObject("shelf_1", "wooden shelf"));
        detectedObjects.add(new DetectedObject("Toy_1", "ball"));
        detectedObjects.add(new DetectedObject("Toy_2", "doll"));
        StampedDetectedObjects stampedDetectedObjects = new StampedDetectedObjects(1, detectedObjects);
        camera.addStampedDetectedObject(stampedDetectedObjects);
        List<DetectedObject> detectedObjects2 = new CopyOnWriteArrayList<DetectedObject>();
        detectedObjects2.add(new DetectedObject("shelf_2", "metal shelf"));
        detectedObjects2.add(new DetectedObject("Toy_1", "ball"));
        StampedDetectedObjects stampedDetectedObjects2 = new StampedDetectedObjects(3, detectedObjects2);
        camera.addStampedDetectedObject(stampedDetectedObjects2);


        liDarDataBase = LiDarDataBase.getInstance();
        liDarDataBase.dump();

        List<CloudPoint> cloudPoints = new CopyOnWriteArrayList<CloudPoint>();
        cloudPoints.add(new CloudPoint(1, 1));
        cloudPoints.add(new CloudPoint(2, 2));
        StampedCloudPoints stampedCloudPoints = new StampedCloudPoints(1, "shelf_1", cloudPoints);
        liDarDataBase.addStampedCloudPoints(stampedCloudPoints);

        List<CloudPoint> cloudPoints2 = new CopyOnWriteArrayList<CloudPoint>();
        cloudPoints2.add(new CloudPoint(3, 3));
        cloudPoints2.add(new CloudPoint(4, 4));
        StampedCloudPoints stampedCloudPoints2 = new StampedCloudPoints(3, "shelf_2", cloudPoints2);
        liDarDataBase.addStampedCloudPoints(stampedCloudPoints2);

        List<CloudPoint> cloudPoints3 = new CopyOnWriteArrayList<CloudPoint>();
        cloudPoints3.add(new CloudPoint(5, 5));
        cloudPoints3.add(new CloudPoint(6, 6));
        StampedCloudPoints stampedCloudPoints3 = new StampedCloudPoints(1, "Toy_1", cloudPoints3);
        liDarDataBase.addStampedCloudPoints(stampedCloudPoints3);

        List<CloudPoint> cloudPoints4 = new CopyOnWriteArrayList<CloudPoint>();
        cloudPoints4.add(new CloudPoint(7, 7));
        cloudPoints4.add(new CloudPoint(8, 8));
        StampedCloudPoints stampedCloudPoints4 = new StampedCloudPoints(1, "Toy_2", cloudPoints4);
        liDarDataBase.addStampedCloudPoints(stampedCloudPoints4);

        List<CloudPoint> cloudPoints5 = new CopyOnWriteArrayList<CloudPoint>();
        cloudPoints5.add(new CloudPoint(3, 3));
        cloudPoints5.add(new CloudPoint(2, 2));
        StampedCloudPoints stampedCloudPoints5 = new StampedCloudPoints(3, "Toy_1", cloudPoints5);
        liDarDataBase.addStampedCloudPoints(stampedCloudPoints5);

        LiDarWorkerTracker liDarWorkerTracker = new LiDarWorkerTracker(1, 2);
        liDarService = new LiDarService(liDarWorkerTracker, latch);

        FusionSlam fusionSlam = FusionSlam.getInstance();
        fusionSlamService = new FusionSlamService(fusionSlam, latch);

        fusionSlam.addPose(new Pose(3, -5, 5.75f, 1));
        fusionSlam.addPose(new Pose(1, 2, 3f, 2));
        fusionSlam.addPose(new Pose(-2, -3, -4f, 3));
    }

    @Test
    private void testCameraService() {
        assertTrue(cameraService.getCamera().getId() == 1);
        assertTrue(cameraService.getCamera().getFrequency() == 1);
        assertTrue(cameraService.getCamera().getStatus() == STATUS.UP);
        assertTrue(cameraService.getCamera().getDetectedObjectsList().size() == 2);
        assertTrue(cameraService.getCamera().getDetectedObjectsList().get(0).getTime() == 1);
        assertTrue(cameraService.getCamera().getDetectedObjectsList().get(1).getTime() == 3);
        assertTrue(cameraService.getCamera().getDetectedObjectsList().get(0).getDetectedObjects().size() == 3);
        assertTrue(cameraService.getCamera().getDetectedObjectsList().get(1).getDetectedObjects().size() == 2); 
        assertTrue(cameraService.getDetectedObjects().size() == 0);
        assertTrue(cameraService.getName().equals("Camera 1"));

        cameraService.addCorrectTimeDO(1);
        assertTrue(cameraService.getDetectedObjects().size() == 1);
        cameraService.addCorrectTimeDO(2);
        assertTrue(cameraService.getDetectedObjects().size() == 1);
        cameraService.addCorrectTimeDO(3);
        assertTrue(cameraService.getDetectedObjects().size() == 2);
    }

    @Test
    private void testLiDarService(){

        assertTrue(liDarService.getLiDarWorkerTracker().getId() == 1);
        assertTrue(liDarService.getLiDarWorkerTracker().getFrequency() == 2);
        assertTrue(liDarService.getLiDarWorkerTracker().getStatus() == STATUS.UP);
        assertTrue(liDarService.getAllocTimeSDObjects().size() == 0);
        assertTrue(liDarService.getName().equals("LiDarTrackerWorker1"));
        assertTrue(liDarService.getLatch().getCount() == 1);
        assertTrue(liDarDataBase.getSCloudPoints().size() == 5);
        
        List<StampedCloudPoints> list = liDarService.getLiDarWorkerTracker().getAllSCP(1);
        assertTrue(list.size() == 3);
        for (StampedCloudPoints stampedCloudPoints : list) {
            liDarService.addStampedDetectedObject(1, getDObySCP(stampedCloudPoints));
        }
        assertTrue(liDarService.getAllocTimeSDObjects().size() == 3);

        list = liDarService.getLiDarWorkerTracker().getAllSCP(3);
        assertTrue(list.size() == 2);
        for (StampedCloudPoints stampedCloudPoints : list) {
            liDarService.addStampedDetectedObject(3, getDObySCP(stampedCloudPoints));
        }
        assertTrue(liDarService.getAllocTimeSDObjects().size() == 5);
    }

    private StampedDetectedObjects getDObySCP(StampedCloudPoints SCP){
        StampedDetectedObjects SDO = null;
        for (Tuple<Integer, StampedDetectedObjects> StampedDetectedObject : cameraService.getDetectedObjects()) {
            for(DetectedObject SD : StampedDetectedObject.getSecond().getDetectedObjects()){
                if(SD.getId() == SCP.getId()) {
                    SDO = StampedDetectedObject.getSecond();
                    break;
                }
            }
        }
        return SDO;
    }

    @Test
    private void testFusionSlam(){
        List<StampedCloudPoints> list = liDarService.getLiDarWorkerTracker().getAllSCP(1);
        list.addAll(liDarService.getLiDarWorkerTracker().getAllSCP(3));
        assertTrue(list.size() == 5);
        List<TrackedObject> trackedObjects = new CopyOnWriteArrayList<TrackedObject>();
        for (StampedCloudPoints stampedCloudPoints : list) {
            trackedObjects.add(getTrackedByDOSCP(stampedCloudPoints));
        }
        assertTrue(trackedObjects.size() == 5);
        fusionSlamService.getFusionSlam().updateMap(trackedObjects);
        assertTrue(fusionSlamService.getFusionSlam().getLandmarks().size() == 5);
        assertTrue(fusionSlamService.getFusionSlam().getPoses().size() == 3);
    }

    private TrackedObject getTrackedByDOSCP(StampedCloudPoints SCP){
        TrackedObject TO = null;
        DetectedObject DO = null;
        StampedDetectedObjects SDO = getDObySCP(SCP); 
        for(DetectedObject SD : SDO.getDetectedObjects()){
            if(SD.getId() == SCP.getId()) {
                DO = SD;
                break;
            }
        }
        if(SDO != null){
            TO = new TrackedObject(DO.getId(), SDO.getTime(), DO.getDescription(), SCP.getCloudPoints());
        }
        return TO;
    }
}
