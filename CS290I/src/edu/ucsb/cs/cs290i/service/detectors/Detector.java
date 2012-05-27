package edu.ucsb.cs.cs290i.service.detectors;

import java.util.List;

import android.content.Context;

public abstract class Detector {
    private String[] params;
    private Context context;


    protected Detector(String... params) {
        this.params = params;
    }


    public Context getContext() {
        return context;
    }


    public String[] getParameters() {
        return params;
    }


    public abstract List<Event> getEvents(long startTime, long endTime);


    public void start(Context c) {
    };


    public void stop(Context c) {
    };

}
