package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;


public class PoseEvent implements Event<Pose> {
    private Pose position;

    public PoseEvent(Pose position) {
        this.position = position;
    }

    public Pose getPosition() {
        return position;
    }
}