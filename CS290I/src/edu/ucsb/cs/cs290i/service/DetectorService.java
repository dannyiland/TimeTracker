package edu.ucsb.cs.cs290i.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.ucsb.cs.cs290i.service.detectors.Detector;
import edu.ucsb.cs.cs290i.service.detectors.Event;
import edu.ucsb.cs.cs290i.service.detectors.calendar.AccountChooser;
import edu.ucsb.cs.cs290i.service.detectors.calendar.CalendarDataSource;

public class DetectorService extends Service {

    private final IBinder binder = new DetectorServiceBinder();
    private List<Detector> detectors;

    private long lastTime;
    private List<Action> actions;
    private boolean restoringActions;


    public DetectorService() {
        this.detectors = Collections.synchronizedList(new ArrayList<Detector>());
        this.actions = Collections.synchronizedList(new ArrayList<Action>());

        lastTime = System.currentTimeMillis();
    }

    public class DetectorServiceBinder extends Binder {
        public DetectorService getService() {
            return DetectorService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize CalendarDataSource.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String accountName = settings.getString("accountName", null);
        String authToken = settings.getString("authToken", null);
        if (accountName == null || authToken == null) {
            Log.w("DetectorService", "Requesting Calendar authorization.");

            Intent intent = new Intent();
            intent.setClass(this, AccountChooser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        } else {
            System.out.println(accountName);
            System.out.println(authToken);
            CalendarDataSource.getInstance().setAuthToken(authToken);
        }

        // Restore saved Detectors/Actions.
        try {
            restoringActions = true;
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getActionsFile()));
            @SuppressWarnings("unchecked")
            List<Action> actions = (List<Action>) ois.readObject();
            ois.close();
            for (Action a : actions) {
                registerAction(a);
            }
        } catch (FileNotFoundException e) {
            Log.w("DetectorService", "Saved Actions file not found");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            restoringActions = false;
        }

    }


    @Override
    public void onDestroy() {
        Log.e("DetectorService", "onDestroy()");

        saveActions();

        super.onDestroy();
    }


    private void saveActions() {
        // Save Detectors/Actions.
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getActionsFile()));
            oos.writeObject(actions);
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private File getActionsFile() {
        return new File(getDir("persist", 0), "actions.dat");
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


    public List<Action> matchActions(long startTime, long endTime) {
        List<Event> events = getEvents(startTime, endTime);
        System.out.println("Events in range are " + events.toString());

        List<Action> activeActions = new ArrayList<Action>();
        for (Action a : actions) {
            System.out.println("Matching against " + a.getName());
            if (a.matches(events)) {
                activeActions.add(a);
            }
        }
        return activeActions;
    }


    public List<Action> triggerTransition() {
        long oldLast = lastTime;
        lastTime = System.currentTimeMillis();
        return matchActions(oldLast, lastTime);
    }


    public List<Action> getRegisteredActions() {
        return actions;
    }


    /**
     * Register a new detector.
     * 
     * @param type
     *            of detector
     * @param params
     *            Configuration parameters
     */
    public void registerAction(Action a) {
        // TODO(iw): I'm not sure if we need a list of detectors anymore. We could just have Actions
        // query their own detectors directly instead of using getEvents(). However, this list might
        // be useful in the future.
        for (Detector d : a.getDetectors()) {
            detectors.add(d);
            d.start(this);
        }
        actions.add(a);

        if (!restoringActions) {
            saveActions();
        }
    }

    public class ConfidenceComparator implements Comparator<Event> {

        @Override
        public int compare(Event lhs, Event rhs) {
            return Integer.valueOf(rhs.getConfidence()).compareTo(lhs.getConfidence());
        }

    }


    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

}
