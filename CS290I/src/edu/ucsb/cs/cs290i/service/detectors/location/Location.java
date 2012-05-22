package edu.ucsb.cs.cs290i.service.detectors.location;

public class Location {

	private int latitude;
	private int longitude;
	private String name;
	private long radius; // in meters

	public Location(String name, int latitude, int longitude, long radius) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
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
		// TODO Auto-generated method stub
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}


	// Returns true if the location instance is within the bounds, false otherwise.
	private boolean matches(LocationInstance candidate) {
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
}
