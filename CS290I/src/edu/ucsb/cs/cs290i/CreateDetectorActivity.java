package edu.ucsb.cs.cs290i;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.ucsb.cs.cs290i.service.detectors.Detector;
import edu.ucsb.cs.cs290i.service.detectors.calendar.CalendarDetector;
import edu.ucsb.cs.cs290i.service.detectors.sensors.WorkoutDetector;

public class CreateDetectorActivity extends ListActivity {

    // List of available Detectors.
    @SuppressWarnings("unchecked")
    private static final Class<Detector>[] DETECTORS = new Class[] {
            WorkoutDetector.class,
            CalendarDetector.class,
    };
    
    // And their display names.
    private static final String[] NAMES = {
            "Workout Detector",
            "Calendar Event Detector",
    };

    private String selectedDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new ArrayAdapter<Class<Detector>>(this, android.R.layout.simple_list_item_1,
                (Class<Detector>[]) DETECTORS) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setText(NAMES[position]);
                return view;
            }

        });

    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // The detector's class.
        Class<Detector> detector = DETECTORS[position];
        selectedDetector = detector.getName();

        // The detector configuration Activity's class.
        Class<? extends Activity> configClass = null;
        for (Class<?> c : detector.getClasses()) {
            System.out.println(c);
            if (c.getName().endsWith("$Config") && Activity.class.isAssignableFrom(c)) {
                @SuppressWarnings("unchecked")
                Class<? extends Activity> ac = (Class<? extends Activity>) c;
                configClass = ac;
                break;
            }
        }

        if (configClass == null) {
            // Detector does not have a Config Activity, skip displaying it.
            onActivityResult(0, RESULT_OK, new Intent());
        } else {
            startActivityForResult(new Intent(this, configClass), 0);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        data.putExtra("detector", selectedDetector);
        // data also contains a "config" extra with the detector's configuration.
        setResult(RESULT_OK, data);
        finish();
    }

}
