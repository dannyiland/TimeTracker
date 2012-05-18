package edu.ucsb.cs.cs290i.service.detectors;

import java.util.List;

public abstract class Detector {
    private final String[] params;


    protected Detector(String... params) {
        this.params = params;
    }


    protected String[] getParameters() {
        return params;
    }


    public abstract List<Event> getEvents(long startTime, long endTime);

    public abstract void start();
    
    public abstract void stop();
    
}
