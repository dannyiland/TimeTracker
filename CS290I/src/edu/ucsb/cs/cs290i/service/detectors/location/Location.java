package edu.ucsb.cs.cs290i.service.detectors.location;

public class Location {

	private double latitude;
	private double longitude;
	private String name;
	private long radius; // in meters
	private long timestamp;
	private String[] types = null;
	
	public Location(String name, double lat, double lon, long radius, String... type) {
		this.name = name;
		this.latitude = lat;
		this.longitude = lon;
		this.radius = radius;
		if ( type != null ) {
			types = new String[type.length];
			for( int i=0; i< type.length;i++) {
				types[i] = type[i];
			}
		}
		//Calculate those others.
	
	}

	public String toString() {
		return latitude + "," + longitude + ":" + name;
	}

	public String getName() {
		return name;
	}

	public long getRadius() {
		return radius;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}


	// Returns true if the location instance is within the bounds, false otherwise.
	public boolean matches(LocationInstance candidate) {
		if(computeDistanceInMeters(candidate, this) > this.getRadius()) {
			return false;
		} else {
			return true;
		}
	}

	// Returns true if the location instance is within the bounds, false otherwise.
		public boolean matches(Location candidate) {
			if(computeDistanceInMeters(candidate, this) > this.getRadius()) {
				return false;
			} else {
				return true;
			}
		}

	private float computeDistanceInMeters(LocationInstance candidate, Location loc) {
		float[] results = new float[3];

		android.location.Location.distanceBetween(candidate.getLatitude(), candidate.getLongitude(),
				loc.getLatitude(), loc.getLongitude(), results);
		return results[0];
	}
	private float computeDistanceInMeters(Location candidate, Location loc) {
		float[] results = new float[3];

		android.location.Location.distanceBetween(candidate.getLatitude(), candidate.getLongitude(),
				loc.getLatitude(), loc.getLongitude(), results);
		return results[0];
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long time) {
		timestamp = time;
	}

	public String getType() {
		// TODO Auto-generated method stub
		String returnString = "";
		if ( types != null ) {
			for(int i=0;i<types.length;i++) {
				returnString = returnString + types[i];
			}
		}
		return returnString;
	}
}
