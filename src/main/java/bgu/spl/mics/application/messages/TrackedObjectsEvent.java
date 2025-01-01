package bgu.spl.mics.application.messages;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

public class TrackedObjectsEvent implements Event<Boolean> {
    private String sender;
    private final CopyOnWriteArrayList<TrackedObject> serials;

    public TrackedObjectsEvent(List<TrackedObject> serials, String sender) {
        this.serials = new CopyOnWriteArrayList<>(serials);
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    public List<TrackedObject> getSerials() {
        return serials;
    }
}