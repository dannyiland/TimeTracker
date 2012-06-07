package edu.ucsb.cs.cs290i.service.detectors.sensors;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.ucsb.cs.cs290i.service.EventDb;
import edu.ucsb.cs.cs290i.service.detectors.sensors.WorkoutDataSource.State;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class ProximityDataSource implements SensorEventListener {

    
    private static ProximityDataSource instance;

    private SensorManager sensorService;

    private EventDb db;

    
    enum State {
        NEAR,FAR
    };

    private State state = State.NEAR;


    public ProximityDataSource() {
    	//don't care
    }


    public static ProximityDataSource getInstance() {
        return instance == null ? instance = new ProximityDataSource() : instance;
    }


    public void start(Context c) {
        db = EventDb.getInstance(c);

        sensorService = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        sensorService.registerListener(this, sensorService.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                SensorManager.SENSOR_DELAY_FASTEST);

        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                logState(state);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        if (x < 10)
        		state = State.NEAR;
        else state = State.FAR;

    }

    private void logState(State state) {
        SQLiteDatabase writableDatabase = db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EventDb.KEY_EVENT_TYPE, "proximity-" + state.toString());
        values.put(EventDb.KEY_TIMESTAMP, System.currentTimeMillis());
        writableDatabase.insert(EventDb.TABLE_NAME, null, values);

        System.out.println("Logged proximity state");
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Don't care.
    }
	
}
