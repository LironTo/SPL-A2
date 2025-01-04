package bgu.spl.mics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	private static volatile MessageBusImpl instance;
	private ConcurrentHashMap<MicroService, BlockingQueue<Message>> microServiceQueue = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Class<? extends Event>, BlockingQueue<MicroService>> eventQueue = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Class<? extends Broadcast>, CopyOnWriteArrayList<MicroService>> broadcastQueue= new ConcurrentHashMap<>();
	private ConcurrentHashMap<Event, Future> futureMap = new ConcurrentHashMap<>();


    private MessageBusImpl() {
        // Initialize any required resources here
    }
	public static MessageBusImpl getInstance() {
        if (instance == null) { // First check (no locking)
            synchronized (MessageBusImpl.class) {
                if (instance == null) { // Second check (with locking)
                    instance = new MessageBusImpl();
                }
            }
        }
        return instance;
    }
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// Ensure thread-safe initialization of the event queue
		eventQueue.computeIfAbsent(type, k -> new java.util.concurrent.LinkedBlockingQueue<>());
	
		// Add the MicroService to the queue of subscribers for the given event type
		eventQueue.get(type).add(m); // Use add instead of put
	}
	
	

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// Ensure thread-safe initialization of the broadcast queue
		broadcastQueue.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>());
	
		// Add the MicroService to the queue of subscribers for the given broadcast type
		broadcastQueue.get(type).add(m); // Use add instead of put

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// Get the future associated with the event
		Future<T> future = futureMap.get(e);
		if (future == null) {
			throw new IllegalStateException("Failed to complete event: " + e + " - no future found");
		}
	
		// Resolve the future with the result
		future.resolve(result);

	}

@Override
public void sendBroadcast(Broadcast b) {
    // Ensure thread-safe initialization of the broadcast subscriber list
    broadcastQueue.computeIfAbsent(b.getClass(), k -> new CopyOnWriteArrayList<>());

    // Get the list of MicroServices subscribed to this broadcast type
    CopyOnWriteArrayList<MicroService> subscribers = broadcastQueue.get(b.getClass());

    // Send the broadcast message to all subscribed MicroServices
    if (subscribers != null) {
        for (MicroService m : subscribers) {
            // Ensure the MicroService has a message queue
            microServiceQueue.computeIfAbsent(m, k -> new LinkedBlockingQueue<>());

            try {
                // Add the broadcast message to the MicroService's queue
                microServiceQueue.get(m).put(b); // Using put to block if the queue is full
                System.out.println("Broadcast " + b.getClass().getSimpleName() + " sent to " + m.getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                System.err.println("Failed to send broadcast to " + m.getName());
            }
        }
    } else {
        System.out.println("No subscribers for broadcast " + b.getClass().getSimpleName());
    }
}



	
@Override
public <T> Future<T> sendEvent(Event<T> e) {
    System.out.println("EventBus: Sending event " + e.getClass().getSimpleName());

    BlockingQueue<MicroService> subscribers = eventQueue.get(e.getClass());
    if (subscribers == null || subscribers.isEmpty()) {
        System.out.println("EventBus: No subscribers found for event " + e.getClass().getSimpleName());
        return null;
    }

    MicroService m;
    synchronized (subscribers) {
        m = subscribers.poll(); // Poll the next MicroService in round-robin fashion
        if (m != null) {
            subscribers.offer(m); // Re-add the MicroService to the end of the queue
        }
    }

    if (m == null) {
        System.out.println("EventBus: Unexpected state: No MicroService selected for event " + e.getClass().getSimpleName());
        return null;
    }

    // Log the selected MicroService for the event
    System.out.println("EventBus: Sending event " + e.getClass().getSimpleName() + " to " + m.getName());

    try {
        // Add the event to the MicroService's message queue
        BlockingQueue<Message> microServiceMessages = microServiceQueue.get(m);
        if (microServiceMessages == null) {
            throw new IllegalStateException("Failed to send event: " + e.getClass().getSimpleName() + " - queue not found for MicroService " + m.getName());
        }

        microServiceMessages.put(e);

        // Log the state of the queue after adding the event
        System.out.println("EventBus: Queue size for " + m.getName() + " after adding event: " + microServiceMessages.size());
    } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Failed to send event: " + e, ex);
    }

    // Create and store the Future for this event
    Future<T> future = new Future<>();
    futureMap.put(e, future);

    // Log the creation of the Future
    System.out.println("EventBus: Future created for event " + e.getClass().getSimpleName());

    return future;
}



	@Override
	public void register(MicroService m) {
		// TODO Auto-generated method stub
		microServiceQueue.computeIfAbsent(m, k -> new LinkedBlockingQueue<>());
	}

	@Override
	public void unregister(MicroService m) {
		microServiceQueue.remove(m);
		synchronized (eventQueue) {
			for (BlockingQueue<MicroService> queue : eventQueue.values()) {
				queue.remove(m); 
			}
		}
		synchronized (broadcastQueue) {
			for (CopyOnWriteArrayList<MicroService> list : broadcastQueue.values()) {
				list.remove(m); 
			}
		}
	}
	

	@Override
public Message awaitMessage(MicroService m) throws InterruptedException {
    System.out.println("EventBus: " + m.getName() + " waiting for a message...");

    BlockingQueue<Message> queue = microServiceQueue.get(m);
    if (queue == null) {
        throw new IllegalStateException("Failed to await message: " + m.getName() + " - no queue found");
    }

    // Log the state of the queue before taking the message
    System.out.println("EventBus: Queue size for " + m.getName() + " before taking message: " + queue.size());

    Message message = queue.take();

    // Log the message that was dequeued
    System.out.println("EventBus: " + m.getName() + " received message: " + message.getClass().getSimpleName());

    return message;
}


	

}
