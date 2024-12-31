package bgu.spl.mics.application.messages;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

public class TrackedObjectsEvent implements Event<List<TrackedObject>> {
    private final CopyOnWriteArrayList<TrackedObject> serials;

    public TrackedObjectsEvent(List<TrackedObject> serials) {
        this.serials = new CopyOnWriteArrayList<>(serials);
    }

    public List<TrackedObject> getSerials() {
        return serials;
    }
}