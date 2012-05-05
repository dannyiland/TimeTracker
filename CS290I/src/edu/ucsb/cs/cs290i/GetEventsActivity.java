// Alohar API keys:
// Application ID: 89 
// Secret Key: 04834345f2729ceafc4c9f750ade319610560103 

package edu.ucsb.cs.cs290i;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alohar.core.Alohar;
import com.alohar.user.callback.ALEventListener;
import com.alohar.user.content.data.ALEvents;
import com.alohar.user.content.data.PlaceProfile;

import edu.ucsb.cs.cs290i.service.DetectorService;
import edu.ucsb.cs.cs290i.service.DetectorService.DetectorServiceBinder;
import edu.ucsb.cs.cs290i.service.detectors.ColorDetector;
import edu.ucsb.cs.cs290i.service.detectors.Event;

public class GetEventsActivity extends Activity {
	protected static final long TEN_MINUTES = 1000 * 60 * 10;
	private static final int APP_ID = 89;
	private static final String API_KEY = "04834345f2729ceafc4c9f750ade319610560103";
	protected String alohar_uid = "0";
	private DetectorService service;
	private Alohar mAlohar;


	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName c, IBinder iBinder) {
			DetectorServiceBinder binder = (DetectorServiceBinder) iBinder;
			service = binder.getService();

			// Register a detector.
			service.registerDetector(ColorDetector.class, "green");
		}


		@Override
		public void onServiceDisconnected(ComponentName c) {
		}

	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final EditText color = (EditText) findViewById(R.id.color);
		final Button addDetector = (Button) findViewById(R.id.add);
		final Button getEvents = (Button) findViewById(R.id.get_events);
		final TextView list = (TextView) findViewById(R.id.list);

		// Alohar Initialization
		alohar_uid = getSavedAloharId();
		Alohar.init(getApplication());
		mAlohar = Alohar.getInstance();
		if (alohar_uid != "0") {
			mAlohar.authenticate(alohar_uid, APP_ID, API_KEY, new ALEventListener(){
				@Override
				public void handleEvent(ALEvents event, Object data) {
					if (event == ALEvents.AUTHENTICATE_CALLBACK) {
						if (data instanceof String) {
							alohar_uid = (String)data;
							// Save UID for future use
						}
					} else if (event == ALEvents.GENERAL_ERROR_CALLBACK 
							|| event == ALEvents.SERVER_ERROR_CALLBACK) {
						// Fail to register or authenticate
					}
				}
			});
		} else {
			mAlohar.register(APP_ID, API_KEY, new ALEventListener(){
				@Override
				public void handleEvent(ALEvents event, Object data) {
					if (event == ALEvents.REGISTRATION_CALLBACK) {
						if (data instanceof String) {
							alohar_uid = (String)data;
							// Save UID for future use
						}
					} else if (event == ALEvents.GENERAL_ERROR_CALLBACK 
							|| event == ALEvents.SERVER_ERROR_CALLBACK) {
						// Fail to register or authenticate
					}

				}
			});
		}
		mAlohar.startServices();
		
		addDetector.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				service.registerDetector(ColorDetector.class, color.getText().toString());
			}
		});

		getEvents.setOnClickListener(new OnClickListener() {
			StringBuilder text = new StringBuilder();

			public void onClick(View v) {

				List<Event> events = service.getEvents(System.currentTimeMillis() - TEN_MINUTES,
						System.currentTimeMillis());

				for (Event e : events) {
					text.append(e.getName());
					text.append(": ");
					text.append(e.getDescription());
					text.append("\n");
				}
				text.append("Last Known Location:\n");
				mAlohar.getPlaceManager().searchPlaces(System.currentTimeMillis() - TEN_MINUTES, System.currentTimeMillis(), "null",1,1000, new ALEventListener(){
					@Override
					public void handleEvent(ALEvents event, Object data) {
						  if (event == ALEvents.PLACES_QUERY_CALLBACK) {
					            ArrayList<PlaceProfile> places = (ArrayList<PlaceProfile>)data;
					            // Post process the places
					            for (PlaceProfile place: places) {
					            	text.append(place.getName());
									text.append(": ");
									text.append(place.getLatE6() + "," + place.getLngE6());
									text.append("\n");
					            }
					        } else if (event == ALEvents.GENERAL_ERROR_CALLBACK 
					        || event == ALEvents.SERVER_ERROR_CALLBACK) {
					            // Error handling
					        }
					    }
					});

				list.setText(text);
			}
		});

		Intent serviceIntent = new Intent(this, DetectorService.class);
		startService(serviceIntent);
		bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
	}


	private String getSavedAloharId() {
		// TODO: persist this variable, alohar_id, across sessions! 
		return "0";
	}
}