package edu.ucsb.cs.cs290i.service.detectors;

public class Event {
    private String name;
    private String description;
    private int confidence;
    private Detector detector;


    public Event(String name, String description, int confidence, Detector detector) {
        this.name = name;
        this.description = description;
        this.confidence = confidence;
        this.detector = detector;
    }


    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }


    public int getConfidence() {
        return confidence;
    }


    public Detector getDetector() {
        return detector;
    }

}
