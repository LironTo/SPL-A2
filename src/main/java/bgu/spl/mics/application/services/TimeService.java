package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.MicroService;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private int TickTime;
    private int Duration;
    private int numberOfServices;
    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */

    public TimeService(int TickTime, int Duration, CountDownLatch latch, int numberOfServices) {
        super("TimeService", latch);
        this.TickTime = TickTime;
        this.Duration = Duration;
        this.numberOfServices = numberOfServices-1;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        // Subscribe to broadcasts for termination or crashes
        subscribeBroadcast(CrashedBroadcast.class, crashBroadcast -> terminate());
        subscribeBroadcast(TerminatedBroadcast.class, termBroadcast -> terminate());
    
        try {
            System.out.println(getName() + ": Waiting for all services to initialize...");
            latch.await(); // Wait for all services to signal they are ready
            System.out.println(getName() + ": All services initialized. Starting ticks.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
    
        for (int tick = 0; tick <= Duration; tick++) {

            // Create a latch for this tick
            StatisticalFolder.getInstance().addOneSystemRuntime();
            CountDownLatch tickLatch = new CountDownLatch(numberOfServices);
    
            // Share the latch for the current tick
            TickBroadcast tickBroadcast = new TickBroadcast(tick, tickLatch);
            sendBroadcast(tickBroadcast);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            long startTime = System.currentTimeMillis();
            try {
                tickLatch.await(); // Wait for all services to finish their work for this tick
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
    
            // Handle the last tick
            if (tick == Duration) {
                sendBroadcast(new TickBroadcast(-1, null)); // Indicate end of simulation
                terminate();
                break;
            }
    
            // Calculate remaining time for the tick and sleep if necessary
            long elapsedTime = System.currentTimeMillis() - startTime;
            long sleepTime = TickTime - elapsedTime;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    

}