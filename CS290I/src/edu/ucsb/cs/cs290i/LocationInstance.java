package edu.ucsb.cs.cs290i;

import java.util.ArrayList;

public class LocationInstance {
	private long startTime;
	private long endTime;
	private Location location;
	
	public LocationInstance(ArrayList candidates, int centroidLatE6, int centroidLngE6, long startTime, long endTime) {
		
		this.location = new Location("NAME", centroidLatE6, centroidLngE6, 200); //TODO: Get name from Candidates;
		this.startTime = startTime;
		this.endTime = endTime;
	}

}
