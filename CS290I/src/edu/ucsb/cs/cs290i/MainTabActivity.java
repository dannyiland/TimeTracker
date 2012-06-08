// Alohar API keys:
// Application ID: 89 
// Secret Key: 04834345f2729ceafc4c9f750ade319610560103 

package edu.ucsb.cs.cs290i;

import java.util.ArrayList;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TabHost;

import com.alohar.core.Alohar;
import com.alohar.user.callback.ALEventListener;
import com.alohar.user.content.data.ALEvents;

import edu.ucsb.cs.cs290i.service.detectors.location.LocationInstance;
import edu.ucsb.cs.cs290i.service.detectors.location.MapDisplay;

public class MainTabActivity extends TabActivity {
    protected static final long TEN_MINUTES = 1000 * 60 * 10;
    private static final int APP_ID = 89;
    private static final String API_KEY = "04834345f2729ceafc4c9f750ade319610560103";
	protected static final String PREF_KEY = "alohar_uid";
    protected String alohar_uid = "0";
    private Alohar mAlohar;
    private ArrayList<LocationInstance> visits = new ArrayList<LocationInstance>();
    
    private ALEventListener alEventListener = new ALEventListener() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleEvent(ALEvents event, Object data) {
           if (event == ALEvents.REGISTRATION_CALLBACK) {
                if (data instanceof String) {
                    alohar_uid = (String) data;
                	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    				prefs.edit().putString(PREF_KEY, alohar_uid).commit();
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

    private void startLocationTracking() {
        // Alohar Initialization
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        alohar_uid = prefs.getString(PREF_KEY, "0");
        Alohar.init(getApplication());
        mAlohar = Alohar.getInstance();
        mAlohar.getProfileManager().resetAccount();
        if (alohar_uid != "0") {
        	mAlohar.authenticate(alohar_uid, APP_ID, API_KEY, alEventListener);
        } else {
            mAlohar.register(APP_ID, API_KEY, alEventListener);
        }
        mAlohar.startServices();
    }
}