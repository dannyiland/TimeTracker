/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package edu.ucsb.cs.cs290i.service.detectors.location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.maps.OverlayItem;

import edu.ucsb.cs.cs290i.R;

/**
 * A view representing a MapView marker information balloon.
 * <p>
 * This class has a number of Android resource dependencies:
 * <ul>
 * <li>drawable/balloon_overlay_bg_selector.xml</li>
 * <li>drawable/balloon_overlay_close.png</li>
 * <li>drawable/balloon_overlay_focused.9.png</li>
 * <li>drawable/balloon_overlay_unfocused.9.png</li>
 * <li>layout/balloon_map_overlay.xml</li>
 * </ul>
 * </p>
 * 
 * @author Jeff Gilfelt
 *
 */
public class BalloonOverlayView extends FrameLayout implements OnSeekBarChangeListener {

	private static final int MAX_RADIUS = 300;
	private LinearLayout layout;
	private TextView title;
	private TextView snippet;
	private Spinner name;
	private SeekBar sb;
	private Button saveLocation;
	private OverlayItem overlayItem;
	private Spinner spinner;
	private Button button1;
	private Button button2;
	private TextView radiusString;
	private final int DEFAULT_RADIUS =50;
	private int radius = DEFAULT_RADIUS;

	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * Remember this Location
	 * Best Guess Name
	 * Visited form ... to ...
	 * Spinner of name guesses, with "Enter Custom Text" as option
	 * Radius  -----RadiusSeekBar--------
	 * 
	 * @param context - The activity context.
	 * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
	 * when rendering this view.
	 */
	public BalloonOverlayView(Context context, int balloonBottomOffset) {

		super(context);

		setPadding(10, 0, 10, balloonBottomOffset);
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.balloon_overlay, layout);
		saveLocation = (Button) v.findViewById(R.id.button1);
		saveLocation.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveLocation();
			}
		});
		title = (TextView) v.findViewById(R.id.balloon_item_title); // Best guess name
		snippet = (TextView) v.findViewById(R.id.balloon_item_snippet); // Time info
		name = (Spinner) v.findViewById(R.id.nameSpinner); // Other name guesses
		sb = (SeekBar) v.findViewById(R.id.radiusBar); // Radius Bar
		sb.setMax(MAX_RADIUS);
		sb.setProgress(DEFAULT_RADIUS);
		sb.setOnSeekBarChangeListener(this);

		radiusString = (TextView) v.findViewById(R.id.radiusView);
		ImageView close = (ImageView) v.findViewById(R.id.close_img_button);
		close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layout.setVisibility(GONE);
			}
		});



		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);
	}

	@Override
	public void onProgressChanged(SeekBar v, int progress, boolean isUser) {
		TextView tv = (TextView)findViewById(R.id.radiusView);
		radius = progress;
		tv.setText(Integer.toString(progress)+"m");
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}





	protected void saveLocation() {
		Location loc = new Location(getName(), overlayItem.getPoint().getLatitudeE6(), overlayItem.getPoint().getLongitudeE6(), getRadius(), "none");
		LocationDB.getInstance(getContext()).addLocation(loc);		
	}


	private String getName() {
		// TODO Auto-generated method stub
		return name.getSelectedItem().toString();
	}


	private long getRadius() {
		// TODO Auto-generated method stub
		return radius;
	}


	/**
	 * Sets the view data from a given overlay item.
	 * 
	 * @param item - The overlay item containing the relevant view data 
	 * (title and snippet). 
	 */
	public void setData(OverlayItem item) {
		overlayItem = item;
		populateSpinner();
		layout.setVisibility(VISIBLE);
		if (item.getTitle() != null) {
			title.setVisibility(VISIBLE);
			title.setText(item.getTitle());
		} else {
			title.setVisibility(GONE);
		}
		if (item.getSnippet() != null) {
			snippet.setVisibility(VISIBLE);
			snippet.setText(item.getSnippet());
		} else {
			snippet.setVisibility(GONE);
		}
		name.setVisibility(VISIBLE);
		sb.setVisibility(VISIBLE);
		saveLocation.setVisibility(VISIBLE);


	}
	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * Remember this Location 
	 * Best Guess Name title
	 * Visited form ... to ...
	 * Spinner of name guesses, with "Enter Custom Text" as option
	 * Radius  -----RadiusSeekBar--------
	 * 
	 * @param context - The activity context.
	 * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
	 * when rendering this view.
	 */
	
private void populateSpinner() {
	Geocoder geocoder = new Geocoder(this.getContext(), Locale.getDefault());
	List<Address> addresses = new ArrayList<Address>();
	List<String> addressStrings = new ArrayList<String>();
	try {
		 addresses = geocoder.getFromLocation(((double)overlayItem.getPoint().getLatitudeE6())/1000000, ((double)overlayItem.getPoint().getLongitudeE6())/1000000, 7);
	} catch (IOException e) {
		// TODO Auto-generated catch block
	}
	for (Address a : addresses) {
		if(a.getFeatureName() != null) {
			addressStrings.add(a.getFeatureName());
		} else {
			int numLines = a.getMaxAddressLineIndex();
			if (numLines != -1) {
				String addString = "";
				for ( int i=0; i<= numLines;i++) {
					addString += a.getAddressLine(i);
				}
				if( addString != "") {
					addressStrings.add(addString);
				}
			}
		}
	}
	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, addressStrings);
	name.setAdapter(adapter);
	
}

}
