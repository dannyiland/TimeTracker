package edu.ucsb.cs.cs290i.service.detectors.location;

import java.util.ArrayList;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Two tables: One for Locations (Lat/Long/Name/Radius) and 
 * one for LocationInstances (Lat/Long/LocationName/
 * @author iland
 *
 */
public class LocationDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "TimeTrackerLocationDB";
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
	public static final String KEY_NAME_LOC = "_id";
	private Context context = null;

    private static final String CREATE_LOCATION_INSTANCE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + LOCATION_TABLE_NAME + " (" +
                    KEY_LATITUDE + " INTEGER, " +
                    KEY_LONGITUDE + " INTEGER, " +
                    KEY_STARTTIME + " INTEGER, " + 
                    KEY_ENDTIME + " INTEGER, " + 
                    KEY_KNOWN + " TEXT," +
                    KEY_NAME + " TEXT" +
                    ");";

//
//    private static final String CREATE_KNOWN_LOCATION_TABLE =
//            "CREATE TABLE IF NOT EXISTS " + KNOWN_LOCATION_TABLE_NAME + " (" +
//                    KEY_TIMESTAMP + " INTEGER, " +
//                    KEY_LATITUDE + " INTEGER, " +
//                    KEY_LONGITUDE + " INTEGER, " +
//                    KEY_NAME + " TEXT, " +
//                    KEY_LOCATION_TYPE + " TEXT " +
//                    KEY_RADIUS + " INTEGER);";
//    

    private static final String CREATE_KNOWN_LOCATION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + KNOWN_LOCATION_TABLE_NAME + " (" +
                    KEY_TIMESTAMP + " INTEGER, " +
                    KEY_LATITUDE + " INTEGER, " +
                    KEY_LONGITUDE + " INTEGER, " +
                    KEY_NAME + " TEXT, " +
                    KEY_LOCATION_TYPE + " TEXT " +
                    KEY_RADIUS + " INTEGER);";
    
	private static final String CACHE_TABLE_NAME = "LAT_LON_BOUNDS";
	private static final String MIN_LATITUDE = "minLat";
	private static final String MAX_LATITUDE = "maxLat";
	private static final String MIN_LONGITUDE = "minLon";
	private static final String MAX_LONGITUDE = "maxLon";
    
   
    private static final String CREATE_LATLON_CACHE_TABLE = 
    		"CRAETE TABLE IF NOT EXISTS " + CACHE_TABLE_NAME + " (" +
    				KEY_NAME_LOC + " TEXT," +
    				KEY_LATITUDE + " INTEGER, " + 
    	    		KEY_LONGITUDE+ " INTEGER, " + 
    	    		MIN_LATITUDE + " INTEGER, " + 
    				MAX_LATITUDE + " INTEGER, " + 
    	    		MIN_LONGITUDE + " INTEGER, "+ 
    	    		MAX_LONGITUDE + " INTEGER);";
    

    public static final String SELECT_TIME_TYPE = KEY_TIMESTAMP + " > ? AND " + KEY_TIMESTAMP + " < ? AND "
            + KEY_LOCATION_TYPE + " = ?";

    private static LocationDB instance;
	private SQLiteDatabase database;


    protected LocationDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    	database = db;
        System.out.println("Create DB");
        db.execSQL("TRUNCATE TABLE " + CREATE_KNOWN_LOCATION_TABLE);
        db.execSQL("TRUNCATE TABLE " + CREATE_LOCATION_INSTANCE_TABLE);
        db.execSQL(CREATE_LOCATION_INSTANCE_TABLE);
        db.execSQL(CREATE_KNOWN_LOCATION_TABLE);
        db.execSQL(CREATE_LATLON_CACHE_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException("DIY");
    }


    public static LocationDB getInstance(Context c) {
        return instance == null ? instance = new LocationDB(c) : instance;
        
    }
    
    public int moveLocation(String name, double oldLat, double oldLon, double newLat, double newLon) {
    	ContentValues val = new ContentValues();
    	val.put(KEY_LATITUDE, newLat);
    	val.put(KEY_LONGITUDE, newLon);
    	int numChanged = LocationDB.getInstance(context).getWritableDatabase().update(KNOWN_LOCATION_TABLE_NAME, val, KEY_NAME + "=" + name, null);
    	System.out.println("Moved location " + name + " from " + oldLat + "," + oldLon + " to " + newLat + "," + newLon);
    	return numChanged;
    }

	public ArrayList<Location> getAllKnownLocations(Context context) {
		SQLiteDatabase db = LocationDB.getInstance(context).getWritableDatabase();
		ArrayList<Location> locations = new ArrayList<Location>();
		Cursor c = db.query(LocationDB.KNOWN_LOCATION_TABLE_NAME, null, null , null, null, null, null);
		System.out.println(c.getCount());
		int max = 200;
		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast() && max > 0) {
				max = max - 1;
				double lat = c.getDouble(c.getColumnIndex(LocationDB.KEY_LATITUDE));
				double lon = c.getDouble(c.getColumnIndex(LocationDB.KEY_LONGITUDE));
				long lastVisit = c.getLong(c.getColumnIndex(LocationDB.KEY_TIMESTAMP));
				String names = c.getString(c.getColumnIndex(LocationDB.KEY_NAME));
				String type = c.getString(c.getColumnIndex(LocationDB.KEY_LOCATION_TYPE));
				long radius = c.getLong(c.getColumnIndex(LocationDB.KEY_RADIUS));
				locations.add(new Location(names, lat, lon, radius, type));

			}
			c.moveToNext();
		}
		return locations;
	}


	// So we don't have to use the DB all the time, for each location in Locations, determine the min and max Lat/Lons that equate to the radius (roughly). Square vs circle.
	public void populateCache(Context context) {
		SQLiteDatabase db = LocationDB.getInstance(context).getWritableDatabase();
		ArrayList<Location> locations = new ArrayList<Location>();
		Cursor c = db.query(LocationDB.KNOWN_LOCATION_TABLE_NAME, null, null ,null,null, null, null);
		System.out.println(c.getCount());
		int max = 10000;
		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast() && max > 0) {
				max = max - 1;				
				double lat = c.getDouble(c.getColumnIndex(LocationDB.KEY_LATITUDE));
				double lon = c.getDouble(c.getColumnIndex(LocationDB.KEY_LONGITUDE));
				long lastVisit = c.getLong(c.getColumnIndex(LocationDB.KEY_TIMESTAMP));
				String names = c.getString(c.getColumnIndex(LocationDB.KEY_NAME));
				String type = c.getString(c.getColumnIndex(LocationDB.KEY_LOCATION_TYPE));
				long radius = c.getLong(c.getColumnIndex(LocationDB.KEY_RADIUS));
				double[] bounds = GeoLocation.fromDegrees(lat, lon).boundingCoordinates(radius, 6371010);
				ContentValues contentValues = new ContentValues();
				contentValues.put(KEY_NAME_LOC, names);
				contentValues.put(KEY_LATITUDE, lat);
				contentValues.put(KEY_LONGITUDE, lon);
				contentValues.put(MIN_LATITUDE,  bounds[0]);
				contentValues.put(MAX_LATITUDE,  bounds[1]);				
				contentValues.put(MIN_LONGITUDE, bounds[2]);
				contentValues.put(MAX_LONGITUDE, bounds[3]);
				db.insert(CACHE_TABLE_NAME, null, contentValues);
				
			}
			c.moveToNext();
		}
	}
	
	public void addLocation(Location loc) {
		double lat = loc.getLatitude();
		double lon = loc.getLongitude();
		long radius = loc.getRadius();
		String names = loc.getName();
		long timestamp = loc.getTimestamp();
		String type = loc.getType();
		SQLiteDatabase db = LocationDB.getInstance(context).getWritableDatabase();
	//	double[] bounds = GeoLocation.fromDegrees(lat, lon).boundingCoordinates(radius, 6371010);
//	
//		ContentValues contentValues = new ContentValues();
//		contentValues.put(KEY_NAME_LOC, names);
//		contentValues.put(KEY_LATITUDE, lat);
//		contentValues.put(KEY_LONGITUDE, lon);
//		contentValues.put(MIN_LATITUDE,  bounds[0]);
//		contentValues.put(MAX_LATITUDE,  bounds[1]);				
//		contentValues.put(MIN_LONGITUDE, bounds[2]);
//		contentValues.put(MAX_LONGITUDE, bounds[3]);
//		db.insert(CACHE_TABLE_NAME, null, contentValues);

		ContentValues newLocation = new ContentValues();
		newLocation.put(KEY_TIMESTAMP,timestamp);
		newLocation.put(KEY_LATITUDE,lat);
		newLocation.put(KEY_LONGITUDE,lon);
		newLocation.put(KEY_NAME,names);
		newLocation.put(KEY_LOCATION_TYPE,type);
		newLocation.put(KEY_RADIUS,radius);
		db.insert(KNOWN_LOCATION_TABLE_NAME, null, newLocation);
	}
	
	public ArrayList<LocationInstance> getLocationsBetween(long startTimeMS, long endTimeMS, Context context) {
		SQLiteDatabase db = LocationDB.getInstance(context).getWritableDatabase();
		ArrayList<LocationInstance> locations = new ArrayList<LocationInstance>();
		Cursor c = db.query(LocationDB.LOCATION_TABLE_NAME, null, null ,null,null, null, null);
		System.out.println(c.getCount());
		int max = 200;
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


	public ArrayList<Location> getKnownLocationsBetween(long startTimeMS, long endTimeMS, Context applicationContext) {
		SQLiteDatabase db = LocationDB.getInstance(context).getWritableDatabase();
		ArrayList<Location> locations = new ArrayList<Location>();
		Cursor c = db.query(LocationDB.KNOWN_LOCATION_TABLE_NAME, null, null ,null,null, null, null);
		System.out.println(c.getCount());
		int max = 200;
		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast() && max > 0) {
				max = max - 1;
				double lat = c.getDouble(c.getColumnIndex(LocationDB.KEY_LATITUDE));
				double lon = c.getDouble(c.getColumnIndex(LocationDB.KEY_LONGITUDE));
				long startTime = c.getLong(c.getColumnIndex(LocationDB.KEY_STARTTIME));
				long endTime = c.getLong(c.getColumnIndex(LocationDB.KEY_ENDTIME));
				String names = c.getString(c.getColumnIndex(LocationDB.KEY_KNOWN));
				locations.add(new Location(names, lat, lon, startTime));
			}
			c.moveToNext();
		}
		return locations;
	}


	public ArrayList<LocationInstance> getLocationsInRange(
			Context applicationContext, long startTimeMS, long endTimeMS) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
