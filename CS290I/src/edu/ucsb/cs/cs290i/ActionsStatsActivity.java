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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.alohar.core.Alohar;
import com.alohar.user.callback.ALEventListener;
import com.alohar.user.content.data.ALEvents;
import com.alohar.user.content.data.PlaceProfile;
import com.alohar.user.content.data.UserStay;

import edu.ucsb.cs.cs290i.service.Action;
import edu.ucsb.cs.cs290i.service.DetectorService;
import edu.ucsb.cs.cs290i.service.DetectorService.DetectorServiceBinder;

public class ActionsStatsActivity extends Activity {
    protected static final long TEN_MINUTES = 1000 * 60 * 10;
    private DetectorService service;
    private TextView list;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName c, IBinder iBinder) {
            DetectorServiceBinder binder = (DetectorServiceBinder) iBinder;
            service = binder.getService();
        }


        @Override
        public void onServiceDisconnected(ComponentName c) {
        }

    };

    public class OnTimeRangeSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
        	StringBuilder text = new StringBuilder();
        	parent.getItemAtPosition(pos).toString();
        	List<Action> actions = 
        					service.matchActions(System.currentTimeMillis() - TEN_MINUTES,
        							System.currentTimeMillis());
        	for (Action a : actions) {
        		text.append(a.getName());
        		text.append("\n");
        	}
        	list.setText(text);
        	
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       	Intent serviceIntent = new Intent(this, DetectorService.class);
    	startService(serviceIntent);	
    	bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        
//        setContentView(R.layout.actions_stats);
//        list = (TextView) findViewById(R.id.list);
//        
//        Spinner spinner = (Spinner) findViewById(R.id.spinner);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
//                this, R.array.time_intervals, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
        //spinner.setOnItemSelectedListener(new OnTimeRangeSelectedListener());
        
     }


  
}