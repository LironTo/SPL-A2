package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class FinishRunBroadcast implements Broadcast {
    private String terminatedName;

    public FinishRunBroadcast(String terminatedName) {
        this.terminatedName = terminatedName;
    }

    public String getTerminatedName() {
        return terminatedName;
    }
}
