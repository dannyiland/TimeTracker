package edu.ucsb.cs.cs290i;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.ucsb.cs.cs290i.service.Action;
import edu.ucsb.cs.cs290i.service.DetectorService;
import edu.ucsb.cs.cs290i.service.DetectorService.DetectorServiceBinder;
import edu.ucsb.cs.cs290i.service.detectors.Detector;

public class CreateActionActivity extends ListActivity {

    protected DetectorService service;

    private List<Detector> detectors;


    public CreateActionActivity() {
        detectors = new ArrayList<Detector>();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_action);

        Button finish = (Button) findViewById(R.id.finish);
        Button addDetector = (Button) findViewById(R.id.add_detector);

        final EditText name = (EditText) findViewById(R.id.action_name);
        final EditText description = (EditText) findViewById(R.id.action_desc);

        addDetector.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(CreateActionActivity.this, CreateDetectorActivity.class), 0);
            }
        });

        finish.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Action action = new Action();
                action.setName(name.getText().toString());
                action.setDescription(description.getText().toString());
                for (Detector d : detectors) {
                    action.addDetector(d);
                }
                service.registerAction(action);

                // Close activity and return to actions list.
                finish();

            }
        });

        Intent serviceIntent = new Intent(this, DetectorService.class);
        startService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        // Create the detector selected with CreateDetectorActivity.

        // First, create the Detector.
        String[] config = data.getStringArrayExtra("config"); // From config Activity.

        Detector detector = null;
        try {
            detector = (Detector) Class.forName(data.getStringExtra("detector"))
                    .getConstructor(new String[0].getClass()).newInstance((Object) config);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (detector != null) {
            detectors.add(detector);
            updateList();
        } else {
            Toast.makeText(this, "Unable to create detector " + data.getStringExtra("detector"), Toast.LENGTH_LONG)
                    .show();
        }
    }


    private void updateList() {
        setListAdapter(new ArrayAdapter<Detector>(this, android.R.layout.simple_list_item_1, detectors));
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
