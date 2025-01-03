import bgu.spl.mics.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class MessageBusTest {

    public class TestEvent implements Event<String> {
        // מחלקה ריקה רק לצורך הבדיקה
    }

    public class TestBroadcast implements Broadcast {
        // מחלקה ריקה רק לצורך הבדיקה
    }

    //private MessageBus messageBus;
    private MessageBusImpl messageBus;
    private MicroService microService1;
    private MicroService microService2;
    private TestEvent testEvent;
    private TestBroadcast testBroadcast;
    private BlockingQueue<Message> queue;

    @BeforeEach
    void setUp() {

        messageBus = MessageBusImpl.getInstance();
        microService1 = new MicroService("Service1") {
            @Override
            protected void initialize() {
                // Implement microservice initialization logic if needed
            }
        };
        microService2 = new MicroService("Service2") {
            @Override
            protected void initialize() {
                // Implement microservice initialization logic if needed
            }
        };
        testEvent = new TestEvent();
        testBroadcast = new TestBroadcast();
        queue = new LinkedBlockingQueue<>();
    }


    @Test
    public void testRegister() {
        messageBus.register(microService1);
        try {
            assertNull(messageBus.awaitMessage(microService1)); // עדיף assertNull במקום השוואה ל-null
        } catch (InterruptedException e) {
            fail("Unexpected InterruptedException: " + e.getMessage());
        }
    }

    @Test
    public void testUnregister() {
        messageBus.register(microService1);
        messageBus.unregister(microService1);
        try {
            messageBus.awaitMessage(microService1);
            fail("Expected IllegalStateException because microService1 was unregistered.");
        } catch (IllegalStateException e) {
            // Expected exception
        } catch (InterruptedException e) {
            fail("Unexpected InterruptedException: " + e.getMessage());
        }
    }
   @Test
    public void testSendBroadcast() {
        messageBus.register(microService1);
        messageBus.subscribeBroadcast(testBroadcast.getClass(), microService1);

        messageBus.sendBroadcast(testBroadcast);

        try {
            Message message = messageBus.awaitMessage(microService1);
            assertTrue(message instanceof Broadcast);
        } catch (InterruptedException e) {
            fail("Unexpected InterruptedException: " + e.getMessage());
        }
    }

    @Test
    public void testCompleteEvent() {
        messageBus.register(microService1);
        messageBus.subscribeEvent(testEvent.getClass(), microService1);

        Future<String> future = messageBus.sendEvent(testEvent);
        assertNotNull(future);

        messageBus.complete(testEvent, "Completed");
        assertEquals("Completed", future.get());

    }

    @Test
    public void testEventQueueing() throws InterruptedException {
        messageBus.register(microService1);
        messageBus.subscribeEvent(testEvent.getClass(), microService1);

        messageBus.sendEvent(testEvent);

        Message receivedMessage = messageBus.awaitMessage(microService1);
        assertTrue(receivedMessage instanceof Event<?>);
        assertEquals(testEvent, receivedMessage);
    }

    @Test
    public void testSubscribeEvent() {
        messageBus.register(microService1);
        messageBus.subscribeEvent(testEvent.getClass(), microService1);

        assertTrue(messageBus.getEventSubscribers()
                .get(testEvent.getClass())
                .contains(microService1));
    }

    @Test
    public void testSubscribeBroadcast() {
        messageBus.register(microService1);
        messageBus.subscribeBroadcast(testBroadcast.getClass(), microService1);

        assertTrue(messageBus.getBroadcastSubscribers()
                .get(testBroadcast.getClass())
                .contains(microService1));
    }

    @Test
    public void testSendEvent_NoSubscribers() {
        Future<String> future = messageBus.sendEvent(testEvent);
        assertNull(future); // If no subscribers, the returned Future should be null.
    }

    @Test
    public void testAwaitMessage_NoRegisteredMicroService() {
        try {
            messageBus.awaitMessage(microService1);
            fail("Expected IllegalStateException when trying to awaitMessage for an unregistered MicroService.");
        } catch (Exception e) {
            assertEquals("MicroService is not registered: Service1", e.getMessage());
        }
    }
}