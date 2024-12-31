package bgu.spl.mics.application.messages;
import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {
    private String source;

    public CrashedBroadcast(String source) {
        this.source=source;
    }

    public String getCameraId() {
        return source;
    }
    
}
