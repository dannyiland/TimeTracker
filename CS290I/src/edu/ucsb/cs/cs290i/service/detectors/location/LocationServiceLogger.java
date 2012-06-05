package edu.ucsb.cs.cs290i.service.detectors.location;

import java.util.ArrayList;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class LocationServiceLogger extends Service {
	private SQLiteDatabase db = null;
	private final IBinder binder = new LocationServiceLoggerBinder();

	private android.location.Location lastKnownLocation = null;
	private long lastRecordedTime=0;
	private float DEFAULT_RADIUS_METERS = 100;
	private long MIN_TIME_BETWEEN_UPDATES_MS = 30000; //30 seconds between location polls
	public static final String ALL_KNOWN_LOCATIONS = LocationDB.KEY_LATITUDE + " > -180 AND " + LocationDB.KEY_LATITUDE + " < 180" ;

	//
//	private ALEventListener alEventListener = new ALEventListener() {
//		@SuppressWarnings("unchecked")
//		@Override
//		public void handleEvent(ALEvents event, Object data) {
//			if (event == ALEvents.USERSTATS_OF_A_PLACE_CALLBACK) {
//				ArrayList<UserStay> stays = (ArrayList<UserStay>) data;
//				for (UserStay stay : stays) {
//					LocationInstance newLoc = new LocationInstance(stay.getCandidates(), stay.getCentroidLatE6(),
//							stay.getCentroidLngE6(), stay.getStartTime(), stay.getEndTime());
//					//visits.add(newLoc);
//				}
//			} else if (event == ALEvents.PLACES_QUERY_CALLBACK) {
//				ArrayList<PlaceProfile> places = (ArrayList<PlaceProfile>) data;
//				// Post process the places
//				for (PlaceProfile place : places) {
//					mAlohar.getPlaceManager().getUserStays(place, alEventListener);
//				}
//			}
//		}
//	};
//	
	

	// Adds a location to database on departure, with arrival and departure time, lat/long, and name if known.
	public void addLocationEntry(android.location.Location location) {
		long startTime = 0;
		if(startTime == 0 | lastKnownLocation == null) {
			lastKnownLocation = location;
			startTime = lastKnownLocation.getTime();
		} else {
			if (lastKnownLocation.distanceTo(location) > DEFAULT_RADIUS_METERS) {
				// We are still here!
				lastRecordedTime = System.currentTimeMillis();
			} else {
				// We've moved! Add the lastKnownLocation to the database
				ContentValues values = new ContentValues();
				values.put(LocationDB.KEY_STARTTIME, startTime);
				values.put(LocationDB.KEY_ENDTIME, lastRecordedTime);
				values.put(LocationDB.KEY_LATITUDE, lastKnownLocation.getLatitude());
				values.put(LocationDB.KEY_LONGITUDE, lastKnownLocation.getLongitude());
				values.put(LocationDB.KEY_NAME, "The name of this place");
				getDatabase().insert(LocationDB.LOCATION_TABLE_NAME, null, values);
				System.out.println("Created new stay from Google Location Service:\n " + 
						lastKnownLocation.getLatitude() + " , " + lastKnownLocation.getLongitude() + 
						"\nFrom: " + startTime + " to " + lastRecordedTime); 
				// Then set lastKnownLocation to this location and start looking for a new one!
				lastKnownLocation = location;
				startTime = 0;

			}
		}
	}


	// Returns any Location that matches the provided coordinates
	private ArrayList<Location> checkKnownLocations(double latitude, double longitude) {
		ArrayList<Location> locations = new ArrayList<Location>();
		float[] distance = new float[1];
		distance[0] = Float.MAX_VALUE;
		Cursor c = getDatabase().query(LocationDB.KNOWN_LOCATION_TABLE_NAME, null, ALL_KNOWN_LOCATIONS,null,null, null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				long lat = c.getLong(c.getColumnIndex(LocationDB.KEY_LATITUDE));
				long lon = c.getLong(c.getColumnIndex(LocationDB.KEY_LONGITUDE));
				long radius = c.getLong(c.getColumnIndex(LocationDB.KEY_RADIUS));
				android.location.Location.distanceBetween(lat, lon, latitude, longitude, distance);
				if (radius > distance[0]) {
					// This locationInstance matches the currently selcted location!
					String name = c.getString(c.getColumnIndex(LocationDB.KEY_NAME));
					locations.add(new Location(name, lat, lon, radius));
				}
				c.moveToNext();
			}
		}
		return locations;
	}

	public LocationServiceLogger() {}

	@Override
	public void onCreate() {
		super.onCreate();
		// CREATE a test entry
		ContentValues values = new ContentValues();
		values.put(LocationDB.KEY_STARTTIME, System.currentTimeMillis());
		values.put(LocationDB.KEY_ENDTIME, System.currentTimeMillis());
		values.put(LocationDB.KEY_LATITUDE, 34.414);
		values.put(LocationDB.KEY_LONGITUDE, -119.8440);
		getDatabase().insert(LocationDB.LOCATION_TABLE_NAME, null, values);
		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new LocationListener() {

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}

			@Override
			public void onLocationChanged(android.location.Location location) {
				addLocationEntry(location);
			}
		};
		// Get network provider location every minute
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES_MS*2, DEFAULT_RADIUS_METERS/2, locationListener);
		// Get GPS every 5 minutes
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES_MS*10, DEFAULT_RADIUS_METERS/2, locationListener);
	}

	private SQLiteDatabase getDatabase() {
		if ( db == null ) {
			db = LocationDB.getInstance(this.getApplicationContext()).getWritableDatabase();
		}
		return db;
	}

	public class LocationServiceLoggerBinder extends Binder {
		public LocationServiceLogger getService() {
			return LocationServiceLogger.this;
		}
	}

	public android.location.Location getCurrentLocation() {
		return lastKnownLocation;		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

}
