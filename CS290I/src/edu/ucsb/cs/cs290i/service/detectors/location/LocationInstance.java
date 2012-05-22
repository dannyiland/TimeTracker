package edu.ucsb.cs.cs290i.service.detectors.location;

public class LocationInstance {
	private long startTime;
	private long endTime;
	private double latitude;
	private double longitude;


    public LocationInstance(double latitude, double longitude, long startTime, long endTime) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.startTime = startTime;
		this.endTime = endTime;
	}


	/**
     * @return the startTime
     */
    public synchronized long getStartTime() {
        return startTime;
    }

    /**
     * @return the endTime
     */
    public synchronized long getEndTime() {
        return endTime;
    }


	public double getLatitude() {
		return latitude;
	}


	public double getLongitude() {
		return longitude;
	}

}
