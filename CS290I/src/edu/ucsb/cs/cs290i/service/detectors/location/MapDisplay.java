/***re   
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

import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

import edu.ucsb.cs.cs290i.R;
import edu.ucsb.cs.cs290i.service.Action;
import edu.ucsb.cs.cs290i.service.EventDb;


public class MapDisplay extends MapActivity {
	private MapView map = null;
	private MyLocationOverlay me=null;
	private double UCSB_lat = 34.41498;
	private double UCSB_lon = -119.844;
	private SQLiteDatabase db;

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_view);
		map = (MapView) findViewById(R.id.mapview);
		map.setBuiltInZoomControls(true);
		map.getController().setCenter(getPoint(UCSB_lat, UCSB_lon));
		Drawable marker=this.getResources().getDrawable(R.drawable.marker);
		if(marker != null) {
			marker.setBounds(0, 0, marker.getIntrinsicWidth(),
					marker.getIntrinsicHeight());

			
			db = EventDb.getInstance(this).getWritableDatabase();
			ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
			for(LocationInstance inst : LocationDB.getAllKnownLocations(this.getApplicationContext())) {
				items.add(new OverlayItem(getPoint(inst.getLatitude(), inst.getLongitude()), inst.getNames(), "Visited from " + inst.getStartTime() + " to " + inst.getEndTime()));
			}
			map.getOverlays().add(new SitesOverlay(marker, items));
			me=new MyLocationOverlay(this, map);
			map.getOverlays().add(me);

		} else {
			System.err.println("Marker is null WTF");
		}
	}


	public GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0),
				(int)(lon*1000000.0)));
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
}
