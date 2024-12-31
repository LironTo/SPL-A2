package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TerminatedBroadcast implements Broadcast {
    private String terminatedName;

    public TerminatedBroadcast(String terminatedName) {
        this.terminatedName = terminatedName;
    }

    public String getTerminatedName() {
        return terminatedName;
    }
}