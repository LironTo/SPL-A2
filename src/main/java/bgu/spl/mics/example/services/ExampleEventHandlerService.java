package bgu.spl.mics.example.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.example.messages.ExampleEvent;

public class ExampleEventHandlerService extends MicroService {

    private int mbt;

    public ExampleEventHandlerService(String name, String[] args) {
        super(name);

        if (args.length != 1) {
            throw new IllegalArgumentException("Event Handler expecting a single argument: mbt (the number of events to answer before termination)");
        }

        try {
            mbt = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Event Handler expecting the argument mbt to be a number > 0, instead received: " + args[0]);
        }

        if (mbt <= 0) {
            throw new IllegalArgumentException("Event Handler expecting the argument mbt to be a number > 0, instead received: " + args[0]);
        }
    }

    @Override
    protected void initialize() {
        System.out.println("Event Handler " + getName() + " started");
        
        subscribeEvent(ExampleEvent.class, ev -> {
            mbt--;
            System.out.println("Event Handler " + getName() + " got a new event from " + ev.getSenderName() + "! (mbt: " + mbt + ")");
            complete(ev, "Hello from " + getName());
            if (mbt == 0) {
                System.out.println("Event Handler " + getName() + " terminating.");
                terminate();
            }
        });
    }

}
