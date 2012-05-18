package edu.ucsb.cs.cs290i.service.detectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ColorDetector extends Detector {
    private String color;
    

    public ColorDetector(String... params) {
        super(params);
        this.color = params[0];
    }


    public List<Event> getEvents(long startTime, long endTime) {
        // Return 3 ColorEvents.
        Random r = new Random(startTime * endTime);
        ArrayList<Event> events = new ArrayList<Event>();
        for (int i = 0; i < 3; i++) {
            events.add(new ColorEvent(color, r));
        }
        return events;
    }

    public class ColorEvent implements Event {
        private final String colorName;
        private final int confidence;


        public ColorEvent(String colorName, Random r) {
            this.colorName = colorName;
            this.confidence = r.nextInt(100);
        }


        public String getName() {
            return "Color Event";
        }


        public String getDescription() {
            return colorName + ", " + confidence;
        }


        public int getConfidence() {
            return confidence;
        }

    }


    @Override
    public void start() {
        // Detector is asynchronous, ignored.
    }


    @Override
    public void stop() {
        // Detector is asynchronous, ignored.
    }
}
