package edu.ucsb.cs.cs290i.service.detectors;

public interface Event {
    // TODO: What goes in an event? Currently name, description, and a confidence score.

    public String getName();


    public String getDescription();


    public int getConfidence();

}
