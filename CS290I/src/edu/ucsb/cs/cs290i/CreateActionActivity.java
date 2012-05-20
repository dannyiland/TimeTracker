package edu.ucsb.cs.cs290i;

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
import edu.ucsb.cs.cs290i.service.Action;
import edu.ucsb.cs.cs290i.service.DetectorService;
import edu.ucsb.cs.cs290i.service.DetectorService.DetectorServiceBinder;
import edu.ucsb.cs.cs290i.service.detectors.calendar.CalendarDetector;

public class CreateActionActivity extends Activity {

    protected DetectorService service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_action);

        Button add = (Button) findViewById(R.id.add_cal_detector);

        add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // onActivityResult will be called with the parameters set in
                // CalendarDetector.Config.
                startActivityForResult(new Intent(CreateActionActivity.this, CalendarDetector.Config.class), 0);
            }
        });

        Intent serviceIntent = new Intent(this, DetectorService.class);
        startService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Create a new Action with a single calendar detector.

        // First, create the Detector.
        String[] config = data.getExtras().getStringArray("config"); // From config Activity.
        CalendarDetector detector = new CalendarDetector(config);

        // Then, create the action and add the detector.
        Action action = new Action();
        action.setName(String.format("Action for calendar event containing \"%s\"", config[0]));
        action.addDetector(detector);

        // Register the action with the service.
        service.registerAction(action);

        // Close activity and return to actions list.
        finish();
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName c, IBinder iBinder) {
            DetectorServiceBinder binder = (DetectorServiceBinder) iBinder;
            service = binder.getService();
        }


        @Override
        public void onServiceDisconnected(ComponentName c) {
            // TODO
        }

    };
}
