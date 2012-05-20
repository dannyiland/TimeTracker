package edu.ucsb.cs.cs290i.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import edu.ucsb.cs.cs290i.service.DetectorService.DetectorServiceBinder;

public class DetectorServiceConnection implements ServiceConnection {

    private DetectorService service;
    private boolean isBound;
    private final Context c;


    public DetectorServiceConnection(Context c) {
        this.c = c;
        bind();
    }


    private void bind() {
        if (!isBound) {
            Intent serviceIntent = new Intent(c, DetectorService.class);
            c.startService(serviceIntent);
            c.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        }
    }


    @Override
    public void onServiceConnected(ComponentName c, IBinder iBinder) {
        DetectorServiceBinder binder = (DetectorServiceBinder) iBinder;
        service = binder.getService();
    }


    public DetectorService getService() {
        return service;
    }


    @Override
    public void onServiceDisconnected(ComponentName c1) {
        if (isBound) {
            c.unbindService(this);
        }
    }

}
