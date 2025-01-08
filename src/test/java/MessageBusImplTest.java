/*package bgu.spl.mics.tests;

import bgu.spl.mics.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

public class MessageBusImplTest {

    // Event and Broadcast classes for testing
    public static class TestEvent implements Event<String> {
    }

    public static class TestBroadcast implements Broadcast {
    }

    private MessageBusImpl messageBus;
    private MicroService microService1;
    private MicroService microService2;
    private TestEvent testEvent;
    private TestBroadcast testBroadcast;

    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getInstance();
        microService1 = new MicroService("Service1") {
            @Override
            protected void initialize() {
            }
        };
        microService2 = new MicroService("Service2") {
            @Override
            protected void initialize() {
            }
        };
        testEvent = new TestEvent();
        testBroadcast = new TestBroadcast();
    }

    @Test
    void testRegisterAndUnregisterMicroService() {
        messageBus.register(microService1);
        messageBus.unregister(microService1);
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(microService1));
    }

    @Test
    void testSendEvent() throws InterruptedException {
        messageBus.register(microService1);
        messageBus.subscribeEvent(TestEvent.class, microService1);
        Future<String> future = messageBus.sendEvent(testEvent);

        assertNotNull(future);

        Message message = messageBus.awaitMessage(microService1);
        assertTrue(message instanceof TestEvent);
        assertEquals(testEvent, message);
    }

    @Test
    void testSendBroadcast() throws InterruptedException {
        messageBus.register(microService1);
        messageBus.subscribeBroadcast(TestBroadcast.class, microService1);

        messageBus.sendBroadcast(testBroadcast);

        Message message = messageBus.awaitMessage(microService1);
        assertTrue(message instanceof TestBroadcast);
        assertEquals(testBroadcast, message);
    }

    @Test
    void testCompleteEvent() {
        messageBus.register(microService1);
        messageBus.subscribeEvent(TestEvent.class, microService1);

        Future<String> future = messageBus.sendEvent(testEvent);
        assertNotNull(future);

        messageBus.complete(testEvent, "Completed");
        assertEquals("Completed", future.get());
    }

    @Test
    void testNoSubscribersForEvent() {
        Future<String> future = messageBus.sendEvent(testEvent);
        assertNull(future);
    }

    @Test
    void testAwaitMessageForUnregisteredMicroService() {
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(microService1));
    }

    @Test
    void testMultipleSubscribersToBroadcast() throws InterruptedException {
        messageBus.register(microService1);
        messageBus.register(microService2);

        messageBus.subscribeBroadcast(TestBroadcast.class, microService1);
        messageBus.subscribeBroadcast(TestBroadcast.class, microService2);

        messageBus.sendBroadcast(testBroadcast);

        Message message1 = messageBus.awaitMessage(microService1);
        Message message2 = messageBus.awaitMessage(microService2);

        assertTrue(message1 instanceof TestBroadcast);
        assertTrue(message2 instanceof TestBroadcast);
        assertEquals(testBroadcast, message1);
        assertEquals(testBroadcast, message2);
    }

    @Test
    void testRoundRobinEventDistribution() throws InterruptedException {
        messageBus.register(microService1);
        messageBus.register(microService2);

        messageBus.subscribeEvent(TestEvent.class, microService1);
        messageBus.subscribeEvent(TestEvent.class, microService2);

        messageBus.sendEvent(testEvent);
        Message message1 = messageBus.awaitMessage(microService1);

        TestEvent testEvent2 = new TestEvent();
        messageBus.sendEvent(testEvent2);
        Message message2 = messageBus.awaitMessage(microService2);

        assertEquals(testEvent, message1);
        assertEquals(testEvent2, message2);
    }
}*/
