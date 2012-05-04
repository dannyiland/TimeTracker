package edu.ucsb.cs.cs290i;

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
import edu.ucsb.cs.cs290i.service.DetectorService;
import edu.ucsb.cs.cs290i.service.DetectorService.DetectorServiceBinder;
import edu.ucsb.cs.cs290i.service.detectors.ColorDetector;
import edu.ucsb.cs.cs290i.service.detectors.Event;

public class GetEventsActivity extends Activity {
    protected static final long TEN_MINUTES = 1000 * 60 * 10;

    private DetectorService service;

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

        addDetector.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                service.registerDetector(ColorDetector.class, color.getText().toString());
            }
        });

        getEvents.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                StringBuilder text = new StringBuilder();

                List<Event> events = service.getEvents(System.currentTimeMillis() - TEN_MINUTES,
                        System.currentTimeMillis());

                for (Event e : events) {
                    text.append(e.getName());
                    text.append(": ");
                    text.append(e.getDescription());
                    text.append("\n");
                }
                
                list.setText(text);
            }
        });

        Intent serviceIntent = new Intent(this, DetectorService.class);
        startService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }
}