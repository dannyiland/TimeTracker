// Alohar API keys:
// Application ID: 89 
// Secret Key: 04834345f2729ceafc4c9f750ade319610560103 

package edu.ucsb.cs.cs290i;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import com.alohar.core.Alohar;
import com.alohar.user.callback.ALEventListener;
import com.alohar.user.content.data.ALEvents;
import com.alohar.user.content.data.PlaceProfile;
import com.alohar.user.content.data.UserStay;

import edu.ucsb.cs.cs290i.service.Action;
import edu.ucsb.cs.cs290i.service.DetectorService;
import edu.ucsb.cs.cs290i.service.DetectorService.DetectorServiceBinder;
import edu.ucsb.cs.cs290i.service.detectors.location.MapDisplay;

public class MainTabActivity extends TabActivity {
    protected static final long TEN_MINUTES = 1000 * 60 * 10;
    private static final int APP_ID = 89;
    private static final String API_KEY = "04834345f2729ceafc4c9f750ade319610560103";
    protected String alohar_uid = "0";
    private Alohar mAlohar;
    private ArrayList<LocationInstance> visits = new ArrayList<LocationInstance>();

    private ALEventListener alEventListener = new ALEventListener() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleEvent(ALEvents event, Object data) {
            if (event == ALEvents.USERSTATS_OF_A_PLACE_CALLBACK) {
                ArrayList<UserStay> stays = (ArrayList<UserStay>) data;
                for (UserStay stay : stays) {
                    LocationInstance newLoc = new LocationInstance(stay.getCandidates(), stay.getCentroidLatE6(),
                            stay.getCentroidLngE6(), stay.getStartTime(), stay.getEndTime());
                    visits.add(newLoc);
                }
            } else if (event == ALEvents.PLACES_QUERY_CALLBACK) {
                ArrayList<PlaceProfile> places = (ArrayList<PlaceProfile>) data;
                // Post process the places
                for (PlaceProfile place : places) {
                    mAlohar.getPlaceManager().getUserStays(place, alEventListener);
                }
            } else if (event == ALEvents.REGISTRATION_CALLBACK) {
                if (data instanceof String) {
                    alohar_uid = (String) data;
                }
            } else if (event == ALEvents.GENERAL_ERROR_CALLBACK
                    || event == ALEvents.SERVER_ERROR_CALLBACK) {
                // Fail to register or authenticate

            }
        }
    };


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startLocationTracking();

		setContentView(R.layout.main);

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Reusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, ActionsStatsActivity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost
				.newTabSpec("actions")
				.setIndicator("Actions",
						res.getDrawable(R.drawable.ic_tab_actions))
				.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, MapDisplay.class);
		spec = tabHost
				.newTabSpec("locations")
				.setIndicator("Locations",
						res.getDrawable(R.drawable.ic_tab_locations))
				.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(1);

	}


    private ArrayList<LocationInstance> getLocations(long start, long end) {
        mAlohar.getPlaceManager().searchPlaces(start, end, "null", 1, 1000, alEventListener);
        return visits;
    }


    private void startLocationTracking() {
        // Alohar Initialization
        alohar_uid = getSavedAloharId();
        Alohar.init(getApplication());
        mAlohar = Alohar.getInstance();
        if (alohar_uid != "0") {
            mAlohar.authenticate(alohar_uid, APP_ID, API_KEY, alEventListener);
        } else {
            mAlohar.register(APP_ID, API_KEY, alEventListener);
        }
        mAlohar.startServices();
    }


    private String getSavedAloharId() {
        // TODO: persist this variable, alohar_id, across sessions!
        return "0";
    }
}