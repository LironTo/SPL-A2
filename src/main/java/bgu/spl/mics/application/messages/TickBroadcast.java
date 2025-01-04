package bgu.spl.mics.application.messages;

import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    private final int tick;
    private final CountDownLatch latch;

    public TickBroadcast(int tick, CountDownLatch latch) {
        this.tick = tick;
        this.latch = latch;
    }

    public int getTick() {
        return tick;
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}

