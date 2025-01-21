package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.Tuple;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.ConsoleColors;
import bgu.spl.mics.MicroService;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private Camera camera;
    List<Tuple<Integer, StampedDetectedObjects>> detectedObjects;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera, CountDownLatch latch, String name) {
        super("Camera " + camera.getId(), latch);
        this.camera = camera;
        detectedObjects = new CopyOnWriteArrayList<Tuple<Integer, StampedDetectedObjects>>();
    }

    public Camera getCamera() {
        return camera;
    }

    public List<Tuple<Integer, StampedDetectedObjects>> getDetectedObjects() {
        return detectedObjects;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        // Register to TickBroadcasts
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            latch= tickBroadcast.getLatch();
            int tick = tickBroadcast.getTick();

            if (tick == -1) {
                latch.countDown();
                terminate();
            } 

            else {
                System.out.println(ConsoleColors.CYAN+"CameraService: Tick received: "+ConsoleColors.RESET + tick);
                StampedDetectedObjects stamped = null;

                try{ // camera.getStatus()==STATUS.ERROR
                    stamped = camera.getDetectedObjects(tick);
                }
                catch (InterruptedException e){
                    System.out.println(ConsoleColors.CYAN+"CameraService: CameraService " + getName() + " crashed due to an error: "+ConsoleColors.RESET + e.getMessage());
                    StatisticalFolder.getInstance().setCrashedOccured(true, getName(), e.getMessage());
                    sendBroadcast(new CrashedBroadcast(getName()));
                    StatisticalFolder.getInstance().incementOffCameraServiceCounter();
                    latch.countDown();
                    terminate();
                }

                

                if(camera.getStatus()==STATUS.DOWN){ // Change
                    StatisticalFolder.getInstance().incementOffCameraServiceCounter();
                    sendBroadcast(new TerminatedBroadcast(getName()));
                    latch.countDown();
                    terminate();
                }

                else if(stamped!=null&&stamped.getDetectedObjects()!=null&&!stamped.getDetectedObjects().isEmpty()){
                    System.out.println(ConsoleColors.CYAN+"Detected " + stamped.getDetectedObjects().size() + " objects"+ConsoleColors.RESET);
                    StatisticalFolder.getInstance().addManyDetectedObject(stamped.getDetectedObjects().size());
                    addCorrectTimeDO(stamped);
                }

                for(int i = 0; i < detectedObjects.size(); i++){
                    Tuple<Integer, StampedDetectedObjects> tuple = detectedObjects.get(i);
                    if(tuple.getFirst() <= tick){
                        StatisticalFolder.getInstance().updateLastCameraFrame(getName(), tuple.getSecond());
                        detectedObjects.remove(i);
                        i--;
                        sendEvent(new DetectObjectsEvent(tuple.getSecond(), tuple.getFirst(), getName()));
                    }
                }
            }

            if (tickBroadcast.getLatch() != null) {
                tickBroadcast.getLatch().countDown();
                System.out.println(ConsoleColors.CYAN+getName() + ": Acknowledged Tick "+ConsoleColors.RESET + tick);
            }

        });
        // subscribeBroadcast(TerminatedBroadcast.class , termBroad -> {
        //     StatisticalFolder.getInstance().incementOffCameraServiceCounter();
        //     latch.countDown();
        //     terminate();
        // });
        subscribeBroadcast(FinishRunBroadcast.class, (finishRunBroadcast) -> {
            // Terminate the service when the FinishRunBroadcast is received
            StatisticalFolder.getInstance().incementOffCameraServiceCounter();
            latch.countDown();
            terminate();
        });
        subscribeBroadcast(CrashedBroadcast.class , crashBroad ->  {
            StatisticalFolder.getInstance().incementOffCameraServiceCounter();
            camera.setStatus(STATUS.ERROR);
            latch.countDown();
            terminate();
        });

        if (latch != null) {
            latch.countDown();
            System.out.println(ConsoleColors.CYAN+getName() + ": Initialization complete, counted down global latch."+ConsoleColors.RESET);
        }
         // Count down the latch after initialization
    }

    public void addCorrectTimeDO(StampedDetectedObjects stampedDetectedObjects){
        detectedObjects.add(new Tuple<Integer, StampedDetectedObjects>(stampedDetectedObjects.getTime() + camera.getFrequency(), stampedDetectedObjects));
    }

    public void addCorrectTimeDO(int time){
        StampedDetectedObjects stampedDetectedObjects = null;
        try{
            stampedDetectedObjects = camera.getDetectedObjects(time);
        }
        catch (InterruptedException e){
            System.out.println(ConsoleColors.CYAN+"CameraService: CameraService " + getName() + " crashed due to an error: "+ConsoleColors.RESET + e.getMessage());
        }
        if(stampedDetectedObjects==null){
            return;
        }
        StatisticalFolder.getInstance().addManyDetectedObject(stampedDetectedObjects.getDetectedObjects().size());
        detectedObjects.add(new Tuple<Integer, StampedDetectedObjects>(stampedDetectedObjects.getTime() + camera.getFrequency(), stampedDetectedObjects));
    }
}
