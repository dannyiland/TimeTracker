/*** 
  Copyright (c) 2008-2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.

  Adapted from _The Busy Coder's Guide to Advanced Android Development_
    http://commonsware.com/AdvAndroid

    Icons licensed under CC 3.0 BY-SA. Author: Nicolas Mollet
 */


package edu.ucsb.cs.cs290i.service.detectors.location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.alohar.core.Alohar;
import com.alohar.user.callback.ALEventListener;
import com.alohar.user.content.ALPlaceManager;
import com.alohar.user.content.data.ALEvents;
import com.alohar.user.content.data.ALLocation;
import com.alohar.user.content.data.UserStay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

import edu.ucsb.cs.cs290i.R;
import edu.ucsb.cs.cs290i.service.Action;
import edu.ucsb.cs.cs290i.service.DetectorService;
import edu.ucsb.cs.cs290i.service.DetectorService.DetectorServiceBinder;


public class MapDisplay extends MapActivity implements ALEventListener{
	private MapView map = null;
	private MyLocationOverlay me=null;
	private double UCSB_lat = 34.41498;
	private double UCSB_lon = -119.844;
	private static int INTENT_INDEX = 0;
	ArrayList<OverlayItem> items = null;
	private View popUp= null;
	ArrayList<LocationInstance> instances = null;
	ArrayList<Location> locations = null;
	long DEFAULT_RADIUS_METERS = 100;
	int MIN_MOVE_DISTANCE = 15;
	private ArrayList<UserStay> userStays;
	private static String MOVE_RETURN_NUM_CHANGED = "MOVED";
	ArrayList<OverlayItem> aloharItems = new ArrayList<OverlayItem>();
	protected DetectorService service;
	private static final int REQUEST_CODE = 1234;


	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName c, IBinder iBinder) {
			Log.e("MapDisplay", "Service Connected");
			DetectorServiceBinder binder = (DetectorServiceBinder) iBinder;
			service = binder.getService();

		}


		@Override
		public void onServiceDisconnected(ComponentName c) {
		}

	};

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.location_menu, menu);
		return true;
	}
	//    <item android:id="@+id/todays_activities" android:title="View Detected Actions"></item>    
	//    <item android:id="@+id/update_locations" android:title="Reposition Locations"></item>
	//    <item android:id="@+id/edit_locations" android:title="All Saved Locations"></item>
	//    <item android:id="@+id/voice_command" android:title="Voice Command"></item>
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.edit_locations:
			viewAllLocations(map.getMapCenter());
			return true;
		case R.id.todays_activities:
			displayActions();
			return true;
		case R.id.update_locations:
			updateLocations(map.getMapCenter());
			return true;
		case R.id.voice_command:
			performVoiceCommand();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	private void viewAllLocations(GeoPoint mapCenter) {
		// TODO Auto-generated method stub

	}

	private void performVoiceCommand() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe a Location...");
		startActivityForResult(intent, REQUEST_CODE);
		// TODO Auto-generated method stub

	}


	public static void getAddressFromLocation(
			final Location location, final Context context, final Handler handler) {
		Thread thread = new Thread() {
			@Override public void run() {
				Geocoder geocoder = new Geocoder(context, Locale.getDefault());   
				String result = null;
				try {
					List<Address> list = geocoder.getFromLocation(
							location.getLatitude(), location.getLongitude(), 1);
					if (list != null && list.size() > 0) {
						Address address = list.get(0);
						// sending back first address line and locality
						result = address.getAddressLine(0) + ", " + address.getLocality();
					}
				} catch (IOException e) {
				} finally {
					Message msg = Message.obtain();
					msg.setTarget(handler);
					if (result != null) {
						msg.what = 1;
						Bundle bundle = new Bundle();
						bundle.putString("address", result);
						msg.setData(bundle);
					} else 
						msg.what = 0;
					msg.sendToTarget();
				}
			}
		};
		thread.start();
	}

	private void updateLocations(GeoPoint mapCenter) {
		Intent intent = new Intent().setClass(this, MoveLocationsMapDisplay.class);
		int[] latLon = new int[2];
		latLon[0] = mapCenter.getLatitudeE6();
		latLon[1] = mapCenter.getLongitudeE6();
		intent.putExtra("latLon", latLon);
		startActivityForResult(intent, INTENT_INDEX );
		displayLocations(System.currentTimeMillis()-(24*60*60*1000), System.currentTimeMillis());
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) { 
		super.onActivityResult(requestCode, resultCode, data); 
		// voice command
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
		{
			// Populate the wordsList with the String values the recognition engine thought it heard
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			Spinner geocodedSpinner = (Spinner)findViewById(R.id.geocoded_string_results);
			geocodedSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, matches));
		} else if(requestCode == 0) {
			if (resultCode == Activity.RESULT_OK) { 
				int numUpdated = data.getIntExtra(MOVE_RETURN_NUM_CHANGED, 0);
				System.out.println("Updated " + numUpdated + "locations");
				// TODO Switch tabs using the index.
			} 		
		} 
		displayLocations(System.currentTimeMillis()-(24*60*60*1000), System.currentTimeMillis());
	}


	private void displayActions() {
		System.out.println(userStays);
		System.out.println(service);
		ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		if (userStays != null && service != null) { 
			for (UserStay stay : userStays) {
				long endTime = stay.getEndTime()*1000;
				UserStay last = Alohar.getInstance().getPlaceManager().getLastKnownStay();
				ALLocation current = Alohar.getInstance().getPlaceManager().getCurrentLocation();
				if(last != null && current != null) { 
				Location lastLoc = new Location("blah", ((double)last.getCentroidLatE6())/1000000, ((double)last.getCentroidLngE6())/1000000, DEFAULT_RADIUS_METERS);
				if(lastLoc.matches(new Location("blah", current.getLatitude(), current.getLongitude(), DEFAULT_RADIUS_METERS))){
					endTime = System.currentTimeMillis();
				}
				}
				List<Action> events = service.matchActions(stay.getStartTime()*1000,endTime);
				Date start = new Date(stay.getStartTime());
				Date end = new Date(stay.getEndTime());
				String times = "From " + start.getHours() + ":" + start.getMinutes() + " to " + end.getHours() + ":" + end.getMinutes() + "\n";
				String actionDescription = "Actions:\n";
				for(Action e: events) {
					actionDescription += e.getName() + " : " + e.getDescription() + "\n";
				}
				items.add(new OverlayItem(getPoint(((double)stay.getCentroidLatE6())/1000000,
						((double)stay.getCentroidLngE6())/1000000),
						stay.getSelectedPlace().getName(),
						times + actionDescription));
			}
			addOverlay(items);
		}
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent serviceIntent = new Intent(getApplicationContext(), DetectorService.class);
		getApplicationContext().startService(serviceIntent);
		getApplicationContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

		setContentView(R.layout.map_view);
		map = (MapView) findViewById(R.id.mapview);

		map.setBuiltInZoomControls(true);
		map.getController().setCenter(getPoint(UCSB_lat, UCSB_lon));
		displayLocations(System.currentTimeMillis()-(24*60*60*1000), System.currentTimeMillis());
	}


	public GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0),
				(int)(lon*1000000.0)));
	}

	@SuppressWarnings("unchecked")
	public void handleEvent(ALEvents event, Object data) {
		if (event == ALEvents.USERSTAYS_QUERY_CALLBACK || event == ALEvents.USERSTATS_OF_A_PLACE_CALLBACK) {
			ArrayList<LocationInstance> locs = new ArrayList<LocationInstance>();
			aloharItems = new ArrayList<OverlayItem>();
			userStays = (ArrayList<UserStay>)data;
			for (UserStay stay : userStays) {
				aloharItems.add(new OverlayItem(getPoint(((double)stay.getCentroidLatE6())/1000000,
						((double)stay.getCentroidLngE6())/1000000),
						stay.getSelectedPlace().getName(),
						"Visited from " + new Date(stay.getStartTime()*1000).toGMTString() + " to " + new Date(1000*stay.getEndTime()).toGMTString()));
			}
			
			addOverlay(aloharItems);
		}
	}

	private void addOverlay(ArrayList<OverlayItem> items) {
		Drawable marker=this.getResources().getDrawable(R.drawable.marker);
		if(marker != null) {
			marker.setBounds(0, 0, marker.getIntrinsicWidth(),	marker.getIntrinsicHeight());
		}
		if(items.size() != 0) {
			map.getOverlays().add(new SitesOverlay(marker, items));
			me=new MyLocationOverlay(this, map);
			map.getOverlays().add(me);
		}
	}

	private void displayLocations(long startTimeMS, long endTimeMS) {
		// Add Alohar Stays in time range and all detected LocationInstances from Google.

		ALPlaceManager a = Alohar.getInstance().getPlaceManager();
		a.searchUserStays(UCSB_lat, UCSB_lon, 50000, 0, endTimeMS/1000, true, 100, this);

		Drawable marker=this.getResources().getDrawable(R.drawable.marker);
		if(marker != null) {
			marker.setBounds(0, 0, marker.getIntrinsicWidth(),
					marker.getIntrinsicHeight());
			ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
			ArrayList<Location> locations = LocationDB.getInstance(this).getAllKnownLocations(this.getApplicationContext());
			ArrayList<Location> display = new ArrayList<Location>();
			ArrayList<LocationInstance> instances = LocationDB.getInstance(this).getLocationsBetween(startTimeMS, endTimeMS, this.getApplicationContext());
			// Merge locations with already known locations within the default radius.
			for (LocationInstance inst : instances) {
				boolean found = false;
				if (locations != null && locations.size() > 0){ 
					for( Location loc : locations) {
						if (loc.matches(inst)) {
							found = true;
							break; // We don't need to map this inst, it's already mapped.
						}
					}
				}


				if(!found) {
					locations.add(new Location(inst.getNames(), inst.getLatitude(), inst.getLongitude(),DEFAULT_RADIUS_METERS));
					items.add(new OverlayItem(getPoint(inst.getLatitude(), inst.getLongitude()), inst.getNames(), "Visited from " + new Date(inst.getStartTime()*1000).toGMTString() + " to " + new Date(inst.getEndTime()*1000).toGMTString()));
					found = false;
				}
			}



			for (Location inst : locations) {
				boolean found = false;
				if (locations != null && locations.size() > 0){ 
					for( Location disp : display) {
						if (inst.matches(disp)) {
							found = true;
							break; // We don't need to map this inst, it's already mapped.
						}
					}
				}

				if(!found) {
					items.add(new OverlayItem(getPoint(inst.getLatitude(), inst.getLongitude()), inst.getName(), "Known location of type" + inst.getType()));
				}
			}

			//		items.add(new OverlayItem(getPoint(UCSB_lat, UCSB_lon), "UCSB", "Visited from " + new Date(System.currentTimeMillis()-1000000).toGMTString() + " to " + new Date(System.currentTimeMillis()).toGMTString()));

			map.getOverlays().add(new SitesOverlay(marker, items));
			me=new MyLocationOverlay(this, map);
			map.getOverlays().add(me);


		} else {
			System.err.println("Marker is null WTF");
		}
	}


	private class SitesOverlay extends BalloonItemizedOverlay<OverlayItem> {
		private List<OverlayItem> items=new ArrayList<OverlayItem>();

		private Drawable marker=null;
		private OverlayItem inDrag=null;
		private ImageView dragImage=null;
		private int xDragImageOffset=0;
		private int yDragImageOffset=0;
		private int xDragTouchOffset=0;
		private int yDragTouchOffset=0;

		public SitesOverlay(Drawable marker, ArrayList<OverlayItem> add_items) {
			super(marker, map);
			if(add_items != null) {
				for ( OverlayItem item : add_items) {
					items.add(item);
				}
			}
			this.marker=marker;
			dragImage=(ImageView)findViewById(R.id.drag);
			xDragImageOffset=dragImage.getDrawable().getIntrinsicWidth()/2;
			yDragImageOffset=dragImage.getDrawable().getIntrinsicHeight();
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return(items.get(i));
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);

			boundCenterBottom(marker);
		}

		@Override
		public boolean onBalloonTap(int index) {
			System.out.println("Tapped item " + index);
			return true;
		}
		@Override
		public int size() {
			return(items.size());
		}

	}
}
