package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.MicroService;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private int TickTime;
    private int Duration;
    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.TickTime = TickTime;
        this.Duration = Duration;
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

    // Start ticking directly in this thread
    for (int tick = 0; tick <= Duration; tick++) {
        // Broadcast the current tick
        StatisticalFolder.getInstance().addOneSystemRuntime();
        sendBroadcast(new TickBroadcast(tick));

        // Sleep for the tick duration
        try {
            Thread.sleep(TickTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Respect interruptions
            break; // Exit the loop gracefully
        }

        // Terminate the service when the final tick is reached
        if (tick == Duration) {
            sendBroadcast(new TickBroadcast(-1)); // Indicate end of simulation
            terminate();
        }
    }
}
}