package edu.ucsb.cs.cs290i.service.detectors.sensors;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.ucsb.cs.cs290i.service.EventDb;
import edu.ucsb.cs.cs290i.service.detectors.sensors.ProximityDataSource.State;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.BatteryManager;

public class IsPluggedDataSource {

	  
    private static IsPluggedDataSource instance;

    private EventDb db;
    
    enum State {
        PLUGGED,UNPLUGGED
    };

    private State state = State.UNPLUGGED;


    public IsPluggedDataSource() {
    	//don't care
    }


    public static IsPluggedDataSource getInstance() {
        return instance == null ? instance = new IsPluggedDataSource() : instance;
    }


    public void start(Context c) {
        db = EventDb.getInstance(c);

        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                logState(state);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }


    private void logState(State state) {
        SQLiteDatabase writableDatabase = db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EventDb.KEY_EVENT_TYPE, "isplugged-" + state.toString());
        values.put(EventDb.KEY_TIMESTAMP, System.currentTimeMillis());
        writableDatabase.insert(EventDb.TABLE_NAME, null, values);

        System.out.println("Logged plugged state");
    }

	
	public static boolean isConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }
	
}
