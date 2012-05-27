package edu.ucsb.cs.cs290i.service.detectors.sensors;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.common.collect.Lists;

import edu.ucsb.cs.cs290i.service.EventDb;
import edu.ucsb.cs.cs290i.service.detectors.Detector;
import edu.ucsb.cs.cs290i.service.detectors.Event;

public class WorkoutDetector extends Detector implements SensorEventListener {
    private static final int PERIOD = 1000;

    // Intensity must reach START to trigger an event. Intensity must then fall below END before
    // reaching START to prevent jitter.
    private static final float START_THRESHOLD = 20;
    private static final float END_THRESHOLD = 10;

    private SensorManager sensorService;
    private SQLiteDatabase db;

    private float windowSum;
    private LinkedList<Float> window;

    enum State {
        IDLE, WORKOUT
    };

    private State state = State.IDLE;


    public WorkoutDetector(String... params) {
        super(params);

        window = new LinkedList<Float>();
    }


    @Override
    public List<Event> getEvents(long startTime, long endTime) {
        List<Event> events = Lists.newArrayList();

        Cursor c = db.query(EventDb.TABLE_NAME, null, EventDb.SELECT_TIME_TYPE,
                EventDb.getTimeTypeArgs(startTime, endTime, this),
                null, null, null);

        System.out.println("Selected " + c.getCount() + " events");

        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                long timeStamp = c.getLong(c.getColumnIndex(EventDb.KEY_TIMESTAMP));
                events.add(new Event("WorkoutDetector", "Workout detected at " + timeStamp, 1, this));
                c.moveToNext();
            }
        }

        return events;
    }


    @Override
    public void start(Context c) {
        sensorService = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        sensorService.registerListener(this, sensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        db = EventDb.getInstance(c).getWritableDatabase();
    }


    @Override
    public void stop(Context c) {
        if (sensorService != null) {
            sensorService.unregisterListener(this);
        }
        if (db != null) {
            db.close();
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Actually the magnitude squared.
        float magnitude = x * x + y * y + z * z;

        // Update moving average.
        windowSum += magnitude;
        window.addLast(magnitude);
        if (window.size() > PERIOD) {
            windowSum -= window.removeFirst();
        }

        float average = windowSum / window.size();

        if (state == State.IDLE && average > START_THRESHOLD) {
            state = State.WORKOUT;
            logEvent();
        } else if (state == State.WORKOUT && average < END_THRESHOLD) {
            state = State.IDLE;
        }

    }


    private void logEvent() {
        ContentValues values = new ContentValues();
        values.put(EventDb.KEY_EVENT_TYPE, getClass().getName());
        values.put(EventDb.KEY_TIMESTAMP, System.currentTimeMillis());
        db.insert(EventDb.TABLE_NAME, null, values);
        System.out.println("Logged event");
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Don't care.
    }
}
