package edu.ucsb.cs.cs290i.service.detectors.location;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import edu.ucsb.cs.cs290i.service.EventDb;
import edu.ucsb.cs.cs290i.service.detectors.Detector;

/**
 * Two tables: One for Locations (Lat/Long/Name/Radius) and 
 * one for LocationInstances (Lat/Long/LocationName/
 * @author iland
 *
 */
public class LocationDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "TimeTracker";
    public static final int DATABASE_VERSION = 1;
	public static final String LOCATION_TABLE_NAME="locationInstances";
	public static final String KNOWN_LOCATION_TABLE_NAME="Locations";

    public static final String KEY_TIMESTAMP = "_id"; // Primary key is time, by standard this is _id
    public static final String KEY_LOCATION_TYPE = "type";
	public static final String KEY_LATITUDE = "lat";
	public static final String KEY_LONGITUDE = "long";
	public static final String KEY_KNOWN = "known";
	public static final String KEY_NAME = "name";
	public static final String KEY_STARTTIME = "_id";
	public static final String KEY_ENDTIME = "end";
	
	public static final String KEY_RADIUS = "radius"; //meters

    private static final String CREATE_LOCATION_INSTANCE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + LOCATION_TABLE_NAME + " (" +
                    KEY_LATITUDE + " INTEGER, " +
                    KEY_LONGITUDE + " INTEGER, " +
                    KEY_STARTTIME + " INTEGER, " + 
                    KEY_ENDTIME + " INTEGER, " + 
                    KEY_KNOWN + " TEXT);";


    private static final String CREATE_KNOWN_LOCATION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + KNOWN_LOCATION_TABLE_NAME + " (" +
                    KEY_TIMESTAMP + " INTEGER, " +
                    KEY_LATITUDE + " INTEGER, " +
                    KEY_LONGITUDE + " INTEGER, " +
                    KEY_NAME + " TEXT, " +
                    KEY_LOCATION_TYPE + " TEXT " +
                    KEY_RADIUS + " INTEGER);";
    

    public static final String SELECT_TIME_TYPE = KEY_TIMESTAMP + " > ? AND " + KEY_TIMESTAMP + " < ? AND "
            + KEY_LOCATION_TYPE + " = ?";

    private static LocationDB instance;


    protected LocationDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("Create DB");
        db.execSQL(CREATE_LOCATION_INSTANCE_TABLE);
        db.execSQL(CREATE_KNOWN_LOCATION_TABLE);
        db.execSQL("TRUNCATE TABLE " + CREATE_KNOWN_LOCATION_TABLE);
        db.execSQL("TRUNCATE TABLE " + CREATE_LOCATION_INSTANCE_TABLE);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException("DIY");
    }


    public static LocationDB getInstance(Context c) {
        return instance == null ? instance = new LocationDB(c) : instance;
        
    }

	public ArrayList<LocationInstance> getAllKnownLocations(Context context) {
		SQLiteDatabase db = EventDb.getInstance(context).getWritableDatabase();
        db.execSQL(CREATE_LOCATION_INSTANCE_TABLE);
        db.execSQL(CREATE_KNOWN_LOCATION_TABLE);
		ArrayList<LocationInstance> locations = new ArrayList<LocationInstance>();
		Cursor c = db.query(LocationDB.LOCATION_TABLE_NAME, null, null ,null,null, null, null);
		System.out.println(c.getCount());
		int max = 20;
		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast() && max > 0) {
				max = max - 1;
				double lat = c.getDouble(c.getColumnIndex(LocationDB.KEY_LATITUDE));
				double lon = c.getDouble(c.getColumnIndex(LocationDB.KEY_LONGITUDE));
				long startTime = c.getLong(c.getColumnIndex(LocationDB.KEY_STARTTIME));
				long endTime = c.getLong(c.getColumnIndex(LocationDB.KEY_ENDTIME));
				String names = c.getString(c.getColumnIndex(LocationDB.KEY_KNOWN));
				locations.add(new LocationInstance(names, lat, lon, startTime, endTime));
			}
			c.moveToNext();
		}
		return locations;
	}
}
