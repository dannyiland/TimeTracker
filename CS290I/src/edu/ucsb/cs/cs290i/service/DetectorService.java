package edu.ucsb.cs.cs290i.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import edu.ucsb.cs.cs290i.service.detectors.Detector;
import edu.ucsb.cs.cs290i.service.detectors.Event;

public class DetectorService extends Service {

    private final IBinder binder = new DetectorServiceBinder();
    private List<Detector> detectors;


    public DetectorService() {
        this.detectors = Collections.synchronizedList(new ArrayList<Detector>());
    }

    public class DetectorServiceBinder extends Binder {
        public DetectorService getService() {
            return DetectorService.this;
        }
    }


    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }


    /**
     * Get events from all detectors in the given time range.
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    public List<Event> getEvents(long startTime, long endTime) {
        ArrayList<Event> events = new ArrayList<Event>();

        // Query each detector for events in the time range.
        for (Detector d : detectors) {
            events.addAll(d.getEvents(startTime, endTime));
        }

        // Sort events by confidence.
        Collections.sort(events, new ConfidenceComparator());
        return events;
    }


    /**
     * Register a new detector.
     * 
     * @param type
     *            of detector
     * @param params
     *            Configuration parameters
     */
    public void registerDetector(Class<? extends Detector> type, String... params) {
        // TODO: The service should store a list of (type, params) on disk so that
        // the detectors list can be recreated after restarts.

        try {
            Detector detector = type.getConstructor(params.getClass()).newInstance((Object) params);
            detectors.add(detector);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ConfidenceComparator implements Comparator<Event> {

        @Override
        public int compare(Event lhs, Event rhs) {
            return Integer.valueOf(rhs.getConfidence()).compareTo(lhs.getConfidence());
        }

    }

}
