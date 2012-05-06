package edu.ucsb.cs.cs290i;

class Location {

	private int latitude;
	private int longitude;
	private String name;
	private long radius; // in meters
	
	public Location(String name, int latitude, int longitude, long radius) {
		this.latitude = latitude;
		this.name = name;
		this.longitude = longitude;
		this.radius = radius;
	}
	
	public String toString() {
		return latitude + "," + longitude + ":" + name;
		
	}
}
