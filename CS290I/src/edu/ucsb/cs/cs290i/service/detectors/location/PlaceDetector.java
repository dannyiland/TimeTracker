package edu.ucsb.cs.cs290i.service.detectors.location;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.alohar.core.Alohar;
import com.alohar.user.callback.ALEventListener;
import com.alohar.user.callback.ALPlaceEventListener;
import com.alohar.user.content.data.ALEvents;
import com.alohar.user.content.data.PlaceProfile;
import com.alohar.user.content.data.UserStay;
import com.google.android.maps.MapView;

import edu.ucsb.cs.cs290i.service.EventDb;
import edu.ucsb.cs.cs290i.service.detectors.Detector;
import edu.ucsb.cs.cs290i.service.detectors.Event;

public class PlaceDetector extends Detector {
	static ArrayList<LocationInstance> visits = new ArrayList<LocationInstance>();	
	SQLiteDatabase db = null;
	
	private PlaceDetector(String... params){
		super(params);
	}

	ALEventListener alEventListener = new ALEventListener() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleEvent(ALEvents event, Object data) {
			// Get all user Stays and LocationInstances
			if (event == ALEvents.USERSTATS_OF_A_PLACE_CALLBACK) {
				ArrayList<UserStay> stays = (ArrayList<UserStay>) data;
				visits.clear();
				for (UserStay stay : stays) {
					LocationInstance newLoc = new LocationInstance(stay.getSelectedPlace().getName(), stay.getCentroidLatE6(),
							stay.getCentroidLngE6(), stay.getStartTime(), stay.getEndTime());
					visits.add(newLoc);
				}
			// Called as result of search for places
			} else if (event == ALEvents.PLACES_QUERY_CALLBACK) {
				ArrayList<PlaceProfile> places = (ArrayList<PlaceProfile>) data;
				// Post process the places, retrieve user stays!
				for (PlaceProfile place : places) {
					Alohar.getInstance().getPlaceManager().getUserStays(place, alEventListener);
				}
			}
		}
	};

	private ALPlaceEventListener realTimeListener = new ALPlaceEventListener() {
		// This is called when the user arrives at a new place
		@Override
		public void onArrival(double latitude, double longitude) {
			// Process the arrival event

		}

		// This is called when the user departs from a place
		@Override
		public void onDeparture(double latitude, double longitude) {
			// Process the departure event
		}

		// This is called when the system detects the name of the place the user stays at
		// or there is some significant attribute change of the userstay.
		@Override
		public void onUserStayChanged(UserStay newUserStay){
			// Process the userstay change. 
		}    
	};


    @Override
    public void start(Context c) {
        db = EventDb.getInstance(c).getWritableDatabase();
    }


    @Override
    public void stop(Context c) {
        if (db != null) {
            db.close();
        }
    }
    
    public static int metersToRadius(float meters, MapView map, double latitude) {
        return (int) (map.getProjection().metersToEquatorPixels(meters) * (1/ Math.cos(Math.toRadians(latitude))));         
    }

	// Listen for place events 
	@Override
	public List<Event> getEvents(long startTime, long endTime) {
		List<Event> ret = new ArrayList<Event>();
		visits.clear();
		Alohar.getInstance().getPlaceManager().searchPlaces(startTime, endTime, "null", 1, 1000, alEventListener);
		for(LocationInstance visit:visits) {
			Event e = new Event("Place Detector", visit.getLatitude()+","+visit.getLongitude(), 1, this);
			ret.add(e);
		}
		return ret;
	}

	public void start() {
		// TODO Auto-generated method stub
		
	}

	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
}

