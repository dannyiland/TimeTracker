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
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alohar.core.Alohar;
import com.alohar.user.callback.ALEventListener;
import com.alohar.user.content.ALPlaceManager;
import com.alohar.user.content.data.ALEvents;
import com.alohar.user.content.data.UserStay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

import edu.ucsb.cs.cs290i.R;


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
			displayActivities();
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
		// TODO Auto-generated method stub

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
		switch(requestCode) { 
		case (0) : { 
			if (resultCode == Activity.RESULT_OK) { 
				int numUpdated = data.getIntExtra(MOVE_RETURN_NUM_CHANGED, 0);
				System.out.println("Updated " + numUpdated + "locations");
				// TODO Switch tabs using the index.
			} 
			break; 
		} 
		} 
		displayLocations(System.currentTimeMillis()-(24*60*60*1000), System.currentTimeMillis());
	}


	private void displayActivities() {
		// Clear Map

		// Add a pin for each activity in the last 24 hours

	}

	private void addLocation(GeoPoint mapCenter) {
		Intent intent = new Intent().setClass(this, AddLocationActivity.class);
		int[] latLon = new int[2];
		latLon[0] = mapCenter.getLatitudeE6();
		latLon[1] = mapCenter.getLongitudeE6();
		intent.putExtra("latLon", latLon);
		startActivity(intent);
		// Ask for name and radius
		// Optional: Ask for string representation of place name, and accept voice!
		// Create a pin at e map center with name, radius

		// Allow user to reposition pin

		// Record final lat/lon 
	}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

	/**
	 * Creates a new point with the name given at the specified location.
	 * @param name 
	 * @param geocode
	 */
	private void addLocation(String name, String geocode) {
		//Take a name, a string that is run through a geocoder (voice or txt) 
		// get lat/lon
		// Ask for name and radius
		android.location.Geocoder geoCoder = new android.location.Geocoder(this, Locale.getDefault());    
		try {
			List<android.location.Address> addresses = geoCoder.getFromLocationName(geocode, 5);
			String add = "";
			String toAdd = "";
			if (addresses.size() > 0) {
				GeoPoint point = new GeoPoint(
						(int) (addresses.get(0).getLatitude() * 1E6), 
						(int) (addresses.get(0).getLongitude() * 1E6));
				if (addresses.get(0).getFeatureName() != null) {
					add = add + addresses.get(0).getFeatureName();
				}
				int i = 0;
				while(addresses.get(0).getAddressLine(i) != null) {
					toAdd = addresses.get(0).getAddressLine(i)+"\n";
					i++;

				}

				items.add(new OverlayItem(point, add, toAdd));
			}    
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Create a pin at map center with name, radius

		// Allow user to reposition pin

		// Record final lat/lon 
	}

	@SuppressWarnings("unchecked")
	public void handleEvent(ALEvents event, Object data) {
		if (event == ALEvents.USERSTAYS_QUERY_CALLBACK || event == ALEvents.USERSTATS_OF_A_PLACE_CALLBACK) {
			ArrayList<LocationInstance> locs = new ArrayList<LocationInstance>();
			aloharItems = new ArrayList<OverlayItem>();
			userStays = (ArrayList<UserStay>)data;
			for (UserStay stay : userStays) {
				locs.add(new LocationInstance(stay.getSelectedPlace().getName(),
						((double)stay.getCentroidLatE6())/1000000,
						((double)stay.getCentroidLngE6())/1000000,
						stay.getStartTime(),
						stay.getEndTime()));
				aloharItems.add(new OverlayItem(getPoint(((double)stay.getCentroidLatE6())/1000000,
						((double)stay.getCentroidLngE6())/1000000),
						stay.getSelectedPlace().getName(),
						"Visited from " + new Date(stay.getStartTime()).toGMTString() + " to " + new Date(stay.getEndTime()).toGMTString()));
			}
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
		a.searchUserStays(UCSB_lat, UCSB_lon, 50000, 0, System.currentTimeMillis()/1000, true, 100, this);

		Drawable marker=this.getResources().getDrawable(R.drawable.marker);
		if(marker != null) {
			marker.setBounds(0, 0, marker.getIntrinsicWidth(),
					marker.getIntrinsicHeight());
			ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
			ArrayList<LocationInstance> instances = LocationDB.getInstance(this).getAllKnownLocations(this.getApplicationContext());
			ArrayList<Location> locations = new ArrayList<Location>();
			// Merge locations with already known locations within the default radius.
			boolean found = false;
			for (LocationInstance inst : instances) {
				for( Location loc : locations) {
					if (loc.matches(inst)) {
						found = true;
						break; // We don't need to map this inst, it's already mapped.
					}
				}

				if(!found) {
					locations.add(new Location(inst.getNames(), inst.getLatitude(), inst.getLongitude(),DEFAULT_RADIUS_METERS));
					items.add(new OverlayItem(getPoint(inst.getLatitude(), inst.getLongitude()), inst.getNames(), "Visited from " + new Date(inst.getStartTime()).toGMTString() + " to " + new Date(inst.getEndTime()).toGMTString()));
					found = false;
				}
			}

			map.getOverlays().add(new SitesOverlay(marker, items));
			me=new MyLocationOverlay(this, map);
			map.getOverlays().add(me);
			addOverlay(aloharItems);


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

		//		@Override
		//		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		//			final int action=event.getAction();
		//			final int x=(int)event.getX();
		//			final int y=(int)event.getY();
		//			boolean result=false;
		//
		//			if (action==MotionEvent.ACTION_DOWN) {
		//				for (OverlayItem item : items) {
		//					Point p=new Point(0,0);
		//
		//					map.getProjection().toPixels(item.getPoint(), p);
		//
		//					if (hitTest(item, marker, x-p.x, y-p.y)) {
		//						result=true;
		//						inDrag=item;
		//						items.remove(inDrag);
		//						populate();
		//
		//						xDragTouchOffset=0;
		//						yDragTouchOffset=0;
		//
		//						setDragImagePosition(p.x, p.y);
		//						dragImage.setVisibility(View.VISIBLE);
		//
		//						xDragTouchOffset=x-p.x;
		//						yDragTouchOffset=y-p.y;
		//
		//						break;
		//					}
		//				}
		//			}
		//			else if (action==MotionEvent.ACTION_MOVE && inDrag!=null) {
		//				setDragImagePosition(x, y);
		//				
		//				result=true;
		//			}
		//			else if (action==MotionEvent.ACTION_UP && inDrag!=null) {
		//				dragImage.setVisibility(View.GONE);
		//
		//				GeoPoint pt=map.getProjection().fromPixels(x-xDragTouchOffset,
		//						y-yDragTouchOffset);
		//				OverlayItem toDrop=new OverlayItem(pt, inDrag.getTitle(),
		//						inDrag.getSnippet());
		//				inDrag.getPoint().getLatitudeE6();
		//				inDrag.getPoint().getLongitudeE6();
		//				float[] distance = new float[1];
		//				android.location.Location.distanceBetween( ((double)pt.getLatitudeE6()/1000000), ((double)pt.getLongitudeE6())/1000000, ((double)inDrag.getPoint().getLatitudeE6())/1000000, ((double)inDrag.getPoint().getLongitudeE6())/1000000, distance);
		//				if (distance[0] > MIN_MOVE_DISTANCE) {
		//					System.out.println("Moved pin" + distance[0] + " meters");
		//					System.out.println("Relocated pin to : "+ ((double)pt.getLatitudeE6())/1000000 + " , " + ((double)pt.getLongitudeE6())/1000000);
		//					// Update the location's lat/lon in the database!
		//				} else {
		//					System.out.print("Did not relocate pin! Display info!");
		////					popUp = getLayoutInflater().inflate(R.layout.popup, map, false);
		////					MapView.LayoutParams mapParams = new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
		////	                        ViewGroup.LayoutParams.WRAP_CONTENT,
		////	                        inDrag.getPoint(), 0,-50,
		////	                        MapView.LayoutParams.BOTTOM_CENTER);
		////					map.addView(popUp, mapParams);
		////					//POPUP for this pin!
		//				}
		//				items.add(toDrop);
		//				populate();
		//				inDrag=null;
		//				result=true;
		//			}
		//
		//			return(result || super.onTouchEvent(event, mapView));
		//		}

		private void setDragImagePosition(int x, int y) {
			RelativeLayout.LayoutParams lp=
					(RelativeLayout.LayoutParams)dragImage.getLayoutParams();

			lp.setMargins(x-xDragImageOffset-xDragTouchOffset,
					y-yDragImageOffset-yDragTouchOffset, 0, 0);
			dragImage.setLayoutParams(lp);
		}
	}
}
