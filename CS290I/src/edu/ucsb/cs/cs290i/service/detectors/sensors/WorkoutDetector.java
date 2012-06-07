package edu.ucsb.cs.cs290i.service.detectors.sensors;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.collect.Lists;

import edu.ucsb.cs.cs290i.service.EventDb;
import edu.ucsb.cs.cs290i.service.detectors.Detector;
import edu.ucsb.cs.cs290i.service.detectors.Event;

public class WorkoutDetector extends Detector {
    private SQLiteDatabase db;


    public WorkoutDetector(String... params) {
        // WorkoutDetector doesn't actually use any params.
        super(params);
    }


    @Override
    public List<Event> getEvents(long startTime, long endTime) {
        List<Event> events = Lists.newArrayList();

        System.out.println(Arrays.toString(EventDb.getTimeTypeArgs(startTime, endTime, WorkoutDetector.class)));

        Cursor c = db.query(EventDb.TABLE_NAME, null, EventDb.SELECT_TIME_TYPE,
                EventDb.getTimeTypeArgs(startTime, endTime, WorkoutDetector.class),
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
        WorkoutDataSource.getInstance().start(c);
        db = EventDb.getInstance(c).getReadableDatabase();
    }


    @Override
    public void stop(Context c) {
        if (db != null) {
            db.close();
        }
    }


    @Override
    public String toString() {
        return "Working out";
    }
}
