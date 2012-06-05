package edu.ucsb.cs.cs290i.service.detectors.location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import edu.ucsb.cs.cs290i.R;

/**
 * A very simple application to handle Voice Recognition intents
 * and display the results
 */
public class AddLocationActivity extends Activity
{

	private static final int REQUEST_CODE = 1234;

	/**
	 * Called with the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_recognition);

		Button speakButton = (Button) findViewById(R.id.speakButton);

		Spinner geoSpimmer = (Spinner) findViewById(R.id.geocoded_string_results);
		
		int[] latLon = getIntent().getIntArrayExtra("latLon");
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		List<Address> addresses = new ArrayList<Address>();
		List<String> addressStrings = new ArrayList<String>();
		try {
			 addresses = geocoder.getFromLocation(((double)latLon[0])/1000000, ((double)latLon[1])/1000000, 5);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		Spinner geocoderResults = (Spinner) findViewById(R.id.center_places_spinner);
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
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, addressStrings);
		geocoderResults.setAdapter(adapter);
		
		// Disable button if no recognition service is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(
				new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0)
		{
			speakButton.setEnabled(false);
			speakButton.setText("N/A");
		}
		
	}

	/**
	 * Handle the action of the button being clicked
	 */
	public void speakButtonClicked(View v)
	{
		startVoiceRecognitionActivity();
	}

	/**
	 * Fire an intent to start the voice recognition activity.
	 */
	private void startVoiceRecognitionActivity()
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe a Location...");
		startActivityForResult(intent, REQUEST_CODE);
	}

	/**
	 * Handle the results from the voice recognition activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
		{
			// Populate the wordsList with the String values the recognition engine thought it heard
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			Spinner geocodedSpinner = (Spinner)findViewById(R.id.geocoded_string_results);
			geocodedSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, matches));
		}
		super.onActivityResult(requestCode, resultCode, data);
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
}
