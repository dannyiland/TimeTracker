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
	private SQLiteDatabase db;
    private final IBinder binder = new LocationServiceLoggerBinder();

	private android.location.Location lastKnownLocation = null;
	private long lastRecordedTime=0;
	private float DEFAULT_RADIUS_METERS = 100;

	// Adds a location to database on departure, with arrival and departure time, lat/long, and name if known.
	public void addLocationEntry(android.location.Location location) {
		if (lastKnownLocation.distanceTo(location) > DEFAULT_RADIUS_METERS) {
			// We are still here!
			lastRecordedTime = System.currentTimeMillis();
		} else {
			// We've moved! Add the lastKnownLocation to the database
			ContentValues values = new ContentValues();
			values.put(LocationDB.KEY_STARTTIME, lastKnownLocation.getTime());
			values.put(LocationDB.KEY_ENDTIME, lastRecordedTime);
			values.put(LocationDB.KEY_LATITUDE, lastKnownLocation.getLatitude());
			values.put(LocationDB.KEY_LONGITUDE, lastKnownLocation.getLongitude());

			ArrayList<Location> thisLocation = checkKnownLocations(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
			if(thisLocation.size() >= 1) {
				String locationsString = "";
				for (Location l : thisLocation) {
					locationsString = locationsString + l.getName() + ",";
				}
				values.put(LocationDB.KEY_KNOWN, locationsString.substring(0, locationsString.length() -1)); // Strip last ,
			}

			getDatabase().insert(LocationDB.LOCATION_TABLE_NAME, null, values);
			// Then set lastKnownLocation to this location!
			lastKnownLocation = location;
		}
	}

	// Returns any Location that matches the provided coordinates
	private ArrayList<Location> checkKnownLocations(double latitude, double longitude) {
		ArrayList<Location> locations = new ArrayList<Location>();
		float[] distance = new float[1];
		distance[0] = Float.MAX_VALUE;
		Cursor c = getDatabase().query(LocationDB.KNOWN_LOCATION_TABLE_NAME, null, LocationDB.ALL_KNOWN_LOCATIONS,null,null, null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				double lat = c.getDouble(c.getColumnIndex(LocationDB.KEY_LATITUDE));
				double lon = c.getDouble(c.getColumnIndex(LocationDB.KEY_LONGITUDE));
				double radius = c.getDouble(c.getColumnIndex(LocationDB.KEY_RADIUS));
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

	public LocationServiceLogger() {
		Context c = this;
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
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

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

	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

}
