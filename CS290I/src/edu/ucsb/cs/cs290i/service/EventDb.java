package edu.ucsb.cs.cs290i.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import edu.ucsb.cs.cs290i.service.detectors.Detector;

public class EventDb extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "TimeTracker";
    public static final int DATABASE_VERSION = 1;

    public static final String KEY_TIMESTAMP = "time";
    public static final String KEY_EVENT_TYPE = "type";

    public static final String TABLE_NAME = "sensordata";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_TIMESTAMP + " INTEGER, " +
                    KEY_EVENT_TYPE + " TEXT);";

    public static final String SELECT_TIME_TYPE = KEY_TIMESTAMP + " > ? AND " + KEY_TIMESTAMP + " < ? AND "
            + KEY_EVENT_TYPE + " = ?";

    private static EventDb instance;


    protected EventDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("Create DB");
        db.execSQL(CREATE_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException("DIY");
    }


    public static EventDb getInstance(Context c) {
        return instance == null ? instance = new EventDb(c) : instance;
    }


    public static String[] getTimeTypeArgs(long startTime, long endTime, String type) {
        return new String[] { Long.toString(startTime), Long.toString(endTime), type };
    }

}
