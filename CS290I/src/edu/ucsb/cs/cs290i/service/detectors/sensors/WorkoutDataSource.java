package edu.ucsb.cs.cs290i.service.detectors.sensors;

import java.util.LinkedList;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import edu.ucsb.cs.cs290i.service.EventDb;

public class WorkoutDataSource implements SensorEventListener {
    private static final int PERIOD = 1000;

    // Intensity must reach START to trigger an event. Intensity must then fall below END before
    // reaching START to prevent jitter.
    private static final float START_THRESHOLD = 200;
    private static final float END_THRESHOLD = 150;

    private static WorkoutDataSource instance;

    private SensorManager sensorService;

    private EventDb db;

    private float windowSum;

    private LinkedList<Float> window;

    enum State {
        IDLE, WORKOUT
    };

    private State state = State.IDLE;


    public WorkoutDataSource() {
        window = new LinkedList<Float>();
    }


    public static WorkoutDataSource getInstance() {
        return instance == null ? instance = new WorkoutDataSource() : instance;
    }


    public void start(Context c) {
        db = EventDb.getInstance(c);

        sensorService = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        sensorService.registerListener(this, sensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO: This doesn't get called if the phone is completely motionless, which can prevent
        // IDLE from being reached.

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
            System.out.println("Idle --> workout");
        } else if (state == State.WORKOUT && average < END_THRESHOLD) {
            state = State.IDLE;
            System.out.println("Workout --> idle");
        }

    }


    private void logEvent() {
        SQLiteDatabase writableDatabase = db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EventDb.KEY_EVENT_TYPE, WorkoutDetector.class.getName());
        values.put(EventDb.KEY_TIMESTAMP, System.currentTimeMillis());
        writableDatabase.insert(EventDb.TABLE_NAME, null, values);
        
        writableDatabase.close();
        System.out.println("Logged event");
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Don't care.
    }

}
