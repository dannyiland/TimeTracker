// Alohar API keys:
// Application ID: 89 
// Secret Key: 04834345f2729ceafc4c9f750ade319610560103 

package edu.ucsb.cs.cs290i;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import edu.ucsb.cs.cs290i.service.Action;
import edu.ucsb.cs.cs290i.service.DetectorService;
import edu.ucsb.cs.cs290i.service.DetectorService.DetectorServiceBinder;

public class ActionsStatsActivity extends Activity {

    protected static final long DAY = 1000 * 60 * 60 * 24;
	static final long[] TIME_RANGES = {DAY, DAY * 7, DAY * 28};

    private DetectorService service;
    private TextView list;
    private ServiceConnection connection = new ServiceConnection() {
       
        @Override
        public void onServiceConnected(ComponentName c, IBinder iBinder) {
            Log.e("!!", "Service Connected");

            DetectorServiceBinder binder = (DetectorServiceBinder) iBinder;
            service = binder.getService();
        }


        @Override
        public void onServiceDisconnected(ComponentName c) {
        }

    };
    private Spinner spinner;

    public class OnTimeRangeSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
           // Log.e("!!", "Selected");
        	StringBuilder text = new StringBuilder();
        	parent.getItemAtPosition(pos).toString();
        	List<Action> actions = 
        					service.matchActions(System.currentTimeMillis() - TIME_RANGES[pos],
        							System.currentTimeMillis());
        	for (Action a : actions) {
        	    System.out.println(a.getName());
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
       	Intent serviceIntent = new Intent(getApplicationContext(), DetectorService.class);
    	getApplicationContext().startService(serviceIntent);	
    	getApplicationContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        
        setContentView(R.layout.actions_stats);
        list = (TextView) findViewById(R.id.list);
        
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.time_intervals, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new OnTimeRangeSelectedListener());

     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions_stats_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.manage:
            manageAction();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }


    private void manageAction() {
        startActivity(new Intent(this, ActionsListActivity.class));
    }
  
}