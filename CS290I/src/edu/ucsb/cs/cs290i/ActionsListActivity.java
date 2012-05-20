package edu.ucsb.cs.cs290i;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.ucsb.cs.cs290i.service.Action;
import edu.ucsb.cs.cs290i.service.DetectorService;
import edu.ucsb.cs.cs290i.service.DetectorService.DetectorServiceBinder;

public class ActionsListActivity extends ListActivity {
    private DetectorService service;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName c, IBinder iBinder) {
            DetectorServiceBinder binder = (DetectorServiceBinder) iBinder;
            service = binder.getService();
            updateList();

        }


        @Override
        public void onServiceDisconnected(ComponentName c) {
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent serviceIntent = new Intent(this, DetectorService.class);
        startService(serviceIntent);

        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Toast.makeText(ActionsListActivity.this, "Action " + ((TextView) view).getText(),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }


    protected void updateList() {
        if (service != null) {
            setListAdapter(new ArrayAdapter<Action>(ActionsListActivity.this, android.R.layout.simple_list_item_1,
                    service.getRegisteredActions()));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions_list_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.add:
            addAction();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }


    private void addAction() {
        startActivity(new Intent(this, CreateActionActivity.class));
    }

}
