import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CameraLiDarFusionTest {
    private CameraService cameraService;
    private LiDarDataBase liDarDataBase;
    private LiDarService liDarService;
    private FusionSlamService fusionSlamService;
    private CountDownLatch latch;

    @BeforeEach
    void setUp() {
        latch = new CountDownLatch(1);
        
        Camera camera = new Camera(1, 1);
        cameraService = new CameraService(camera, latch);
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
        camera.addStampedDetectedObject(stampedDetectedObjects);


        liDarDataBase = LiDarDataBase.getInstance();

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
    }

    @Test
    public void testCameraService() {
        
    }
}
