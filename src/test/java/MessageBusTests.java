
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


import static org.junit.jupiter.api.Assertions.*;

class MessageBusTests {
    private MessageBusImpl messageBus;
    private MicroService testService1;
    private MicroService testService2;

    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getInstance();
        CountDownLatch countDownLatch = new CountDownLatch(2);
        testService1 = new MicroService("TestService1",countDownLatch) {
            @Override
            protected void initialize() {}
        };
        testService2 = new MicroService("TestService2",countDownLatch) {
            @Override
            protected void initialize() {}
        };
        messageBus.register(testService1);
        messageBus.register(testService2);
    }

    @Test
    void testSubscribeEvent() {
        messageBus.subscribeEvent(TestEvent.class, testService1);
        Event<String> event = new TestEvent();

        Future<String> future = messageBus.sendEvent(event);
        assertNotNull(future);

        try {
            Message receivedMessage = messageBus.awaitMessage(testService1);
            assertTrue(receivedMessage instanceof TestEvent);
        } catch (InterruptedException e) {
            fail("Thread interrupted while waiting for a message");
        }
    }

    @Test
    void testSubscribeBroadcast() {
        messageBus.subscribeBroadcast(TestBroadcast.class, testService1);
        Broadcast broadcast = new TestBroadcast();
        messageBus.sendBroadcast(broadcast);

        try {
            Message receivedMessage = messageBus.awaitMessage(testService1);
            assertTrue(receivedMessage instanceof TestBroadcast);
        } catch (InterruptedException e) {
            fail("Thread interrupted while waiting for a message");
        }
    }

    @Test
    void testCompleteEvent() {
        messageBus.subscribeEvent(TestEvent.class, testService1);
        Event<String> event = new TestEvent();
        Future<String> future = messageBus.sendEvent(event);
        assertNotNull(future);

        messageBus.complete(event, "Result");
        assertTrue(future.isDone());
        assertEquals("Result", future.get(1, TimeUnit.SECONDS));
    }

    @Test
    void testUnregister() {
        messageBus.subscribeEvent(TestEvent.class, testService1);
        messageBus.unregister(testService1);

        Event<String> event = new TestEvent();
        Future<String> future = messageBus.sendEvent(event);
        assertNull(future, "Event should not be sent to unregistered service");

        assertThrows(IllegalStateException.class, () -> {
            messageBus.awaitMessage(testService1);
        }, "Unregistered service should throw IllegalStateException");
    }


    @Test
    void testAwaitMessageBlocking() {
        Thread testThread = new Thread(() -> {
            try {
                messageBus.awaitMessage(testService1);
                fail("Should not receive a message before one is sent");
            } catch (IllegalStateException e) {
                fail("Service is registered, should not throw IllegalStateException");
            } catch (InterruptedException ignored) {}
        });

        testThread.start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        assertTrue(testThread.isAlive(), "awaitMessage should block if no messages are available");

        testThread.interrupt();
    }
    // Dummy classes for testing purposes
    public class TestEvent implements Event<String> {}

    public class TestBroadcast implements Broadcast {}

}
