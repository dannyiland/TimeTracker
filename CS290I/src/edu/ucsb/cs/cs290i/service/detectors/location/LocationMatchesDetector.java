package edu.ucsb.cs.cs290i.service.detectors.location;

import java.util.List;

import com.google.common.collect.Lists;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import edu.ucsb.cs.cs290i.service.EventDb;
import edu.ucsb.cs.cs290i.service.detectors.Detector;
import edu.ucsb.cs.cs290i.service.detectors.Event;

public class LocationMatchesDetector extends Detector {

private SQLiteDatabase db;

public LocationMatchesDetector() {
	db = EventDb.getInstance(null).getWritableDatabase();
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


}
