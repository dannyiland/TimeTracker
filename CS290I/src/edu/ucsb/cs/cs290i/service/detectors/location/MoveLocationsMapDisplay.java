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
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alohar.core.Alohar;
import com.alohar.user.callback.ALEventListener;
import com.alohar.user.content.ALPlaceManager;
import com.alohar.user.content.data.ALEvents;
import com.alohar.user.content.data.PlaceProfile;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

import edu.ucsb.cs.cs290i.R;


public class MoveLocationsMapDisplay extends MapActivity implements ALEventListener {
	private ArrayList<OverlayItem> aloharPlaces=new ArrayList<OverlayItem>();
	int sizeOfap = 0;
	private MapView map = null;
	private MyLocationOverlay me=null;
	private double UCSB_lat = 34.41498;
	private double UCSB_lon = -119.844;
	ArrayList<OverlayItem> items = null;
	ArrayList<LocationInstance> instances = null;
	ArrayList<Location> locations = null;
	long DEFAULT_RADIUS_METERS = 100;
	int MIN_MOVE_DISTANCE = 15;
	private static String MOVE_RETURN_NUM_CHANGED = "MOVED";
	private int numChanged = 0;


	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.relocate_menu, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent resultIntent = new Intent();
		switch (item.getItemId()) {
		case R.id.done:
			//	viewAllLocations(map.getMapCenter());
			resultIntent.putExtra(MOVE_RETURN_NUM_CHANGED, numChanged);
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
			return true;
		case R.id.cancel:
			resultIntent.putExtra(MOVE_RETURN_NUM_CHANGED, 0);
			setResult(Activity.RESULT_CANCELED, resultIntent);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_view);
		map = (MapView) findViewById(R.id.mapview);

		map.setBuiltInZoomControls(true);
		int[] latLon = getIntent().getIntArrayExtra("latLon");
		map.getController().setCenter(new GeoPoint(latLon[0], latLon[1]));
		displayKnownLocations(System.currentTimeMillis()-(24*60*60*1000), System.currentTimeMillis());
		getAloharPlaces();
	}


	private void getAloharPlaces() {
		ArrayList<OverlayItem> items;// TODO Auto-generated method stub
		ALPlaceManager mPlaceManager = Alohar.getInstance().getPlaceManager();
		mPlaceManager.searchPlaces(0, System.currentTimeMillis()/ 1000, ".*", 0, 100, this);


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


	private void displayKnownLocations(long startTimeMS, long endTimeMS) {
		Drawable marker=this.getResources().getDrawable(R.drawable.marker);
		if(marker != null) {
			marker.setBounds(0, 0, marker.getIntrinsicWidth(),
					marker.getIntrinsicHeight());
			ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
			ArrayList<Location> locations = LocationDB.getInstance(this).getKnownLocationsBetween(startTimeMS, endTimeMS, this.getApplicationContext());
			for (Location inst : locations) {
				items.add(new OverlayItem(getPoint(inst.getLatitude(), inst.getLongitude()), inst.getName(), "Radius: " + inst.getRadius()));

			}
			map.getOverlays().add(new SitesOverlay(marker, items));
			me=new MyLocationOverlay(this, map);
			map.getOverlays().add(me);
		}
		else {
			System.err.println("Marker is null WTF");
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


	private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
		private List<OverlayItem> items=new ArrayList<OverlayItem>();
		private Drawable marker=null;
		private OverlayItem inDrag=null;
		private ImageView dragImage=null;
		private int xDragImageOffset=0;
		private int yDragImageOffset=0;
		private int xDragTouchOffset=0;
		private int yDragTouchOffset=0;

		public SitesOverlay(Drawable marker, ArrayList<OverlayItem> add_items) {
			super(marker);
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
		public int size() {
			return(items.size());
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			
			final int action=event.getAction();
			final int x=(int)event.getX();
			final int y=(int)event.getY();
			boolean result=false;

			if (action==MotionEvent.ACTION_DOWN) {
				if(sizeOfap != aloharPlaces.size()) { 
					addOverlay(aloharPlaces);
					sizeOfap = aloharPlaces.size();
				}
				for (OverlayItem item : items) {
					Point p=new Point(0,0);

					map.getProjection().toPixels(item.getPoint(), p);

					if (hitTest(item, marker, x-p.x, y-p.y)) {
						result=true;
						inDrag=item;
						items.remove(inDrag);
						populate();

						xDragTouchOffset=0;
						yDragTouchOffset=0;

						setDragImagePosition(p.x, p.y);
						dragImage.setVisibility(View.VISIBLE);

						xDragTouchOffset=x-p.x;
						yDragTouchOffset=y-p.y;

						break;
					}
				}
			}
			else if (action==MotionEvent.ACTION_MOVE && inDrag!=null) {
				setDragImagePosition(x, y);

				result=true;
			}
			else if (action==MotionEvent.ACTION_UP && inDrag!=null) {
				dragImage.setVisibility(View.GONE);

				GeoPoint pt=map.getProjection().fromPixels(x-xDragTouchOffset,
						y-yDragTouchOffset);
				OverlayItem toDrop=new OverlayItem(pt, inDrag.getTitle(),
						inDrag.getSnippet());
				inDrag.getPoint().getLatitudeE6();
				inDrag.getPoint().getLongitudeE6();
				double newLat = ((double)pt.getLatitudeE6())/1000000;
				double newLon = ((double)pt.getLongitudeE6())/1000000;
				double oldLat = ((double)inDrag.getPoint().getLatitudeE6())/1000000;
				double oldLon = ((double)inDrag.getPoint().getLongitudeE6())/1000000;
				float[] distance = new float[1];
				android.location.Location.distanceBetween( oldLat, oldLon, newLat, newLon, distance);
				if (distance[0] > MIN_MOVE_DISTANCE) {
					System.out.println("Moved pin" + distance[0] + " meters");
					System.out.println("Relocated pin to : "+ ((double)pt.getLatitudeE6())/1000000 + " , " + ((double)pt.getLongitudeE6())/1000000);
					// Update the location's lat/lon in the database!
					numChanged = numChanged + LocationDB.getInstance(getApplicationContext()).moveLocation("name here", oldLat, oldLon, newLat, newLon);
				} else {
					System.out.print("Did not relocate pin! Display info!");
					Toast.makeText(MoveLocationsMapDisplay.this, inDrag.getSnippet() + " " + inDrag.getTitle(), Toast.LENGTH_SHORT).show();
				}
				items.add(toDrop);
				populate();
				inDrag=null;
				result=true;
			}

			return(result || super.onTouchEvent(event, mapView));
		}

		private void setDragImagePosition(int x, int y) {
			RelativeLayout.LayoutParams lp=
					(RelativeLayout.LayoutParams)dragImage.getLayoutParams();

			lp.setMargins(x-xDragImageOffset-xDragTouchOffset,
					y-yDragImageOffset-yDragTouchOffset, 0, 0);
			dragImage.setLayoutParams(lp);
		}
	}

	@Override
	public void handleEvent(ALEvents event, Object data) {
		if (event == ALEvents.PLACES_QUERY_CALLBACK) {
			ArrayList<PlaceProfile> places = (ArrayList<PlaceProfile>)data;
			for( PlaceProfile place : places) {
				aloharPlaces.add(new OverlayItem(new GeoPoint(place.getLatE6(), place.getLngE6()),
						place.getName(),place.getAddress()));
			}
			addOverlay(aloharPlaces);
		}
	}
}

